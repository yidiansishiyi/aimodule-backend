package com.yidiansishiyi.aimodule.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yidiansishiyi.aimodule.mapper.WmsensitiveMapper;
import com.yidiansishiyi.aimodule.model.entity.Wmsensitive;
import com.yidiansishiyi.aimodule.service.WmsensitiveService;
import com.yidiansishiyi.aimodule.utils.SensitiveWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zeroc
 * @description 针对表【wmSensitive(铭感词表)】的数据库操作Service实现
 * @createDate 2023-06-28 14:58:53
 */
@Slf4j
@Service
public class WmsensitiveServiceImpl extends ServiceImpl<WmsensitiveMapper, Wmsensitive> implements WmsensitiveService {

    @Resource
    private WmsensitiveMapper wmsensitiveMapper;

    @Override
    public boolean checkSensitiveWords(String args) {
        boolean flag = true;
        Map<String, Object> dictionaryMap = SensitiveWordUtil.getDictionaryMap();
        if (dictionaryMap == null || dictionaryMap.size() == 0) {
            synchronized (SensitiveWordUtil.class) {
                if (dictionaryMap == null || dictionaryMap.size() == 0) {
                    List<Wmsensitive> wmSensitives =
                            wmsensitiveMapper.selectList(Wrappers.<Wmsensitive>lambdaQuery()
                                    .select(Wmsensitive::getSensitives)
                                    .isNotNull(Wmsensitive::getSensitives));
                    List<String> sensitiveList = wmSensitives.stream()
                            .map(Wmsensitive::getSensitives)
                            .collect(Collectors.toList());
                    // 初始化敏感词库
                    SensitiveWordUtil.initMap(sensitiveList);
                }
            }
            // 查看文本是否包含敏感词
            Map<String, Integer> map = SensitiveWordUtil.matchWords(args);
            if (map.size() > 0) {
                flag = false;
            }
        }
        return flag;
    }

}




