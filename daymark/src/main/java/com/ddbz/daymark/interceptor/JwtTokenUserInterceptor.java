package com.ddbz.daymark.interceptor;

import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Slf4j
@Component
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        //获得到请求路径
        String requestURI = request.getRequestURI();


        //获取到请求头当中的token
        String token = request.getHeader("token");
        if(token == null || token.isEmpty()){
            log.info("令牌为空，响应401");
            response.setStatus(401);
            return false;
        }

        //判断令牌是否合法
        try{
            Claims claims = jwtUtil.parseClaims(token);    // 解析一次，uid 和 role 都在里面
            BaseContext.setCurrentUid(claims.getSubject());          // 存 uid
            BaseContext.setCurrentRole(claims.get("role", Integer.class));  // 存 role
        }catch (Exception e){
            log.info("令牌违法，响应401");
            response.setStatus(401);
            return false;
        }

        log.info("令牌合法，允许放行");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        BaseContext.clear();
    }
}
