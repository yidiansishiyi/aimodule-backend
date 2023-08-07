package com.yidiansishiyi.aimodule.mapper;

import org.apache.ibatis.jdbc.SQL;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChartRawDataMapperTest {

    @Resource
    ChartRawDataMapper chartRawDataMapper;

    @Test
    void testChartRawDataMapper(){

        SQL sql = new SQL();
        sql.SELECT("*");
        sql.FROM("chart_" + "1674388862987681794");
        String querySql = sql.toString();

        List<Map<String, Object>> stringListMap = chartRawDataMapper.queryChartData(querySql);
        System.out.println(stringListMap.toString());
    }
}