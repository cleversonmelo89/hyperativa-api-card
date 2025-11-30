package com.hyperativa.cardapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID = "requestId";
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_ID, requestId);
        request.setAttribute(START_TIME, System.currentTimeMillis());
        
        log.info("Incoming request - ID: {}, Method: {}, URI: {}, RemoteAddr: {}",
                requestId, request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                          @SuppressWarnings("unused") ModelAndView modelAndView) {
        String requestId = (String) request.getAttribute(REQUEST_ID);
        Long startTime = (Long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - (startTime != null ? startTime : 0);
        
        log.info("Outgoing response - ID: {}, Status: {}, Duration: {}ms",
                requestId, response.getStatus(), duration);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex != null) {
            String requestId = (String) request.getAttribute(REQUEST_ID);
            log.error("Request failed - ID: {}, Error: {}", requestId, ex.getMessage(), ex);
        }
    }
}


