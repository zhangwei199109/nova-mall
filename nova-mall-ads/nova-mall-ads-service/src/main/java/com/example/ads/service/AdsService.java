package com.example.ads.service;

import com.example.ads.api.dto.CampaignDTO;
import com.example.ads.api.dto.CreativeDTO;
import com.example.ads.api.dto.TrackingLinkRequest;
import com.example.ads.api.dto.TrackingLinkResponse;

import java.util.List;

/**
 * 广告服务：活动/创意管理、追踪链接生成、点击与转化埋点。
 */
public interface AdsService {
    CampaignDTO createCampaign(CampaignDTO dto);

    List<CampaignDTO> listCampaigns();

    CreativeDTO createCreative(CreativeDTO dto);

    List<CreativeDTO> listCreatives(Long campaignId);

    TrackingLinkResponse genLink(TrackingLinkRequest req);

    boolean trackClick(String traceId, Long creativeId, Long campaignId, String ip, String ua);

    boolean trackConversion(String traceId, String orderNo);
}

