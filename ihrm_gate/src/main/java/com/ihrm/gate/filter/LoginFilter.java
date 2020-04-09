package com.ihrm.gate.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 自定义过滤器
 */
//@Component
public class LoginFilter extends ZuulFilter {
    /**
     * String类型的返回值
     *  定义过滤器类型的
     *      pre     : 在执行路由请求之前执行
     *      routing : 在路由请求时调用(先执行过滤然后再进行路由)
     *      post    : 在routing（路由）和error（发生错误，发生了error过滤器之后依然会执行该过滤器）过滤器之后执行
     *      error   : 处理请求出现异常的时候执行
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * int类型的返回值
     *  定义过滤器的优先级 : 数字越小,优先级越高
     *      比方说还有一个过滤器，相对LoginFilter来说看谁的优先级更高些
     */
    @Override
    public int filterOrder() {
        return 2;
    }

    /**
     * boolean类型的返回值
     *  判断过滤器是否需要执行
     *  true才允许放行过滤器，进行路由的转发
     *  false不允许放行
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * run方法 : 过滤器中负责的具体业务逻辑
     *  使用过滤器进行jwt的鉴权
     */
    @Override
    public Object run() throws ZuulException {
//        System.out.println("执行了LoginFilter的run方法");
        //基于jwt的统一鉴权
        //获取Zuul网关的上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        //获取request对象
        HttpServletRequest request = requestContext.getRequest();
        //获取请求头中Authorization信息
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)){
            //没有传递token信息,需要登录,拦截,不允许路由
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;//允许路由
    }
}
