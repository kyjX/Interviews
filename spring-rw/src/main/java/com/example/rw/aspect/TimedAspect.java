package com.example.rw.aspect;

import com.example.rw.annotation.Timed;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义注解 @Timed 的切面实现：统计并输出方法耗时。
 */
@Aspect
@Component
public class TimedAspect {

    private static final Logger log = LoggerFactory.getLogger(TimedAspect.class);

    @Around("@annotation(timed)")
    public Object logElapsed(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            String label = timed.value().isBlank()
                    ? joinPoint.getSignature().toShortString()
                    : timed.value();
            log.info("[Timed] {} finished in {} ms", label, elapsedMs);
        }
    }
}
