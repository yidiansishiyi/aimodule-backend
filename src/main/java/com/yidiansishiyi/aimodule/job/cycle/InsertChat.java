package com.yidiansishiyi.aimodule.job.cycle;

import com.yidiansishiyi.aimodule.model.entity.Chart;
import com.yidiansishiyi.aimodule.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

//@Component
@Slf4j
@RestController
@RequestMapping("/chartScheduled")
public class InsertChat {
    @Resource
    private ChartService chartService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    @Value("${scheduled.testChartDataInsert:false}")
    private Boolean testChartDataInsert;

    @GetMapping("/insertChat")
    @Scheduled(fixedRate = 30 * 1000) // 三十秒一次百万数据
    public void chatrun() {
        if ( testChartDataInsert ){
            List<List<Chart>> futures = new ArrayList<>();
            StopWatch totalDuration = new StopWatch();
            StopWatch buildInputDuration = new StopWatch();
            totalDuration.start();

            ArrayList<CompletableFuture<Void>> completableFutures = new ArrayList<>();
            buildInputDuration.start();
            for (int i = 0; i < 30; i++) {
                List<Chart> charts = new LinkedList<>();
                for (int j = 0; j < 50000; j++) {
                    Chart chart = Chart.builder()
                            .goal("分析店铺内商品" + j)
                            .name("商品分析" + j)
                            .chartType("折线图")
                            .status("succeed")
                            .userId(1652457118348935170L)
                            .build();
                    charts.add(chart);
                }
                log.info("构建数据时长: " + buildInputDuration.getTime());
                buildInputDuration.suspend();
                buildInputDuration.resume();
                CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                    try {
                        System.out.println("threadName: " + Thread.currentThread().getName());
                        StopWatch singleBatchInsertionTime = new StopWatch();
                        singleBatchInsertionTime.start();
                        chartService.saveBatch(charts, 500);
                        singleBatchInsertionTime.stop();
                        log.info("单批次插入时长: " + singleBatchInsertionTime.getTime());
                    } catch (Exception e) {
                        // 处理异常，可以记录到日志文件中
                        log.error("插入数据发生异常: " + e.getMessage());
                    }
                }, threadPoolExecutor);
                completableFutures.add(voidCompletableFuture);
            }


            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{})).join();

            totalDuration.stop();
            log.info("插入 数据总时长: " + totalDuration.getTime());
        }
    }

}
