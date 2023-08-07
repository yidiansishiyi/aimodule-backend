package com.yidiansishiyi.aimodule.model.dto.chartRawData;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @author sanqi
 *   
 */
@Data
public class ChartRawDataQueryRequest implements Serializable {

    /**
     * 图表 id
     */
    private Long id;

    /**
     * 表头
     */
    private String meterHeader;

    /**
     * 排序字段
     */
    private String sortField;


    private static final long serialVersionUID = 1L;
}