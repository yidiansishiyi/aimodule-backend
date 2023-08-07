package com.yidiansishiyi.aimodule.manager;

import com.github.rholder.retry.*;
import com.yidiansishiyi.aimodule.common.ErrorCode;
import com.yidiansishiyi.aimodule.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 用于对接 AI 平台
 */
@Service
@Slf4j
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    /**
     * AI 对话
     *
     * @param modelId
     * @param message
     * @return
     */
    public String doChat(long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = null;
        try {
            response = yuCongMingClient.doChat(devChatRequest);
            if (response == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
            }
        } catch (Exception e) {
            response = retryDoChat(devChatRequest);
            log.error("Error generating{}", e.getMessage());
            throw new RuntimeException(e);
        }

        return response.getData().getContent();
    }

    /**
     * 重试调用AI对话接口
     *
     * @param
     * @return
     */
    public BaseResponse<DevChatResponse>  retryDoChat(DevChatRequest devChatRequest) {
        Retryer<BaseResponse<DevChatResponse>> retryer = RetryerBuilder.<BaseResponse<DevChatResponse>>newBuilder()
                .retryIfResult(result -> result == null || result.getData().getContent().isEmpty()) // 在返回结果为null或空字符串时进行重试
                .withStopStrategy(StopStrategies.stopAfterAttempt(3)) // 设置最大重试次数为3次
                .withWaitStrategy(WaitStrategies.fixedWait(500, TimeUnit.MILLISECONDS)) // 设置重试间隔为500毫秒
                .build();

        Callable<BaseResponse<DevChatResponse>> retryLogic = () -> {
            // 重试逻辑，调用AI对话接口
            BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
            return response;
        };

        try {
            // 使用Retryer来执行重试逻辑
            return retryer.call(retryLogic);
        } catch (ExecutionException | RetryException e) {
            // 重试失败的处理逻辑
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "重试失败：" + e.getMessage());
        }
    }
}
