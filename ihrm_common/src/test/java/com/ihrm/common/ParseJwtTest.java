package com.ihrm.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class ParseJwtTest {
    public static void main(String[] args) {
        String token =
                "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMDc1MzgzMTMzMTA2NDI1ODU2Iiwic3ViIjoidGVzdDAwMSIsImNvbXBhbnlJZCI6IjEiLCJjb21wYW55TmFtZSI6IuS8oOaZuuaSreWuoiIsImV4cCI6MTU4NDQ0MDY4OH0.8iCAqIeSYk_mmQMWDt6b5UrGFDQsgRuMXQHsORoJI3o";
        Claims claims = Jwts.parser().setSigningKey("saas-ihrm").parseClaimsJws(token).getBody();
        //所有的信息都存在claims中了
        System.out.println(claims.getId());
        System.out.println(claims.getSubject());
        System.out.println(claims.getIssuedAt());
        //解析claims中自定义的内容
        System.out.println((String) claims.get("companyId")+"------------"+(String)claims.get("companyName"));
    }
}
