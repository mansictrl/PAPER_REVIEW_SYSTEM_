package com.project.paperreview.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        // 👇 THIS LINE PRINTS THE PATH
        System.out.println("🔍 [FILTER] Request Path: " + req.getRequestURI());

        // Continue request
        chain.doFilter(request, response);
    }
}