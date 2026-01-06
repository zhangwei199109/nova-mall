package com.example.user.web.controller;

import com.example.common.dto.Result;
import com.example.user.api.UserApi;
import com.example.user.api.dto.UserDTO;
import com.example.user.api.dto.AddressDTO;
import com.example.user.service.UserAppService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class UserController implements UserApi {

    @Autowired
    private UserAppService userService;

    @Override
    public Result<List<UserDTO>> list() {
        return Result.success(userService.list());
    }

    @Override
    public Result<UserDTO> detail(@PathVariable Long id) {
        UserDTO user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    @Override
    public Result<UserDTO> create(@Valid @RequestBody UserDTO user) {
        return Result.success(userService.create(user));
    }

    @Override
    public Result<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserDTO user) {
        UserDTO updatedUser = userService.update(id, user);
        if (updatedUser == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(updatedUser);
    }

    @Override
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = userService.delete(id);
        if (!deleted) {
            return Result.error(404, "用户不存在");
        }
        return Result.success();
    }

    @Override
    public Result<String> health() {
        return Result.success("User service is running");
    }

    @Override
    public Result<List<AddressDTO>> listAddresses(Long userId) {
        return Result.success(userService.listAddress(userId));
    }

    @Override
    public Result<AddressDTO> createAddress(Long userId, @Valid @RequestBody AddressDTO dto) {
        return Result.success(userService.createAddress(userId, dto));
    }

    @Override
    public Result<AddressDTO> updateAddress(Long userId, Long addressId, @Valid @RequestBody AddressDTO dto) {
        AddressDTO updated = userService.updateAddress(userId, addressId, dto);
        if (updated == null) {
            return Result.error(404, "地址不存在");
        }
        return Result.success(updated);
    }

    @Override
    public Result<Boolean> deleteAddress(Long userId, Long addressId) {
        boolean ok = userService.deleteAddress(userId, addressId);
        if (!ok) {
            return Result.error(404, "地址不存在");
        }
        return Result.success(true);
    }

    @Override
    public Result<Boolean> setDefaultAddress(Long userId, Long addressId) {
        boolean ok = userService.setDefaultAddress(userId, addressId);
        if (!ok) {
            return Result.error(404, "地址不存在");
        }
        return Result.success(true);
    }
}

