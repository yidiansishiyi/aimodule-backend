package com.yidiansishiyi.aimodule.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yidiansishiyi.aimodule.bizmq.BiMessageProducer;
import com.yidiansishiyi.aimodule.common.ErrorCode;
import com.yidiansishiyi.aimodule.exception.BusinessException;
import com.yidiansishiyi.aimodule.exception.ThrowUtils;
import com.yidiansishiyi.aimodule.manager.AiManager;
import com.yidiansishiyi.aimodule.mapper.ChartMapper;
import com.yidiansishiyi.aimodule.model.dto.chart.ChartQueryRequest;
import com.yidiansishiyi.aimodule.model.dto.chart.CreateChartExcelDTO;
import com.yidiansishiyi.aimodule.model.dto.chart.GenChartByAiRequest;
import com.yidiansishiyi.aimodule.model.dto.chart.SaveChatDTO;
import com.yidiansishiyi.aimodule.model.entity.Chart;
import com.yidiansishiyi.aimodule.model.entity.User;
import com.yidiansishiyi.aimodule.model.vo.BiResponse;
import com.yidiansishiyi.aimodule.model.vo.ChartOriginalVO;
import com.yidiansishiyi.aimodule.service.ChartService;
import com.yidiansishiyi.aimodule.service.UserService;
import com.yidiansishiyi.aimodule.service.WmsensitiveService;
import com.yidiansishiyi.aimodule.utils.DataCleaningUtils;
import com.yidiansishiyi.aimodule.utils.ExcelUtils;
import com.yidiansishiyi.aimodule.constant.CommonConstant;
import com.yidiansishiyi.aimodule.utils.SqlUtils;
import com.yidiansishiyi.zelinaiclientsdk.model.ZelinAIRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    @Resource
    private AiManager aiManager;

    @Resource
    private UserService userService;

    @Resource
    private ChartMapper chartMapper;

    @Resource
    private BiMessageProducer biMessageProducer;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private WmsensitiveService wmsensitiveService;

    @Override
    public Boolean verifyDocument(MultipartFile multipartFile,
                                  GenChartByAiRequest genChartByAiRequest) {
        // todo 可以加入敏感字校验,引入第三方 api 进行更细力度的校验
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100,
                ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 a.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        // 校验文件是否有敏感词
        StringBuilder stringBuilder = new StringBuilder();
        String userInput = ExcelUtils.excelToCsv(multipartFile);
        stringBuilder.append(name).append(goal).append(userInput);
        String userInputStr = StrUtil.toStringOrNull(stringBuilder);
        return wmsensitiveService.checkSensitiveWords(userInputStr);
    }

    @Override
    public HashMap<String, String> getUserInput(MultipartFile multipartFile,
                                                GenChartByAiRequest genChartByAiRequest) {
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        HashMap<String, String> userInputs = new HashMap();
        userInputs.put("userInput", CharSequenceUtil.str(userInput));
        userInputs.put("csvData", csvData);

        return userInputs;
    }

    @Override
    public String getAiGenerateChart(String userInput) {
        // todo 可以做一个注册表,维护不同 ai 来源 即使用 yucongming 也可以配置文件传入 模型 id
        long biModelId = CommonConstant.BI_MODEL_ID;
        // ai 调用 及数据清洗

        String result = aiManager.doChat(biModelId, userInput);
        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        return result;
    }

    @Override
    public String genChartByZelinAi(String userInput) {
        long biModelId = CommonConstant.BI_MODEL_ID;

        String userInputNew = userInput.replaceAll("\\n", " ");
//        // zelinai 接口限制 \n 为铭感字
//        StringBuffer buffer = new [+[](userInput);
//        String userInputZelinAi = "";
//        for (int i = 0; i < buffer.length(); i++) {
//            if (i == buffer.length()-2){
//                break;
//            }
//            char c = buffer.charAt(i);
//            char z = buffer.charAt(i + 1);
//            if (c == '\n') {
//                userInputZelinAi += ' ';
//            }else {
//                userInputZelinAi += c;
//            }
//        }
//        userInput.replace("\\n"," ");
        // ai 调用 及数据清洗
        ZelinAIRequest zelinAIRequest = new ZelinAIRequest();
        zelinAIRequest.setApp_id("nBoA5U7hJtQzqNMwLfLJTi");
        zelinAIRequest.setRequest_id("124224");
        zelinAIRequest.setUid("152525");
        zelinAIRequest.setContent(userInputNew);

        String result = aiManager.doChat(zelinAIRequest);
        String[] splits = result.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        return result;
    }

    @Override
    public BiResponse saveChart(SaveChatDTO saveChatDTO) {
        GenChartByAiRequest genChartByAiRequest = saveChatDTO.getGenChartByAiRequest();
        String result = saveChatDTO.getResult();
        Map<String, String> userInputs = saveChatDTO.getUserInputs();
        HttpServletRequest request = saveChatDTO.getRequest();
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setStatus("succeed");
        String join = String.join(",", ExcelUtils.getHeaderList());
        chart.setMeterHeader(join);
        String genChartNew = "";
        String genResult = "";
        if (StringUtils.isNotBlank(result)){
            String[] splits = result.split("【【【【【");
            String genChart = splits[1].trim();
            genChartNew = DataCleaningUtils.extractJsonPart(genChart);
            genResult = splits[2].trim();
            chart.setGenChart(genChartNew);
            chart.setGenResult(genResult);
        }
        chart.setUserId(userService.getLoginUser(request).getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChartNew);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createChart(CreateChartExcelDTO createChartExcelDTO) {

        // 读取数据
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(createChartExcelDTO.getMultipartFile().getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误", e);
        }
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        // 读取表头
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
        List<String> headerList = headerMap.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());

        StringBuilder createSql = new StringBuilder();
        createSql.append("CREATE TABLE ");
        String createName = String.format("chart_%s", createChartExcelDTO.getChartId() + " ");
        createSql.append(createName);
        String format = "VARCHAR(255) NULL DEFAULT NULL";


        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id BIGINT(20) AUTO_INCREMENT,");
        for (String headerName : headerList) {
            stringBuilder.append(headerName).append(" ").append(format).append(",");
        }
        stringBuilder.append("isDelete tinyint default 0 not null comment '是否删除',");
        stringBuilder.append("PRIMARY KEY (`id`)");

        createSql.append(" (");
        createSql.append(stringBuilder);
        createSql.append(");");

        String sql = createSql.toString();
        Boolean aBoolean = new Boolean(true);

        try {
            chartMapper.createChartExelByID(sql);
        } catch (Exception e) {
            aBoolean = false;
            throw new RuntimeException(e);
        }

        if (aBoolean) {
            // 读取数据
            List<String> dataLines = new ArrayList<>();
            for (int i = 1; i < list.size(); i++) {
                LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
                List<String> dataList = dataMap.values().stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
                dataLines.add(StringUtils.join(dataList, ","));
            }

            // 插入数据
            String insertSql = String.format("INSERT INTO %s ", createName);
            StringBuilder columnNames = new StringBuilder();
            StringBuilder insertValues = new StringBuilder();
            boolean isFirstColumn = true;

            for (String columnName : headerList) {
                if (isFirstColumn) {
                    columnNames.append(columnName);
                    isFirstColumn = false;
                } else {
                    columnNames.append(", ").append(columnName);
                }
            }

            insertValues.append(" VALUES ");

            for (String line : dataLines) {
                StringBuilder values = new StringBuilder();
                String[] valuesArray = line.split(",");
                for (String value : valuesArray) {
                    values.append("'").append(value).append("',");
                }
                values.deleteCharAt(values.length() - 1);
                insertValues.append("(").append(values).append("),");
            }

            insertValues.deleteCharAt(insertValues.length() - 1);
            insertSql += "(" + columnNames + ")" + insertValues;

            Boolean chartDataInserted = chartMapper.insertChartData(insertSql);
            return chartDataInserted;
        }

        return false;
    }

    @Override
    public ChartOriginalVO getOriginalChartById(String id) {
        LambdaQueryWrapper<Chart> qwp = Wrappers.lambdaQuery();
        qwp.select(Chart::getMeterHeader).eq(Chart::getId, id).isNotNull(Chart::getMeterHeader);
        String meterHeader = this.getOne(qwp).getMeterHeader();
        SQL sql = new SQL();
        sql.SELECT(meterHeader);
        sql.FROM("chart_" + id);
        String getOriginalChart = sql.toString();
        List<Map> chartOriginal = chartMapper.getOriginalChartById(getOriginalChart);

        ThrowUtils.throwIf(ObjectUtils.isEmpty(chartOriginal), ErrorCode.NOT_FOUND_ERROR, "数据错误查找内容为空");
        
        return new ChartOriginalVO(chartOriginal, chartOriginal.size(), meterHeader);
    }

    @Override
    public BiResponse genChartByAiAsync(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        HashMap<String, String> userInputs = this.getUserInput(multipartFile, genChartByAiRequest);
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(genChartByAiRequest.getName());
        chart.setGoal(genChartByAiRequest.getGoal());
        chart.setChartType(genChartByAiRequest.getChartType());
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        // todo 建议处理任务队列满了后，抛异常的情况
        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = this.updateById(updateChart);
            if (!b) {
                handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
                return;
            }
            // 调用 AI
            String result = getAiGenerateChart(userInputs.get("userInput"));

            Chart updateChartResult = new Chart();
            String genResult = "",genChartNew = "";
            if (StringUtils.isNotBlank(result)){
                String[] splits = result.split("【【【【【");
                String genChart = splits[1].trim();
                genChartNew = DataCleaningUtils.extractJsonPart(genChart);
                genResult = splits[2].trim();
                chart.setGenChart(genChartNew);
                chart.setGenResult(genResult);
            }
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChartNew);
            updateChartResult.setGenResult(genResult);
            // todo 建议定义状态为枚举值
            updateChartResult.setStatus("succeed");
            boolean updateResult = this.updateById(updateChartResult);
            if (!updateResult) {
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }
        }, threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        
        return biResponse;
    }

    @Override
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage("execMessage");
        boolean updateResult = this.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }

    @Override
    public BiResponse genChartByAiAsyncMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        // 构造用户输入
        HashMap<String, String> userInputs = this.getUserInput(multipartFile, genChartByAiRequest);

        User loginUser = userService.getLoginUser(request);
        String csvData = userInputs.get("csvData");
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        long newChartId = chart.getId();
        biMessageProducer.sendMessage(String.valueOf(newChartId));
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(newChartId);
        return biResponse;
    }

    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

}



