package com.example.ads.api;

import com.example.ads.api.dto.CampaignDTO;
import com.example.ads.api.dto.CreativeDTO;
import com.example.ads.api.dto.TrackingLinkRequest;
import com.example.ads.api.dto.TrackingLinkResponse;
import com.example.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** 对外广告服务契约：活动/创意管理、追踪链接生成、点击与转化埋点。 */
@FeignClient(name = "ads-service", url = "${service.ads.base-url:http://localhost:8090}", path = "/ads")
@Tag(name = "广告接口")
public interface AdsApi {

    @Operation(summary = "创建活动", description = "创建广告活动，包含渠道、预算、有效期等")
    @PostMapping("/campaign")
    Result<CampaignDTO> createCampaign(@RequestBody CampaignDTO dto);

    @Operation(summary = "活动列表", description = "查询全部活动")
    @GetMapping("/campaign")
    Result<List<CampaignDTO>> listCampaigns();

    @Operation(summary = "创建创意", description = "创建广告素材/落地页，关联活动")
    @PostMapping("/creative")
    Result<CreativeDTO> createCreative(@RequestBody CreativeDTO dto);

    @Operation(summary = "创意列表", description = "按活动过滤创意，campaignId 可选")
    @GetMapping("/creative")
    Result<List<CreativeDTO>> listCreatives(@RequestParam(value = "campaignId", required = false) Long campaignId);

    @Operation(summary = "生成追踪链接", description = "生成带 traceId/utm 的落地页 URL")
    @PostMapping("/tracking/link")
    Result<TrackingLinkResponse> genLink(@RequestBody TrackingLinkRequest req);

    @Operation(summary = "点击埋点", description = "上报 traceId + 活动/创意 + IP/UA，用于后续转化归因")
    @PostMapping("/track/click")
    Result<Boolean> trackClick(@RequestParam("traceId") String traceId,
                               @RequestParam("creativeId") Long creativeId,
                               @RequestParam("campaignId") Long campaignId,
                               @RequestHeader(value = "X-Real-IP", required = false) String ip,
                               @RequestHeader(value = "User-Agent", required = false) String ua);

    @Operation(summary = "转化埋点", description = "上报 traceId + 订单号，关联最近点击做归因")
    @PostMapping("/track/conversion")
    Result<Boolean> trackConversion(@RequestParam("traceId") String traceId,
                                    @RequestParam("orderNo") String orderNo);
}

