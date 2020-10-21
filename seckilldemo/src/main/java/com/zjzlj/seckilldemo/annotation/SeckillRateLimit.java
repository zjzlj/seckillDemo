package com.zjzlj.seckilldemo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface SeckillRateLimit {
    long permitsPerSecond() default 1000;

    long maxBurstSeconds() default 1;

    long maxWaitSecond() default 1;

    String key() default "";

    Class<?>[] groups() default { };


}
