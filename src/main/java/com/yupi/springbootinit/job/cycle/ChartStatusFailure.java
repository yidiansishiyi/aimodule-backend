package com.yupi.springbootinit.job.cycle;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yupi.springbootinit.mapper.ChartMapper;
import com.yupi.springbootinit.model.entity.Chart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Component
@Slf4j
public class ChartStatusFailure {
    @Resource
    private ChartMapper chartMapper;

    @Scheduled(fixedRate = 60 * 1000 * 1)
    public void chatrun() {
        LocalDateTime currentTime = LocalDateTime.now().minusMinutes(30);

        LambdaUpdateWrapper<Chart> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(Chart::getStatus, "failure") // 将status更新为"failure"
                .lt(Chart::getCreateTime, currentTime) // 创建时间大于当前时间三分钟以上
                .ne(Chart::getStatus, "succeed"); // status不等于"succeed"

        int updatedCount = chartMapper.update(null, updateWrapper);
        log.info("更新了 {} 条记录的status为\"failure\"", updatedCount);
    }
}
