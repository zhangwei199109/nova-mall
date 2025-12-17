package com.example.user.web.controller;

import com.example.common.dto.Result;
import com.example.user.api.UserApi;
import com.example.user.service.UserAppService;
import com.example.user.api.dto.UserDTO;
import com.example.user.web.convert.UserWebConvert;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class UserController implements UserApi {

    private final UserAppService userService;
    private final UserWebConvert userWebConvert;

    public UserController(UserAppService userService, UserWebConvert userWebConvert) {
        this.userService = userService;
        this.userWebConvert = userWebConvert;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<List<UserDTO>> list() {
        return Result.success(userService.list());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<UserDTO> detail(@PathVariable Long id) {
        UserDTO user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDTO> create(@Valid @RequestBody UserDTO user) {
        return Result.success(userService.create(userWebConvert.toCreateDto(user)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserDTO user) {
        UserDTO updatedUser = userService.update(id, userWebConvert.toUpdateDto(id, user));
        if (updatedUser == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(updatedUser);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = userService.delete(id);
        if (!deleted) {
            return Result.error(404, "用户不存在");
        }
        return Result.success();
    }

    @Override
    @PreAuthorize("permitAll()")
    public Result<String> health() {
        return Result.success("User service is running");
    }
}

