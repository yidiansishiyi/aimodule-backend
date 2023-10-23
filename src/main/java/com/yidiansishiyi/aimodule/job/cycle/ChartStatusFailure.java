package com.yidiansishiyi.aimodule.job.cycle;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yidiansishiyi.aimodule.model.entity.Chart;
import com.yidiansishiyi.aimodule.mapper.ChartMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ChartStatusFailure {
    @Resource
    private ChartMapper chartMapper;

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void chatrun() {
        log.info("开始补偿机制");
        RLock lock = redissonClient.getLock("aimodule:job:IncSyncSensitiveToMap:lock");
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                RMap<Object, Date> rMap = redissonClient.getMap("aimodule:job:operationTime");
                Date startTime = rMap.get("startTime");
                Date endTime = new Date();

                // 如果 startTime 为空，表示是第一次执行，可以将其设置为当前时间
                if (startTime == null) {
                    rMap.put("startTime", endTime);
                }

                rMap.put("endTime", endTime);

                log.info("上次开始时间startTime: " + startTime);

                // 使用 Duration.between 计算两个时间点之间的间隔
                Duration duration = Duration.between(startTime.toInstant(), endTime.toInstant());
                long minutesBetween = duration.toMinutes();

                LambdaUpdateWrapper<Chart> updateWrapper = Wrappers.lambdaUpdate();
                updateWrapper.set(Chart::getStatus, "failure")
                        .between(Chart::getCreateTime, startTime, endTime)
                        .notIn(Chart::getStatus, "succeed", "failure");

                int updatedCount = chartMapper.update(null, updateWrapper);

                log.info("更新了 {} 条记录的status为\"failure\"", updatedCount);

                // 将本次的结束时间设置为下一轮的开始时间
                rMap.put("startTime", endTime);

            } else {
                log.info("未获取到锁，跳过本次执行");
            }
        } catch (InterruptedException e) {
            log.error("发生异常", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }

        log.info("补偿机制执行完毕");
    }


//    private Date initialTime = new Date();
//
////    @Scheduled(fixedRate = 60 * 1000 * 1)
//    @Scheduled(cron = "* * * * * ?")
//    public void chatrun() {
//        log.info("开始枪锁 ------> 执行补偿机制");
//        RLock lock = redissonClient.getLock("aimodule:job:IncSyncSensitiveToMap:lock");
//        LocalDateTime currentTime = LocalDateTime.now().minusMinutes(30);
//
//        try {
//            if ( lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
//                RMap<Object, Date> rMap = redissonClient.getMap("aimodule:job:operationTime");
//                Date startTime = rMap.get("startTime");
//                Date endTime = new Date();
//                rMap.put("endTime", endTime);
//
//                log.info("枪锁成功 ------> 上次开始时间startTime: " + startTime);
//                LambdaUpdateWrapper<Chart> updateWrapper = Wrappers.lambdaUpdate();
//                updateWrapper.set(Chart::getStatus, "failure") // 将status更新为"failure"
//                        .between("createTime",startTime,endTime)
////                        .lt(Chart::getCreateTime, currentTime) // 创建时间大于当前时间三分钟以上
//                        .notIn(Chart::getStatus,"succeed","failure"); // status 不等于"succeed"和"failure"
//
//                int updatedCount = chartMapper.update(null, updateWrapper);
//                log.info("更新了 {} 条记录的status为\"failure\"", updatedCount);
//                rMap.put("startTime", endTime);
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        log.info("一轮任务执行完毕 ------> 补偿机制执行中");
//    }

}
