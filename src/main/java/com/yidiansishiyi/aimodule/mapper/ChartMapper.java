package com.yidiansishiyi.aimodule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yidiansishiyi.aimodule.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.yidiansishiyi.aimodule.model.entity.Chart
 */
public interface ChartMapper extends BaseMapper<Chart> {

    List<Map<String, Object>> queryChartData(String querySql);

    void createChartExelByID(String createSql);

    Boolean insertChartData(String insertSql);

    List<Map> getOriginalChartById(String getOriginalChart);
}




