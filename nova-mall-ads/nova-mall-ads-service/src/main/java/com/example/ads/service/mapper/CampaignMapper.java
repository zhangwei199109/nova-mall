package com.example.ads.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ads.service.entity.Campaign;
import org.apache.ibatis.annotations.Mapper;

/** Campaign 表基础 CRUD Mapper。 */
@Mapper
public interface CampaignMapper extends BaseMapper<Campaign> {
}

