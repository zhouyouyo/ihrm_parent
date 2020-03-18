package com.ihrm.common.interceptor;

import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从请求头中获取jwt令牌
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.isEmpty(authorization)  && authorization.startsWith("Bearer")) {
            String token = authorization.replace("Bearer ", "");
            Claims claims = jwtUtils.parseJwt(token);
            if (claims != null){
                //从claims中获取api权限信息
                String apis = (String) claims.get("apis");
                //通过Handler
                HandlerMethod handlerMethod = (HandlerMethod) handler;//封装了要调用的具体哪个api接口
                //通过HandlerMethod获取要调用的具体的api接口名
                RequestMapping requestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
                String name = requestMapping.name();//获取api接口名,前提是在api接口中指定了名称
                if (apis.contains(name)){
                    request.setAttribute("user_claims",claims);
                    return true;
                }else{
                    throw new CommonException(ResultCode.UNAUTHORISE);
                }
            }
            throw new CommonException(ResultCode.UNAUTHENTICATED);
        }
        throw new CommonException(ResultCode.UNAUTHENTICATED);
    }
}
