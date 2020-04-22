package com.ihrm.domain.system.response;

import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import lombok.Data;
import org.crazycake.shiro.AuthCachePrincipal;

import java.io.Serializable;
import java.util.*;

@Data
public class ProfileResult implements Serializable, AuthCachePrincipal {

    private String mobile;
    private String username;
    private String company;
    private String companyId;
    private String userId;
    private Map<String, Object> roles = new HashMap<>();

    /**
     * saas管理员与企业管理员的所有权限
     * @param user
     * @param list
     */
    public ProfileResult(User user, List<Permission> list){
        this.mobile = user.getMobile();
        this.username = user.getUsername();
        this.userId = user.getId();
        this.company = user.getCompanyName();
        this.companyId = user.getCompanyId();
        Set<Role> roles = user.getRoles();
        Set<String> menus = new HashSet<>();
        Set<String> points = new HashSet<>();
        Set<String> apis = new HashSet<>();
        for (Permission permission : list) {
            String code = permission.getCode();
            if (permission.getType() == 1) {
                menus.add(code);
            } else if (permission.getType() == 2) {
                points.add(code);
            } else {
                apis.add(code);
            }
        }
        this.roles.put("menus",menus);
        this.roles.put("points",points);
        this.roles.put("apis",apis);
    }

    /**
     * 企业用户的权限
     * @param user
     */
    public ProfileResult(User user) {
        this.mobile = user.getMobile();
        this.username = user.getUsername();
        this.company = user.getCompanyName();
        this.companyId = user.getCompanyId();
        this.userId = user.getId();
        Set<Role> roles = user.getRoles();
        Set<String> menus = new HashSet<>();
        Set<String> points = new HashSet<>();
        Set<String> apis = new HashSet<>();
        for (Role role : roles) {
            Set<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                String code = permission.getCode();
                if (permission.getType() == 1) {
                    menus.add(code);
                } else if (permission.getType() == 2) {
                    points.add(code);
                } else {
                    apis.add(code);
                }
            }
        }
        this.roles.put("menus",menus);
        this.roles.put("points",points);
        this.roles.put("apis",apis);
    }

    @Override
    public String getAuthCacheKey() {
        return null;
    }
}

