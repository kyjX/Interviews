package com.example.rw.aspect;

import com.example.rw.annotation.ReadOnly;
import com.example.rw.annotation.WriteOnly;
import com.example.rw.datasource.DataSourceContextHolder;
import com.example.rw.datasource.DataSourceType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * AOP 切面：根据 @ReadOnly / @WriteOnly 在调用前切换数据源，调用后清理 ThreadLocal。
 */
@Aspect
@Component
public class DataSourceAspect {

    @Around("@annotation(com.example.rw.annotation.ReadOnly) || @annotation(com.example.rw.annotation.WriteOnly)")
    public Object route(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DataSourceType type = resolveType(method, joinPoint.getTarget().getClass());
        DataSourceContextHolder.set(type);
        try {
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    private DataSourceType resolveType(Method method, Class<?> targetClass) {
        if (AnnotationUtils.findAnnotation(method, WriteOnly.class) != null
                || AnnotationUtils.findAnnotation(targetClass, WriteOnly.class) != null) {
            return DataSourceType.MASTER;
        }
        if (AnnotationUtils.findAnnotation(method, ReadOnly.class) != null
                || AnnotationUtils.findAnnotation(targetClass, ReadOnly.class) != null) {
            return DataSourceType.SLAVE;
        }
        return DataSourceType.MASTER;
    }
}
