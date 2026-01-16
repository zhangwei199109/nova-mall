package com.example.ads.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ads.api.dto.CampaignDTO;
import com.example.ads.api.dto.CreativeDTO;
import com.example.ads.api.dto.TrackingLinkRequest;
import com.example.ads.api.dto.TrackingLinkResponse;
import com.example.ads.service.AdsService;
import com.example.ads.service.channel.MediaChannelClient;
import com.example.ads.service.entity.Campaign;
import com.example.ads.service.entity.ClickLog;
import com.example.ads.service.entity.ConversionLog;
import com.example.ads.service.entity.Creative;
import com.example.ads.service.mapper.CampaignMapper;
import com.example.ads.service.mapper.ClickLogMapper;
import com.example.ads.service.mapper.ConversionLogMapper;
import com.example.ads.service.mapper.CreativeMapper;
import com.example.common.dto.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 广告服务实现：活动/创意 CRUD、追踪链接生成、点击与转化埋点。
 * - 链接生成：拼接 utm 与 traceId，便于后续归因。
 * - 点击埋点：落库 traceId/IP/UA。
 * - 转化埋点：traceId + 订单号去重，关联最近点击。
 */
@Service
public class AdsServiceImpl implements AdsService {

    private static final Logger log = LoggerFactory.getLogger(AdsServiceImpl.class);

    private final CampaignMapper campaignMapper;
    private final CreativeMapper creativeMapper;
    private final ClickLogMapper clickLogMapper;
    private final ConversionLogMapper conversionLogMapper;
    private final ConversionRetryRepository conversionRetryRepository;
    private final List<MediaChannelClient> channelClients;

    public AdsServiceImpl(CampaignMapper campaignMapper, CreativeMapper creativeMapper,
                          ClickLogMapper clickLogMapper, ConversionLogMapper conversionLogMapper,
                          ConversionRetryRepository conversionRetryRepository,
                          List<MediaChannelClient> channelClients) {
        this.campaignMapper = campaignMapper;
        this.creativeMapper = creativeMapper;
        this.clickLogMapper = clickLogMapper;
        this.conversionLogMapper = conversionLogMapper;
        this.conversionRetryRepository = conversionRetryRepository;
        this.channelClients = channelClients;
    }

    @Override
    public CampaignDTO createCampaign(CampaignDTO dto) {
        Campaign po = new Campaign();
        BeanUtils.copyProperties(dto, po);
        po.setEnabled(Boolean.TRUE.equals(dto.getEnabled()));
        campaignMapper.insert(po);
        dto.setId(po.getId());
        return dto;
    }

    @Override
    public List<CampaignDTO> listCampaigns() {
        return campaignMapper.selectList(null).stream().map(this::toCampaignDTO).collect(Collectors.toList());
    }

    @Override
    public CreativeDTO createCreative(CreativeDTO dto) {
        Creative po = new Creative();
        BeanUtils.copyProperties(dto, po);
        po.setEnabled(Boolean.TRUE.equals(dto.getEnabled()));
        creativeMapper.insert(po);
        dto.setId(po.getId());
        return dto;
    }

    @Override
    public List<CreativeDTO> listCreatives(Long campaignId) {
        LambdaQueryWrapper<Creative> qw = new LambdaQueryWrapper<>();
        if (campaignId != null) {
            qw.eq(Creative::getCampaignId, campaignId);
        }
        return creativeMapper.selectList(qw).stream().map(this::toCreativeDTO).collect(Collectors.toList());
    }

    @Override
    public TrackingLinkResponse genLink(TrackingLinkRequest req) {
        Creative creative = creativeMapper.selectById(req.getCreativeId());
        if (creative == null || !Boolean.TRUE.equals(creative.getEnabled())) {
            return new TrackingLinkResponse(null, null);
        }
        String traceId = (req.getTraceId() == null || req.getTraceId().isBlank())
                ? UUID.randomUUID().toString()
                : req.getTraceId();
        String url = creative.getLandingUrl();
        String utm = creative.getUtm();
        if (utm != null && !utm.isBlank()) {
            url = appendQuery(url, utm);
        }
        url = appendQuery(url, "traceId=" + traceId + "&cid=" + creative.getCampaignId() + "&crid=" + creative.getId());
        return new TrackingLinkResponse(traceId, url);
    }

    @Override
    @Transactional
    public boolean trackClick(String traceId, Long creativeId, Long campaignId, String ip, String ua) {
        Creative creative = creativeMapper.selectById(creativeId);
        if (creative == null || !Boolean.TRUE.equals(creative.getEnabled())) {
            return false;
        }
        ClickLog log = new ClickLog();
        log.setTraceId(traceId);
        log.setCreativeId(creativeId);
        log.setCampaignId(campaignId != null ? campaignId : creative.getCampaignId());
        log.setIp(ip);
        log.setUserAgent(ua);
        log.setCreateTime(LocalDateTime.now());
        clickLogMapper.insert(log);
        return true;
    }

    @Override
    @Transactional
    public boolean trackConversion(String traceId, String orderNo) {
        // 简单去重：若已存在该 traceId + orderNo 则不再插入
        LambdaQueryWrapper<ConversionLog> qw = new LambdaQueryWrapper<>();
        qw.eq(ConversionLog::getTraceId, traceId).eq(ConversionLog::getOrderNo, orderNo);
        if (conversionLogMapper.selectCount(qw) > 0) {
            return true;
        }
        // 关联最近的 click（如果有）
        ClickLog last = clickLogMapper.selectList(new LambdaQueryWrapper<ClickLog>()
                .eq(ClickLog::getTraceId, traceId)
                .orderByDesc(ClickLog::getCreateTime)
                .last("limit 1"))
                .stream().findFirst().orElse(null);

        ConversionLog log = new ConversionLog();
        log.setTraceId(traceId);
        log.setOrderNo(orderNo);
        if (last != null) {
            log.setCampaignId(last.getCampaignId());
            log.setCreativeId(last.getCreativeId());
        }
        log.setCreateTime(LocalDateTime.now());
        conversionLogMapper.insert(log);

        // 根据活动渠道回传媒体（尽力而为，失败记录重试任务）
        if (log.getCampaignId() != null && channelClients != null) {
            Campaign campaign = campaignMapper.selectById(log.getCampaignId());
            if (campaign != null && campaign.getChannel() != null) {
                for (MediaChannelClient client : channelClients) {
                    if (campaign.getChannel().equalsIgnoreCase(client.channel())) {
                        try {
                            client.sendConversion(log, null, null);
                        } catch (Exception e) {
                            log.warn("conversion send failed channel={} traceId={} orderNo={} err={}",
                                    campaign.getChannel(), log.getTraceId(), log.getOrderNo(), e.toString());
                            if (conversionRetryRepository != null) {
                                ConversionRetryTask task = new ConversionRetryTask();
                                task.setChannel(campaign.getChannel());
                                task.setTraceId(log.getTraceId());
                                task.setOrderNo(log.getOrderNo());
                                task.setCampaignId(log.getCampaignId());
                                task.setCreativeId(log.getCreativeId());
                                task.setRetry(1);
                                task.setLastError(e.toString());
                                task.setNextRunTime(LocalDateTime.now().plusMinutes(1));
                                conversionRetryRepository.save(task);
                            }
                        }
                        break;
                    }
                }
            }
        }
        return true;
    }

    private CampaignDTO toCampaignDTO(Campaign po) {
        CampaignDTO dto = new CampaignDTO();
        BeanUtils.copyProperties(po, dto);
        return dto;
    }

    private CreativeDTO toCreativeDTO(Creative po) {
        CreativeDTO dto = new CreativeDTO();
        BeanUtils.copyProperties(po, dto);
        return dto;
    }

    private String appendQuery(String url, String query) {
        if (url.contains("?")) {
            return url + "&" + query;
        }
        return url + "?" + query;
    }
}

