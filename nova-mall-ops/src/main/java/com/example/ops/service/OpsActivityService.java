package com.example.ops.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.exception.BusinessException;
import com.example.ops.dto.ActivityAdminDTO;
import com.example.ops.entity.OpsSeckillActivity;
import com.example.ops.mapper.OpsSeckillActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpsActivityService {

    private final OpsSeckillActivityMapper mapper;

    public List<ActivityAdminDTO> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<OpsSeckillActivity>()
                        .orderByDesc(OpsSeckillActivity::getId))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ActivityAdminDTO> listActive() {
        LocalDateTime now = LocalDateTime.now();
        return mapper.selectList(new LambdaQueryWrapper<OpsSeckillActivity>()
                        .eq(OpsSeckillActivity::getStatus, "ONLINE")
                        .le(OpsSeckillActivity::getStartTime, now)
                        .ge(OpsSeckillActivity::getEndTime, now)
                        .orderByAsc(OpsSeckillActivity::getStartTime))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ActivityAdminDTO create(ActivityAdminDTO dto) {
        OpsSeckillActivity entity = new OpsSeckillActivity();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("OFFLINE");
        }
        mapper.insert(entity);
        return toDTO(mapper.selectById(entity.getId()));
    }

    public ActivityAdminDTO update(ActivityAdminDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "ID不能为空");
        }
        OpsSeckillActivity exist = mapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException(404, "活动不存在");
        }
        BeanUtils.copyProperties(dto, exist);
        mapper.updateById(exist);
        return toDTO(mapper.selectById(dto.getId()));
    }

    public boolean online(Long id) {
        OpsSeckillActivity exist = mapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "活动不存在");
        }
        exist.setStatus("ONLINE");
        return mapper.updateById(exist) > 0;
    }

    public boolean offline(Long id) {
        OpsSeckillActivity exist = mapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "活动不存在");
        }
        exist.setStatus("OFFLINE");
        return mapper.updateById(exist) > 0;
    }

    private ActivityAdminDTO toDTO(OpsSeckillActivity entity) {
        ActivityAdminDTO dto = new ActivityAdminDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}



















