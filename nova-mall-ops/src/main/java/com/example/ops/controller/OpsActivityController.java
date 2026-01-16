package com.example.ops.controller;

import com.example.common.dto.Result;
import com.example.ops.dto.ActivityAdminDTO;
import com.example.ops.service.OpsActivityService;
import com.example.product.api.ProductApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class OpsActivityController {

    private final OpsActivityService opsActivityService;
    private final ProductApi productApi;

    @GetMapping("/activities")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<List<ActivityAdminDTO>> listAll() {
        return Result.success(opsActivityService.listAll());
    }

    @GetMapping("/activities/active")
    public Result<List<ActivityAdminDTO>> listActive() {
        return Result.success(opsActivityService.listActive());
    }

    @PostMapping("/activities")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<ActivityAdminDTO> create(@Valid @RequestBody ActivityAdminDTO dto) {
        return Result.success(opsActivityService.create(dto));
    }

    @PutMapping("/activities/{id}")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<ActivityAdminDTO> update(@PathVariable Long id, @Valid @RequestBody ActivityAdminDTO dto) {
        dto.setId(id);
        return Result.success(opsActivityService.update(dto));
    }

    @PostMapping("/activities/{id}/online")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<Boolean> online(@PathVariable Long id) {
        return Result.success(opsActivityService.online(id));
    }

    @PostMapping("/activities/{id}/offline")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<Boolean> offline(@PathVariable Long id) {
        return Result.success(opsActivityService.offline(id));
    }

    @PostMapping("/products/{id}/on-shelf")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<Boolean> productOnShelf(@PathVariable Long id) {
        return productApi.onShelf(id);
    }

    @PostMapping("/products/{id}/off-shelf")
    @PreAuthorize("hasRole('OPS_ADMIN')")
    public Result<Boolean> productOffShelf(@PathVariable Long id) {
        return productApi.offShelf(id);
    }
}

