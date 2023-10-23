package com.yidiansishiyi.aimodule.job.cycle;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yidiansishiyi.aimodule.mapper.WmsensitiveMapper;
import com.yidiansishiyi.aimodule.model.entity.Wmsensitive;
import com.yidiansishiyi.aimodule.utils.SensitiveWordUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class IncSyncSensitiveToMap {

    @Value("${scheduled.cycle.wmsensitive:false}")
    private Boolean flag;

    @Resource
    private WmsensitiveMapper wmsensitiveMapper;

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(initialDelay = 1000, fixedRate = 60 * 1000 * 60 * 3)
    public void run(){
        RLock lock = redissonClient.getLock("aimodule:job:IncSyncSensitiveToMap:lock");
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                List<Wmsensitive> wmSensitives = wmsensitiveMapper.selectList(Wrappers.<Wmsensitive>lambdaQuery()
                        .select(Wmsensitive::getSensitives)
                        .isNotNull(Wmsensitive::getSensitives)
                        .groupBy(Wmsensitive::getSensitives));
                List<String> sensitiveList = wmSensitives.stream().map(Wmsensitive::getSensitives).collect(Collectors.toList());

                // 初始化敏感词库
                SensitiveWordUtil.initMap(sensitiveList);

                log.info("同步了 {} 条敏感词", sensitiveList.size());
            }
        } catch (Exception e) {
            log.error("IncSyncSensitiveToMap ", e);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
