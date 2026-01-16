package com.example.ads.web.controller;

import com.example.ads.api.AdsApi;
import com.example.ads.api.dto.CampaignDTO;
import com.example.ads.api.dto.CreativeDTO;
import com.example.ads.api.dto.TrackingLinkRequest;
import com.example.ads.api.dto.TrackingLinkResponse;
import com.example.ads.service.AdsService;
import com.example.common.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告 Web 层控制器，实现 AdsApi：用于活动/创意管理、追踪链接、点击/转化上报。
 */
@RestController
@RequestMapping("/ads")
public class AdsController implements AdsApi {

    private final AdsService adsService;

    public AdsController(AdsService adsService) {
        this.adsService = adsService;
    }

    @PostMapping("/campaign")
    @Override
    public Result<CampaignDTO> createCampaign(@Valid @RequestBody CampaignDTO dto) {
        return Result.success(adsService.createCampaign(dto));
    }

    @GetMapping("/campaign")
    @Override
    public Result<List<CampaignDTO>> listCampaigns() {
        return Result.success(adsService.listCampaigns());
    }

    @PostMapping("/creative")
    @Override
    public Result<CreativeDTO> createCreative(@Valid @RequestBody CreativeDTO dto) {
        return Result.success(adsService.createCreative(dto));
    }

    @GetMapping("/creative")
    @Override
    public Result<List<CreativeDTO>> listCreatives(@RequestParam(value = "campaignId", required = false) Long campaignId) {
        return Result.success(adsService.listCreatives(campaignId));
    }

    @PostMapping("/tracking/link")
    @Override
    public Result<TrackingLinkResponse> genLink(@Valid @RequestBody TrackingLinkRequest req) {
        return Result.success(adsService.genLink(req));
    }

    @PostMapping("/track/click")
    @Override
    public Result<Boolean> trackClick(@RequestParam("traceId") String traceId,
                                      @RequestParam("creativeId") Long creativeId,
                                      @RequestParam("campaignId") Long campaignId,
                                      @RequestHeader(value = "X-Real-IP", required = false) String ip,
                                      @RequestHeader(value = "User-Agent", required = false) String ua) {
        return Result.success(adsService.trackClick(traceId, creativeId, campaignId, ip, ua));
    }

    @PostMapping("/track/conversion")
    @Override
    public Result<Boolean> trackConversion(@RequestParam("traceId") String traceId,
                                           @RequestParam("orderNo") String orderNo) {
        return Result.success(adsService.trackConversion(traceId, orderNo));
    }
}

