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
}






























