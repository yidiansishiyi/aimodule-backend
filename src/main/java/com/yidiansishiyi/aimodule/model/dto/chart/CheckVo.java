package com.yidiansishiyi.aimodule.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * "需要审核的文字"
     */
    private String text;
}