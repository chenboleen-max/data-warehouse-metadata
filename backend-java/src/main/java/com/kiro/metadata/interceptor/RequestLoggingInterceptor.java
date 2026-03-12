package com.kiro.metadata.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 请求日志拦截器
 */
@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 生成请求 ID
        String requestId = UUID.randomUUID().toString();
        request.setAttribute("requestId", requestId);
        request.setAttribute("startTime", System.currentTimeMillis());

        // 设置 MDC（用于日志追踪）
        MDC.put("requestId", requestId);

        // 记录请求信息
        log.info("API request: method={}, path={}, query={}, ip={}, userAgent={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // 计算处理时间
        long startTime = (Long) request.getAttribute("startTime");
        long processTime = System.currentTimeMillis() - startTime;

        // 设置响应头
        response.setHeader("X-Process-Time", String.valueOf(processTime));

        // 记录响应信息
        log.info("API response: method={}, path={}, status={}, processTime={}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                processTime);

        // 清除 MDC
        MDC.clear();
    }
}
