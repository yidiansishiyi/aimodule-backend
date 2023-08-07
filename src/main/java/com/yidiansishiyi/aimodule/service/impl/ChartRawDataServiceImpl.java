package com.yidiansishiyi.aimodule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yidiansishiyi.aimodule.common.ErrorCode;
import com.yidiansishiyi.aimodule.exception.BusinessException;
import com.yidiansishiyi.aimodule.mapper.ChartRawDataMapper;
import com.yidiansishiyi.aimodule.model.dto.chartRawData.ChartRawDataQueryRequest;
import com.yidiansishiyi.aimodule.model.entity.Chart;
import com.yidiansishiyi.aimodule.model.entity.User;
import com.yidiansishiyi.aimodule.service.ChartRawDataService;
import com.yidiansishiyi.aimodule.service.ChartService;
import com.yidiansishiyi.aimodule.service.UserService;
import com.yidiansishiyi.aimodule.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChartRawDataServiceImpl implements ChartRawDataService {
    @Resource
    private ChartRawDataMapper chartRawDataMapper;

    @Resource
    private UserService userService;

    @Resource
    private ChartService chartService;


    @Override
    public List<Map<String, Object>> getOriginalChartById(ChartRawDataQueryRequest chartRawDataQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        QueryWrapper<Chart> wrapper = new QueryWrapper<>();
        wrapper.eq("id",chartRawDataQueryRequest.getId());
        wrapper.eq("userId", loginUser.getId());
        long count = chartService.count(wrapper);

        if (count != 1){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请求不合法,或请求原始数据不存在!");
        }

        Long id = chartRawDataQueryRequest.getId();
        String sortField = chartRawDataQueryRequest.getSortField();
        String meterHeader = chartRawDataQueryRequest.getMeterHeader();
        String newHeaders = SqlUtils.validMeterHeaderField(meterHeader);
        SQL sql = new SQL();
        sql.SELECT(newHeaders);
        sql.FROM("chart_" + id);
        if (SqlUtils.validSortField(sortField)) {
            sql.ORDER_BY(sortField);
        }
        String querySql = sql.toString();
        List<Map<String, Object>> stringListMap = null;
        try {
            stringListMap = chartRawDataMapper.queryChartData(querySql);
        } catch (Exception e) {
            log.error("查询失败: id" + chartRawDataQueryRequest.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据查询失败!");
        }
        return stringListMap;
    }
}
