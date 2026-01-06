package com.example.user.api;

import com.example.common.dto.Result;
import com.example.user.api.dto.UserDTO;
import com.example.user.api.dto.AddressDTO;
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

    @Operation(summary = "地址列表")
    @GetMapping("/{userId}/addresses")
    Result<java.util.List<AddressDTO>> listAddresses(@PathVariable("userId") Long userId);

    @Operation(summary = "新增地址")
    @PostMapping("/{userId}/addresses")
    Result<AddressDTO> createAddress(@PathVariable("userId") Long userId,
                                     @Valid @RequestBody AddressDTO dto);

    @Operation(summary = "更新地址")
    @PutMapping("/{userId}/addresses/{addressId}")
    Result<AddressDTO> updateAddress(@PathVariable("userId") Long userId,
                                     @PathVariable("addressId") Long addressId,
                                     @Valid @RequestBody AddressDTO dto);

    @Operation(summary = "删除地址")
    @DeleteMapping("/{userId}/addresses/{addressId}")
    Result<Boolean> deleteAddress(@PathVariable("userId") Long userId,
                                  @PathVariable("addressId") Long addressId);

    @Operation(summary = "设为默认地址")
    @PostMapping("/{userId}/addresses/{addressId}/default")
    Result<Boolean> setDefaultAddress(@PathVariable("userId") Long userId,
                                      @PathVariable("addressId") Long addressId);
}

