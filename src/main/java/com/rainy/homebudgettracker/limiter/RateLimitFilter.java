package com.rainy.homebudgettracker.limiter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(1)
@Log4j2
public class RateLimitFilter implements Filter {
    private final ScheduledExecutorService executor;
    private final ConcurrentHashMap<String, AtomicLong> requestCount;
    private final Long limit;
    private final Long blockDurationInMinutes;
    private final Long limitDurationInSeconds;

    public RateLimitFilter() {
        executor = new ScheduledThreadPoolExecutor(8);
        requestCount = new ConcurrentHashMap<>();
        limit = 1000L;
        blockDurationInMinutes = 5L;
        limitDurationInSeconds = 2L;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        String ipAddress = servletRequest.getRemoteAddr();
        AtomicLong count = requestCount.computeIfAbsent(ipAddress, k -> new AtomicLong());

        if (count.incrementAndGet() > limit) {
            log.info("IP: {} count: {}", ipAddress, count.get());

            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests from this IP. Please try again later.");

            executor.schedule(() -> {
                requestCount.remove(ipAddress);
            }, blockDurationInMinutes, TimeUnit.MINUTES);

            return;
        }

        executor.schedule(() -> {
            if (count.get() >=  limit) {
                return;
            }

            if (count.decrementAndGet() == 0) {
                requestCount.remove(ipAddress);
            }
        }, limitDurationInSeconds, TimeUnit.SECONDS);

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
