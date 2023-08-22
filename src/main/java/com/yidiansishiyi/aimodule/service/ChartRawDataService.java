package com.yidiansishiyi.aimodule.service;

import com.yidiansishiyi.aimodule.model.dto.chartRawData.ChartRawDataQueryRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface ChartRawDataService {
    List<Map<String, Object>> getOriginalChartById(ChartRawDataQueryRequest chartRawDataQueryRequest, HttpServletRequest request);

}
