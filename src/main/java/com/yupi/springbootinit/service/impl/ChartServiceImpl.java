package com.yupi.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.dto.chart.CreateChartExcelDTO;
import com.yupi.springbootinit.model.dto.chart.GenChartByAiRequest;
import com.yupi.springbootinit.model.dto.chart.SaveChatDTO;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.model.vo.ChartOriginalVO;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.service.WmsensitiveService;
import com.yupi.springbootinit.utils.DataCleaningUtils;
import com.yupi.springbootinit.utils.ExcelUtils;
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
    public BiResponse saveChart(SaveChatDTO saveChatDTO) {
        GenChartByAiRequest genChartByAiRequest = saveChatDTO.getGenChartByAiRequest();
        String result = saveChatDTO.getResult();
        Map<String, String> userInputs = saveChatDTO.getUserInputs();
        HttpServletRequest request = saveChatDTO.getRequest();
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        String[] splits = result.split("【【【【【");
        String genChart = splits[1].trim();
        String genChartNew = DataCleaningUtils.extractJsonPart(genChart);
        String genResult = splits[2].trim();

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(userInputs.get("csvData"));
        chart.setChartType(chartType);
        chart.setGenChart(genChartNew);
        chart.setGenResult(genResult);
        chart.setStatus("succeed");
        String join = String.join(",", ExcelUtils.getHeaderList());
        chart.setMeterHeader(join);
        chart.setUserId(userService.getLoginUser(request).getId());
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    @Override
    @Transactional
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

}



