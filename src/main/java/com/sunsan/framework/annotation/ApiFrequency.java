package com.sunsan.framework.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 用于api访问的频率控制
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ApiFrequency {
    /**
     * api的别名，仅仅用于显示
     *
     * @return
     */
    String name() default "all";

    /**
     * api频率宽度时间，单位秒
     *
     * @return
     */
    int time() default 0;

    /**
     * api在频率宽度内的访问限制次数
     *
     * @return
     */
    int limit() default 0;
}
