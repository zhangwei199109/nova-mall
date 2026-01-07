package com.example.user.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.user.service.entity.UserSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSessionMapper extends BaseMapper<UserSession> {
}

