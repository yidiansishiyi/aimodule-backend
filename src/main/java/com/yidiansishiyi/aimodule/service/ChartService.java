package com.yidiansishiyi.aimodule.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yidiansishiyi.aimodule.model.dto.chart.ChartQueryRequest;
import com.yidiansishiyi.aimodule.model.dto.chart.CreateChartExcelDTO;
import com.yidiansishiyi.aimodule.model.dto.chart.GenChartByAiRequest;
import com.yidiansishiyi.aimodule.model.dto.chart.SaveChatDTO;
import com.yidiansishiyi.aimodule.model.entity.Chart;
import com.yidiansishiyi.aimodule.model.vo.BiResponse;
import com.yidiansishiyi.aimodule.model.vo.ChartOriginalVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 *
 */
public interface ChartService extends IService<Chart> {

    /**
     * 校验上传文档
     *
     * @param genChartByAiRequest 智能分析参数
     * @return 是否合法
     */
    Boolean verifyDocument(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest);
    /**
     * 获取用户输入
     *
     * @param genChartByAiRequest 智能分析参数
     * @return 用户输入
     */
    HashMap<String, String> getUserInput(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest);

    String getAiGenerateChart(String userInput);

    String genChartByZelinAi(String userInput);

    BiResponse saveChart(SaveChatDTO saveChatDTO);

    Boolean createChart(CreateChartExcelDTO createChartExcelDTO);

    ChartOriginalVO getOriginalChartById(String id);

    BiResponse genChartByAiAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    void handleChartUpdateError(long chartId, String execMessage);

    BiResponse genChartByAiAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
}
