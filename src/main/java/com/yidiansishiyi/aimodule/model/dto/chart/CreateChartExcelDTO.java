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
     * 图表ID
     */
    private Long chartId;

    public CreateChartExcelDTO(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }
}
