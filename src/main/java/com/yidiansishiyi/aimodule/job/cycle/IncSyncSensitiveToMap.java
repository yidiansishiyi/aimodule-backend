package com.yidiansishiyi.aimodule.job.cycle;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yidiansishiyi.aimodule.mapper.WmsensitiveMapper;
import com.yidiansishiyi.aimodule.model.entity.Wmsensitive;
import com.yidiansishiyi.aimodule.utils.SensitiveWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class IncSyncSensitiveToMap {

    @Resource
    private WmsensitiveMapper wmsensitiveMapper;

    @Scheduled(initialDelay = 1000, fixedRate = 60 * 1000 * 60 * 3)
    public void run() {
        List<Wmsensitive> wmSensitives = wmsensitiveMapper.selectList(Wrappers.<Wmsensitive>lambdaQuery().select(Wmsensitive::getSensitives).isNotNull(Wmsensitive::getSensitives).groupBy(Wmsensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(Wmsensitive::getSensitives).collect(Collectors.toList());

        // 初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);

        log.info("同步了 {} 条敏感词", sensitiveList.size());
    }
}
