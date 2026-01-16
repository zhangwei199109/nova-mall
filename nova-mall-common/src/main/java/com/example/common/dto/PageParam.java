package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页入参对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageParam {
    private Integer pageNo;
    private Integer pageSize;

    /**
     * 归一化分页参数，设置默认页码/尺寸并限制最大页大小。
     */
    public PageParam normalized(int defaultPageNo, int defaultPageSize, int maxPageSize) {
        int pn = (pageNo == null || pageNo < 1) ? defaultPageNo : pageNo;
        int ps = (pageSize == null || pageSize < 1) ? defaultPageSize : pageSize;
        if (ps > maxPageSize) {
            ps = maxPageSize;
        }
        return new PageParam(pn, ps);
    }
}






















