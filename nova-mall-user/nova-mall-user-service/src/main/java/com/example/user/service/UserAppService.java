package com.example.user.service;

import com.example.user.api.dto.UserDTO;

import java.util.List;

public interface UserAppService {

    List<UserDTO> list();

    UserDTO getById(Long id);

    UserDTO create(UserDTO dto);

    UserDTO update(Long id, UserDTO dto);

    boolean delete(Long id);

    java.util.List<com.example.user.api.dto.AddressDTO> listAddress(Long userId);

    com.example.user.api.dto.AddressDTO createAddress(Long userId, com.example.user.api.dto.AddressDTO dto);

    com.example.user.api.dto.AddressDTO updateAddress(Long userId, Long addressId, com.example.user.api.dto.AddressDTO dto);

    boolean deleteAddress(Long userId, Long addressId);

    boolean setDefaultAddress(Long userId, Long addressId);

    /**
     * 注册用户，已做唯一性校验。
     */
    com.example.user.api.dto.UserDTO register(String username, String email, String mobile, String encodedPassword);

    /**
     * 通过用户名/邮箱/手机号查询用户。
     */
    com.example.user.api.dto.UserDTO findByIdentifier(String identifier);

    /**
     * 更新用户密码（BCrypt 已编码）。
     */
    boolean updatePassword(Long userId, String encodedPassword);

    void recordSession(Long userId, String deviceId, String refreshHash, java.time.Instant expireAt, String ip, String userAgent);

    void deactivateSessionByHash(String refreshHash);
}






























