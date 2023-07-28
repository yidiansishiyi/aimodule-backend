package com.yidiansishiyi.aimodule.model.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChartExcelDTO {

    /**
     * 上传文件
     */
    private MultipartFile multipartFile;

    /**
     * 请求
     */
    private HttpServletRequest request;

    /**
     * 图表ID
     */
    private Long chartId;

    public CreateChartExcelDTO(MultipartFile multipartFile, HttpServletRequest request) {
        this.multipartFile = multipartFile;
        this.request = request;
    }
}
