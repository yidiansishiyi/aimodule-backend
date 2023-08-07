package com.yidiansishiyi.aimodule.utils;

import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import com.yidiansishiyi.aimodule.service.ChartService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RetryUtils {
    private static final int MAX_RETRY_TIMES = 3;
    private static final long RETRY_INTERVAL_MS = 500;

    /**
     * 重试调用逻辑
     *
     * @param userInput 用户输入数据
     * @param chartService 调用图表服务的接口
     * @return 生成的图表数据，若重试失败则返回null
     */
    public static String retryGenerateChart(String userInput, ChartService chartService) {
        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
                .retryIfResult(Predicates.isNull()) // 在返回结果为null时进行重试
                .withStopStrategy(StopStrategies.stopAfterAttempt(MAX_RETRY_TIMES)) // 设置最大重试次数
                .withWaitStrategy(WaitStrategies.fixedWait(RETRY_INTERVAL_MS, TimeUnit.MILLISECONDS)) // 设置重试间隔
                .build();

        Callable<String> retryLogic = () -> {
            // 重试逻辑，调用chartService的方法进行生成图表
            return chartService.getAiGenerateChart(userInput);
        };

        try {
            // 使用Retryer来执行重试逻辑
            return retryer.call(retryLogic);
        } catch (ExecutionException | RetryException e) {
            // 重试失败的处理逻辑
            log.error("Retry failed: {}", e.getMessage());
            System.out.println("Retry failed: " + e.getMessage());
            return null;
        }
    }
}
