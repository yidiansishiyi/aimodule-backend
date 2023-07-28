package com.yidiansishiyi.aimodule.model.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveChatDTO {
    /**
     * 图标请求参数
     */
    GenChartByAiRequest genChartByAiRequest;
    /**
     * ai 返回消息
     */
    String result;
    /**
     * 用户输入
     */
    HashMap<String, String> userInputs;
    /**
     * 用户请求
     */
    HttpServletRequest request;
}
