package com.yidiansishiyi.aimodule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    long value() default 2; // 限流阈值，表示允许通过的请求数量

    long duration() default 1; // 限流时间窗口，单位为毫秒

    String key(); // 添加一个key属性，用于接收genChartByAi_的值
}
