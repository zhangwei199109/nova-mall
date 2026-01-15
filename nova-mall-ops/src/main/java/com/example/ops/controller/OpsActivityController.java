package com.example.ops.controller;

import com.example.common.dto.Result;
import com.example.ops.dto.ActivityAdminDTO;
import com.example.ops.service.OpsActivityService;
import com.example.product.api.ProductApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "运营后台", description = "活动配置与商品上下架")
@RequiredArgsConstructor
public class OpsActivityController {

    private final OpsActivityService opsActivityService;
    private final ProductApi productApi;

    @GetMapping("/activities")
    @Operation(summary = "活动列表")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<List<ActivityAdminDTO>> listAll() {
        return Result.success(opsActivityService.listAll());
    }

    @GetMapping("/activities/active")
    @Operation(summary = "查询在线且在时间窗内的活动（给下游拉取）")
    public Result<List<ActivityAdminDTO>> listActive() {
        return Result.success(opsActivityService.listActive());
    }

    @PostMapping("/activities")
    @Operation(summary = "新建活动")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<ActivityAdminDTO> create(@Valid @RequestBody ActivityAdminDTO dto) {
        return Result.success(opsActivityService.create(dto));
    }

    @PutMapping("/activities/{id}")
    @Operation(summary = "更新活动")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<ActivityAdminDTO> update(@PathVariable Long id, @Valid @RequestBody ActivityAdminDTO dto) {
        dto.setId(id);
        return Result.success(opsActivityService.update(dto));
    }

    @PostMapping("/activities/{id}/online")
    @Operation(summary = "活动上架（上线）")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<Boolean> online(@PathVariable Long id) {
        return Result.success(opsActivityService.online(id));
    }

    @PostMapping("/activities/{id}/offline")
    @Operation(summary = "活动下架（下线）")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<Boolean> offline(@PathVariable Long id) {
        return Result.success(opsActivityService.offline(id));
    }

    @PostMapping("/products/{id}/on-shelf")
    @Operation(summary = "商品上架")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<Boolean> productOnShelf(@PathVariable Long id) {
        return productApi.onShelf(id);
    }

    @PostMapping("/products/{id}/off-shelf")
    @Operation(summary = "商品下架")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<Boolean> productOffShelf(@PathVariable Long id) {
        return productApi.offShelf(id);
    }
}

