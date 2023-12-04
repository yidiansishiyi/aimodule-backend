package com.yidiansishiyi.aimodule.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yidiansishiyi.aimodule.mapper.WmsensitiveMapper;
import com.yidiansishiyi.aimodule.model.entity.Wmsensitive;
import com.yidiansishiyi.aimodule.service.WmsensitiveService;
import com.yidiansishiyi.aimodule.utils.SensitiveWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean checkSensitiveWords(String args) {
        boolean flag = true;
        Map<String, Object> dictionaryMap = SensitiveWordUtil.getDictionaryMap();
        RLock lock = redissonClient.getLock("aimodule:job:IncSyncSensitiveToMap:lock");
        if (dictionaryMap == null || dictionaryMap.size() == 0) {
            try {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    if (dictionaryMap == null || dictionaryMap.size() == 0){
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
                } else {
                    log.info("未获取到锁，跳过本次执行");
                }
            } catch (InterruptedException e) {
                log.error("枪锁错误", e);
            }
        }
        // 查看文本是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(args);
        if (map.size() > 0) {
            flag = false;
        }

        return flag;
    }

}




