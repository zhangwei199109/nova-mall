package com.example.order.web.controller;

import com.example.common.dto.Result;
import com.example.common.web.AuthContext;
import com.example.order.api.SeckillApi;
import com.example.order.api.dto.SeckillActivityDTO;
import com.example.order.api.dto.SeckillOrderResultDTO;
import com.example.order.api.dto.SeckillPlaceRequest;
import com.example.order.web.service.SeckillService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController implements SeckillApi {

    private final SeckillService seckillService;
    private final AuthContext authContext;

    public SeckillController(SeckillService seckillService, AuthContext authContext) {
        this.seckillService = seckillService;
        this.authContext = authContext;
    }

    @Override
    public Result<List<SeckillActivityDTO>> activities() {
        return Result.success(seckillService.activities());
    }

    @Override
    public Result<SeckillOrderResultDTO> place(String idemKey, @Valid SeckillPlaceRequest req) {
        Long userId = authContext.currentUserId();
        return Result.success(seckillService.place(userId, idemKey, req));
    }

    @Override
    public Result<SeckillOrderResultDTO> result(Long activityId) {
        Long userId = authContext.currentUserId();
        return Result.success(seckillService.result(userId, activityId));
    }
}



















