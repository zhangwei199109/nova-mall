package com.example.user.service.impl;

import com.example.user.service.UserAppService;
import com.example.user.api.dto.AddressDTO;
import com.example.user.api.dto.UserDTO;
import com.example.user.service.entity.Address;
import com.example.user.service.entity.User;
import com.example.user.service.entity.UserDevice;
import com.example.user.service.entity.UserSession;
import com.example.user.service.mapper.AddressMapper;
import com.example.user.service.mapper.UserMapper;
import com.example.user.service.mapper.UserDeviceMapper;
import com.example.user.service.mapper.UserSessionMapper;
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
    @Autowired
    private UserDeviceMapper userDeviceMapper;
    @Autowired
    private UserSessionMapper userSessionMapper;

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

    @Override
    public UserDTO register(String username, String email, String mobile, String encodedPassword) {
        // 唯一性校验
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        if (mobile != null && existsByMobile(mobile)) {
            throw new IllegalArgumentException("手机号已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setPassword(encodedPassword);
        userMapper.insert(user);
        return toDTO(user);
    }

    @Override
    public UserDTO findByIdentifier(String identifier) {
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, identifier)
                .or(w -> w.eq(User::getEmail, identifier))
                .or(w -> w.eq(User::getMobile, identifier)));
        return user == null ? null : toDTO(user);
    }

    @Override
    public boolean updatePassword(Long userId, String encodedPassword) {
        return userMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getPassword, encodedPassword)) > 0;
    }

    @Override
    @Transactional
    public void recordSession(Long userId, String deviceId, String refreshHash, java.time.Instant expireAt, String ip, String userAgent) {
        // upsert device
        UserDevice device = userDeviceMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserDevice>()
                .eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getDeviceId, deviceId));
        if (device == null) {
            device = new UserDevice();
            device.setUserId(userId);
            device.setDeviceId(deviceId);
            device.setStatus(0);
            device.setUserAgent(userAgent);
            device.setLastIp(ip);
            device.setLastLoginTime(java.time.LocalDateTime.now());
            userDeviceMapper.insert(device);
        } else {
            device.setUserAgent(userAgent);
            device.setLastIp(ip);
            device.setLastLoginTime(java.time.LocalDateTime.now());
            userDeviceMapper.updateById(device);
        }
        // session single entry per refresh hash
        userSessionMapper.insert(buildSession(userId, deviceId, refreshHash, expireAt));
    }

    @Override
    public void deactivateSessionByHash(String refreshHash) {
        userSessionMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<UserSession>()
                .eq(UserSession::getRefreshHash, refreshHash)
                .set(UserSession::getActive, 0));
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getAge(),
                user.getMobile(), user.getAvatar(), user.getGender(), user.getPassword());
    }

    private User toEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        user.setMobile(dto.getMobile());
        user.setAvatar(dto.getAvatar());
        user.setGender(dto.getGender());
        user.setPassword(dto.getPassword());
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

    private boolean existsByUsername(String username) {
        return userMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }

    private boolean existsByEmail(String email) {
        return userMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)) > 0;
    }

    private boolean existsByMobile(String mobile) {
        return userMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getMobile, mobile)) > 0;
    }

    private UserSession buildSession(Long userId, String deviceId, String refreshHash, java.time.Instant expireAt) {
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setDeviceId(deviceId);
        session.setRefreshHash(refreshHash);
        session.setExpireTime(java.time.LocalDateTime.ofInstant(expireAt, java.time.ZoneId.systemDefault()));
        session.setActive(1);
        return session;
    }
}

