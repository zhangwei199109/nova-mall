package com.example.user.api;

import com.example.common.dto.Result;
import com.example.user.api.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户服务", description = "用户对外 HTTP 契约")
@RequestMapping("/user")
public interface UserApi {

    @Operation(summary = "获取所有用户")
    @GetMapping("/list")
    Result<List<UserDTO>> list();

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    Result<UserDTO> detail(@PathVariable Long id);

    @Operation(summary = "创建用户")
    @PostMapping
    Result<UserDTO> create(@Valid @RequestBody UserDTO user);

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    Result<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserDTO user);

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    Result<Void> delete(@PathVariable Long id);

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    Result<String> health();
}

