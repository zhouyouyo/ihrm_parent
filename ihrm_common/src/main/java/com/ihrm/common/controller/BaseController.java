package com.ihrm.common.controller;

import com.ihrm.domain.system.response.ProfileResult;
import io.jsonwebtoken.Claims;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BaseController {

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected String companyId;
    protected String companyName;
    protected Claims claims;

    //通过jwt来获取
    /*@ModelAttribute//无论控制器中的哪个接口被调用了，该方法都被调用,在springMVC拦截器发生之后接口被调用之前执行
    public void setResAnReq(HttpServletRequest request,HttpServletResponse response) {
        this.request = request;
        this.response = response;
        Object obj = request.getAttribute("user_claims");
        if (obj != null){
            this.claims = (Claims) obj;
            this.companyId = (String)claims.get("companyId");
            this.companyName = (String)claims.get("companyName");
        }
    }*/

    //通过shiro来获取
    @ModelAttribute
    public void setResAnReq(HttpServletRequest request,HttpServletResponse response) {
        this.request = request;
        this.response = response;
        //获取session中的安全数据
        Subject subject = SecurityUtils.getSubject();
        //1.subject获取所有的安全数据集合
        PrincipalCollection principals = subject.getPrincipals();
        if(principals!=null && !principals.isEmpty()){
            //2.获取安全数据
            ProfileResult profileResult = (ProfileResult) principals.getPrimaryPrincipal();
            if (profileResult != null){
                this.companyId = profileResult.getCompanyId();
                this.companyName = profileResult.getCompany();
            }
        }
    }
}
