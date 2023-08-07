package com.yidiansishiyi.aimodule.mapper;

import java.util.List;
import java.util.Map;

public interface ChartRawDataMapper {
    List<Map<String, Object>> queryChartData(String querySql);

}
