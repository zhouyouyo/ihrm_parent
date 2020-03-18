package com.ihrm.common.utils;

import io.jsonwebtoken.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Date;
import java.util.Map;

@Data
@ConfigurationProperties("jwt.config")//自动将配置文件中的属性值读取出来赋值给加了该注解的类的同名的属性身上，指明前缀之后能自动赋值
public class JwtUtils {

    private String key;//私钥
    private Long ttl;//jwt失效时间

    /**
     * 创建jwt令牌
     * @param id 登录用户id
     * @param subject 登录用户名
     * @param map jwt中自定义内容信息
     * @return jwt令牌
     */
    public String createJwt(String id, String subject, Map<String,Object> map){
        long now = System.currentTimeMillis();
        long exp = now+ttl;//失效时间
        JwtBuilder jwtBuilder = Jwts.builder().setId(id).setSubject(subject)
                .signWith(SignatureAlgorithm.HS256, key);
        for (String key : map.keySet()) {
            jwtBuilder.claim(key,map.get(key));
        }
        if (ttl>0){
            jwtBuilder.setExpiration(new Date(exp));
        }
        return jwtBuilder.compact();
    }

    /**
     * 解析jwt
     * @param token
     * @return
     */
    public Claims parseJwt(String token){
        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
            return claims;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
