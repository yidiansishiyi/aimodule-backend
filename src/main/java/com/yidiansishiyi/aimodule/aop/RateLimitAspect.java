package com.yidiansishiyi.aimodule.aop;

import com.yidiansishiyi.aimodule.annotation.RateLimit;
import com.yidiansishiyi.aimodule.manager.RedisLimiterManager;
import com.yidiansishiyi.aimodule.model.entity.User;
import com.yidiansishiyi.aimodule.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RateLimitAspect {
    @Resource
    private UserService userService;

    private final RedisLimiterManager redisLimiterManager;

    public RateLimitAspect(RedisLimiterManager redisLimiterManager) {
        this.redisLimiterManager = redisLimiterManager;
    }

    @Around("@annotation(rateLimit)")
    public Object applyRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        User loginUser = userService.getLoginUser(request);

        String key = rateLimit.key(); // 获取genChartByAi_的值
        long value = rateLimit.value();
        long duration = rateLimit.duration();

        redisLimiterManager.doRateLimit(key + "_" + loginUser.getId(), value, duration);

        // 执行被限流的方法
        return joinPoint.proceed();
    }
}
