package com.example.user.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.user.service.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}



