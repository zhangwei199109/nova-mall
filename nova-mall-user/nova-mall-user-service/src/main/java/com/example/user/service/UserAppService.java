package com.example.user.service;

import com.example.user.api.dto.UserDTO;

import java.util.List;

public interface UserAppService {

    List<UserDTO> list();

    UserDTO getById(Long id);

    UserDTO create(UserDTO dto);

    UserDTO update(Long id, UserDTO dto);

    boolean delete(Long id);
}










