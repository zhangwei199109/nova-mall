package com.example.user.service.impl;

import com.example.user.service.UserAppService;
import com.example.user.api.dto.UserDTO;
import com.example.user.service.entity.User;
import com.example.user.service.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserAppService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserDTO> list() {
        return userMapper.selectList(null).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getById(Long id) {
        User user = userMapper.selectById(id);
        return user == null ? null : toDTO(user);
    }

    @Override
    public UserDTO create(UserDTO userDTO) {
        User user = toEntity(userDTO);
        userMapper.insert(user);
        return toDTO(user);
    }

    @Override
    public UserDTO update(Long id, UserDTO userDTO) {
        User existUser = userMapper.selectById(id);
        if (existUser == null) {
            return null;
        }
        User user = toEntity(userDTO);
        user.setId(id);
        userMapper.updateById(user);
        return toDTO(user);
    }

    @Override
    public boolean delete(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getAge());
    }

    private User toEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        return user;
    }
}

