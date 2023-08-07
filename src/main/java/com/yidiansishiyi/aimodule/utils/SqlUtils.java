package com.yidiansishiyi.aimodule.utils;

import com.yidiansishiyi.aimodule.common.ErrorCode;
import com.yidiansishiyi.aimodule.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * SQL 工具
 *
 * @author sanqi
 *   
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
    /**
     * 校验传入表都字段是否合法（防止 SQL 注入）
     *
     * @param meterHeader 查询列
     * @return
     */
    public static String validMeterHeaderField(String meterHeader) {
        StringBuilder headers = new StringBuilder();
        String[] split = StringUtils.split(meterHeader,",");
        for (int i = 0; i < split.length; i++) {
            if (StringUtils.containsAny(split[i], "=", "(", ")", " ")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不合法,检查列名是否合法");
            }
            if (i == 0){
                headers.append("`").append(split[i]).append("`");
            }
            headers.append(",").append("`").append(split[i]).append("`");
        }

        return headers.toString();
    }
}
