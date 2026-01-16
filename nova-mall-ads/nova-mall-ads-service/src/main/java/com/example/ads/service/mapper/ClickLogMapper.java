package com.example.ads.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ads.service.entity.ClickLog;
import org.apache.ibatis.annotations.Mapper;

/** 点击埋点表 Mapper。 */
@Mapper
public interface ClickLogMapper extends BaseMapper<ClickLog> {
}

