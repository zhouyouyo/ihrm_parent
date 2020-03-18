package com.ihrm.system.service;

import com.ihrm.common.service.BaseService;
import com.ihrm.common.utils.IdWorker;
//import com.ihrm.common.utils.PermissionConstants;
import com.ihrm.common.utils.PermissionConstants;
import com.ihrm.domain.system.Permission;

//import com.ihrm.system.dao.PermissionDao;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import com.ihrm.system.dao.PermissionDao;
import com.ihrm.system.dao.RoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * 角色操作业务逻辑层
 */
@Service
public class RoleService extends BaseService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PermissionDao permissionDao;

//    @Autowired
//    private PermissionDao permissionDao;

    /**
     * 分配权限
     */
    /*public void assignPerms(String roleId,List<String> permIds) {
        //1.获取分配的角色对象
        Role role = roleDao.findById(roleId).get();
        //2.构造角色的权限集合
        Set<Permission> perms = new HashSet<>();
        for (String permId : permIds) {
            Permission permission = permissionDao.findById(permId).get();
            //需要根据父id和类型查询API权限列表
            List<Permission> apiList = permissionDao.findByTypeAndPid(PermissionConstants.PERMISSION_API, permission.getId());
            perms.addAll(apiList);//自定赋予API权限
            perms.add(permission);//当前菜单或按钮的权限
        }
        System.out.println(perms.size());
        //3.设置角色和权限的关系
        role.setPermissions(perms);
        //4.更新角色
        roleDao.save(role);
    }*/

    /**
     * 添加角色
     */
    public void save(Role role){
        //填充其他参数
        role.setId(idWorker.nextId() + "");
        roleDao.save(role);
    }

    /**
     * 更新角色
     */
    public void update(Role role) {
        Role targer = roleDao.getOne(role.getId());
        targer.setDescription(role.getDescription());
        targer.setName(role.getName());
        roleDao.save(targer);
    }

    /**
     * 根据ID查询角色
     */
    public Role findById(String id) {
        return roleDao.findById(id).get();
    }

    public List<Role> findAll(String companyId) {
        return roleDao.findAll(getSpec(companyId));
    }

    /**
     * 删除角色
     */
    public void delete(String id) {
        roleDao.deleteById(id);
    }

    public Page<Role> findByPage(String companyId, int page, int size) {
        return roleDao.findAll(getSpec(companyId), PageRequest.of(page-1, size));
    }

    /**
     * 给角色赋予权限
     * @param id
     * @param permIds 所勾选的菜单id或者按钮id
     */
    public void assignPerms(String id, List<String> permIds) {
        Role role = roleDao.findById(id).get();
        Set<Permission> set = new HashSet<>();
        for (String permId : permIds) {
            //查询当前所勾选的菜单权限id或者按钮权限id的权限信息
            Permission permission = permissionDao.findById(permId).get();
            //当前菜单权限id或者按钮权限id查询所对应api权限信息,
            List<Permission> list = permissionDao.findByTypeAndPid(PermissionConstants.PY_API, permission.getId());
            set.addAll(list);//添加api权限信息
            set.add(permission);//添加菜单或按钮信息
        }
        role.setPermissions(set);
        //更新角色
        roleDao.save(role);
    }


}
