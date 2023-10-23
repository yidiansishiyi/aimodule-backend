package com.yidiansishiyi.aimodule.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yidiansishiyi.aimodule.annotation.AuthCheck;
import com.yidiansishiyi.aimodule.annotation.RateLimit;
import com.yidiansishiyi.aimodule.common.BaseResponse;
import com.yidiansishiyi.aimodule.common.DeleteRequest;
import com.yidiansishiyi.aimodule.common.ErrorCode;
import com.yidiansishiyi.aimodule.common.ResultUtils;
import com.yidiansishiyi.aimodule.constant.UserConstant;
import com.yidiansishiyi.aimodule.exception.BusinessException;
import com.yidiansishiyi.aimodule.exception.ThrowUtils;
import com.yidiansishiyi.aimodule.mapper.ChartMapper;
import com.yidiansishiyi.aimodule.model.dto.chart.*;
import com.yidiansishiyi.aimodule.model.entity.Chart;
import com.yidiansishiyi.aimodule.model.entity.User;
import com.yidiansishiyi.aimodule.model.vo.BiResponse;
import com.yidiansishiyi.aimodule.model.vo.ChartOriginalVO;
import com.yidiansishiyi.aimodule.service.ChartService;
import com.yidiansishiyi.aimodule.service.UserService;
import com.yidiansishiyi.aimodule.service.WmsensitiveService;
import com.yidiansishiyi.aimodule.utils.ExcelUtils;
import jodd.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 图表接口
 *
 * @author sanqi
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private WmsensitiveService wmsensitiveService;

    @Resource
    private RedissonClient redissonClient;


    private Date initialTime = new Date();

    @Profile({"dev", "local"})
    @PostMapping("/ceses")
    public boolean ceses(String ceses) {
        return wmsensitiveService.checkSensitiveWords(ceses);
    }

    @Profile({"dev", "local"})
    @PostMapping("/initialTime")
    public String initialTime() {
        RMap<Object, Date> rMap = redissonClient.getMap("aimodule:job:operationTime");
        rMap.put("startTime",initialTime);
        rMap.put("endTime",initialTime);
        return rMap.get("startTime").toString();
    }

    @Profile({"dev", "local"})
    @PostMapping("/toCSV")
    public boolean toCSV(@RequestPart("file") MultipartFile multipartFile) throws IOException {
        String s = ExcelUtils.excelToCsv(multipartFile);
        FileUtil.writeString(new File("D:\\files\\creatSql.txt"), s);
        String s1 = FileUtil.readString(new File("D:\\files\\creatSql.txt"));
        System.out.println(s1);
        return true;
    }

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @PostMapping("/insertChat")
    public boolean insertChat() throws IOException {
        List<List<Chart>> futures = new ArrayList<>();
        StopWatch totalDuration = new StopWatch();
        StopWatch buildInputDuration = new StopWatch();
        totalDuration.start();

        ArrayList<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        buildInputDuration.start();
        for (int i = 0; i < 20; i++) {
            List<Chart> charts = new LinkedList<>();
            for (int j = 0; j < 50000; j++) {
                Chart chart = Chart.builder()
                        .goal("分析店铺内商品" + j)
                        .name("商品分析" + j)
                        .chartType("折线图")
                        .status("succeed")
                        .userId(1652457118348935170L)
                        .build();
                charts.add(chart);
            }
            log.info("构建数据时长: " + buildInputDuration.getTime());
            buildInputDuration.suspend();
            buildInputDuration.resume();
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                try {
                    System.out.println("threadName: " + Thread.currentThread().getName());
                    StopWatch singleBatchInsertionTime = new StopWatch();
                    singleBatchInsertionTime.start();
                    chartService.saveBatch(charts, 500);
                    singleBatchInsertionTime.stop();
                    log.info("单批次插入时长: " + singleBatchInsertionTime.getTime());
                } catch (Exception e) {
                    // 处理异常，可以记录到日志文件中
                    log.error("插入数据发生异常: " + e.getMessage());
                }
            }, threadPoolExecutor);
           completableFutures.add(voidCompletableFuture);
        }

        try {
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{})).join();
        } finally {
            // 关闭线程池
            threadPoolExecutor.shutdown();
        }

        totalDuration.stop();
        log.info("插入 数据总时长: " + totalDuration.getTime());
        return true;
    }


    // region 增删改查
    @Profile({"dev", "local"})
    @PostMapping("/zero")
    public Boolean zero(@RequestPart("file") MultipartFile multipartFile,
                        GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        CreateChartExcelDTO createChartExcelDTO = new CreateChartExcelDTO(multipartFile);
        return chartService.createChart(createChartExcelDTO);
    }

    @GetMapping("/getOriginal/{id}")
    public ChartOriginalVO getChartOriginalById(@PathVariable String id) {
        ChartOriginalVO originalChartById = chartService.getOriginalChartById(id);
        return originalChartById;
    }


    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @RateLimit(key = "addChart")
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @Validated 淡出放这
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @RateLimit(key = "genChartByAi")
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(!chartService.verifyDocument(multipartFile, genChartByAiRequest),
                ErrorCode.PARAMS_ERROR, "存在敏感字");

        // 构造用户输入
        HashMap<String, String> userInputs = chartService.getUserInput(multipartFile, genChartByAiRequest);

        String result = chartService.getAiGenerateChart(userInputs.get("userInput"));
        SaveChatDTO saveChatDTO = new SaveChatDTO(genChartByAiRequest, result, userInputs, request);
        BiResponse biResponse = chartService.saveChart(saveChatDTO);
        CreateChartExcelDTO createChartExcelDTO = new CreateChartExcelDTO(multipartFile, biResponse.getChartId());
        chartService.createChart(createChartExcelDTO);
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @RateLimit(key = "genChartByAi")
    @PostMapping("/genChartByZelinAi")
    public BaseResponse<BiResponse> genChartByZelinAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(!chartService.verifyDocument(multipartFile, genChartByAiRequest),
                ErrorCode.PARAMS_ERROR, "存在敏感字");
        // 构造用户输入
        HashMap<String, String> userInputs = chartService.getUserInput(multipartFile, genChartByAiRequest);

        String result = chartService.genChartByZelinAi(userInputs.get("userInput"));
        SaveChatDTO saveChatDTO = new SaveChatDTO(genChartByAiRequest, result, userInputs, request);
        BiResponse biResponse = chartService.saveChart(saveChatDTO);
        CreateChartExcelDTO createChartExcelDTO = new CreateChartExcelDTO(multipartFile, biResponse.getChartId());
        chartService.createChart(createChartExcelDTO);
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @RateLimit(key = "genChartByAi")
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(!chartService.verifyDocument(multipartFile, genChartByAiRequest),
                ErrorCode.PARAMS_ERROR, "存在敏感字");
        BiResponse biResponse = chartService.genChartByAiAsync(multipartFile, genChartByAiRequest, request);
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @RateLimit(key = "genChartByAi")
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        ThrowUtils.throwIf(!chartService.verifyDocument(multipartFile, genChartByAiRequest),
                ErrorCode.PARAMS_ERROR, "存在敏感字");

        BiResponse biResponse = chartService.genChartByAiAsyncMq(multipartFile, genChartByAiRequest,request);
        return ResultUtils.success(biResponse);
    }

}
