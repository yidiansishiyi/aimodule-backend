package com.yidiansishiyi.aimodule.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yidiansishiyi.aimodule.common.ErrorCode;
import com.yidiansishiyi.aimodule.common.ResultUtils;
import com.yidiansishiyi.aimodule.exception.BusinessException;
import com.yidiansishiyi.aimodule.exception.ThrowUtils;
import com.yidiansishiyi.aimodule.model.dto.chart.ChartQueryRequest;
import com.yidiansishiyi.aimodule.model.dto.chartRawData.ChartRawDataQueryRequest;
import com.yidiansishiyi.aimodule.model.entity.Chart;
import com.yidiansishiyi.aimodule.model.entity.User;
import com.yidiansishiyi.aimodule.model.vo.ChartOriginalVO;
import com.yidiansishiyi.aimodule.service.ChartRawDataService;
import com.yidiansishiyi.aimodule.service.ChartService;
import com.yidiansishiyi.aimodule.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 图表原始数据接口接口
 * 1. 根据 id 查看可展示图标(前端传递查询列) 用于整页查询和查询部分字段(存在 sql 注入风险需要写工具类过滤传值 对于除查询外的关键字都进行过滤)
 * 2. 传递用户 id 根据id 查询全部图标id 在根据id 拼接查出所有复合的数据,(比较麻烦,可以后期加)
 * 3. 传递字段值,和查询值构建查询条件
 * 4. 修改和删除功能,(不建议直接删除,在图标删除功能中加入,或者写定时任务定期删除图标被删除的表)
 * 5. 修改可以出下载功能,然后根据下载的表格进行修改重新上传
 *
 * @author sanqi
 */
@RestController
@RequestMapping("/chartRawData")
@Slf4j
public class ChartRawDataController {

    @Resource
    private ChartRawDataService chartRawDataService;

    @PostMapping("/getOriginal/id")
    public List<Map<String, Object>> getChartOriginalById(@RequestBody ChartRawDataQueryRequest chartRawDataQueryRequest,HttpServletRequest request) {

        if (chartRawDataQueryRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不合法!");
        }

        List<Map<String, Object>> res = chartRawDataService.getOriginalChartById(chartRawDataQueryRequest,request);

        return res;
    }

}
