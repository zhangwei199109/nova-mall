package com.example.user.service.impl;

import com.example.user.service.UserAppService;
import com.example.user.api.dto.UserDTO;
import com.example.user.api.dto.AddressDTO;
import com.example.user.service.entity.Address;
import com.example.user.service.entity.User;
import com.example.user.service.mapper.AddressMapper;
import com.example.user.service.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserAppService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressMapper addressMapper;

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

    @Override
    public List<AddressDTO> listAddress(Long userId) {
        return addressMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .eq(Address::getDeleted, 0)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getId))
                .stream().map(this::toAddressDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDTO createAddress(Long userId, AddressDTO dto) {
        dto.setUserId(userId);
        Address entity = toAddressEntity(dto);
        if (entity.getIsDefault() != null && entity.getIsDefault() == 1) {
            clearDefault(userId);
        }
        addressMapper.insert(entity);
        return toAddressDTO(addressMapper.selectById(entity.getId()));
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, AddressDTO dto) {
        Address exist = addressMapper.selectById(addressId);
        if (exist == null || !exist.getUserId().equals(userId)) {
            return null;
        }
        dto.setUserId(userId);
        Address entity = toAddressEntity(dto);
        entity.setId(addressId);
        if (entity.getIsDefault() != null && entity.getIsDefault() == 1) {
            clearDefault(userId);
        }
        addressMapper.updateById(entity);
        return toAddressDTO(addressMapper.selectById(addressId));
    }

    @Override
    public boolean deleteAddress(Long userId, Long addressId) {
        return addressMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Address>()
                .eq(Address::getId, addressId)
                .eq(Address::getUserId, userId)) > 0;
    }

    @Override
    @Transactional
    public boolean setDefaultAddress(Long userId, Long addressId) {
        Address exist = addressMapper.selectById(addressId);
        if (exist == null || !exist.getUserId().equals(userId)) {
            return false;
        }
        clearDefault(userId);
        addressMapper.updateById(updateDefault(exist, 1));
        return true;
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

    private AddressDTO toAddressDTO(Address entity) {
        AddressDTO dto = new AddressDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setReceiverName(entity.getReceiverName());
        dto.setMobile(entity.getMobile());
        dto.setProvince(entity.getProvince());
        dto.setCity(entity.getCity());
        dto.setDistrict(entity.getDistrict());
        dto.setDetail(entity.getDetail());
        dto.setZipCode(entity.getZipCode());
        dto.setIsDefault(entity.getIsDefault());
        return dto;
    }

    private Address toAddressEntity(AddressDTO dto) {
        Address entity = new Address();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setReceiverName(dto.getReceiverName());
        entity.setMobile(dto.getMobile());
        entity.setProvince(dto.getProvince());
        entity.setCity(dto.getCity());
        entity.setDistrict(dto.getDistrict());
        entity.setDetail(dto.getDetail());
        entity.setZipCode(dto.getZipCode());
        entity.setIsDefault(dto.getIsDefault() == null ? 0 : dto.getIsDefault());
        return entity;
    }

    private void clearDefault(Long userId) {
        addressMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0));
    }

    private Address updateDefault(Address addr, int flag) {
        addr.setIsDefault(flag);
        return addr;
    }
}

