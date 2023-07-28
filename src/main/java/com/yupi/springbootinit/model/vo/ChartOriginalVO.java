package com.yupi.springbootinit.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartOriginalVO {
    /**
     * 图标原始数据
     */
    private List<Map> chartOriginal;

    /**
     * 图表大小
     */
    private Integer size;

    /**
     * 图表标题
     */
    private String meterHeader;
}
