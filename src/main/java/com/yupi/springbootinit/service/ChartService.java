package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.chart.CreateChartExcelDTO;
import com.yupi.springbootinit.model.dto.chart.GenChartByAiRequest;
import com.yupi.springbootinit.model.dto.chart.SaveChatDTO;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.model.vo.ChartOriginalVO;
import org.springframework.web.multipart.MultipartFile;

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

    BiResponse saveChart(SaveChatDTO saveChatDTO);

    Boolean createChart(CreateChartExcelDTO createChartExcelDTO);

    ChartOriginalVO getOriginalChartById(String id);
}
