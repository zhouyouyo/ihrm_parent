package com.ihrm.system.service;

import com.ihrm.common.utils.IdWorker;
import com.ihrm.common.utils.QiniuUploadUtil;
import com.ihrm.domain.company.Department;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import com.ihrm.system.client.DepartmentFeignClient;
import com.ihrm.system.dao.RoleDao;
import com.ihrm.system.dao.UserDao;
import com.ihrm.system.utils.BaiduAiUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.lang.annotation.Target;
import java.util.*;

import com.sun.org.apache.xml.internal.security.utils.Base64;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private DepartmentFeignClient departmentFeignClient;

    @Autowired
    private BaiduAiUtil baiduAiUtil;

    /**
     * 1.保存用户
     */
    public void save(User user) {
        //设置主键的值
        String id = idWorker.nextId()+"";
        user.setPassword("123456");//设置初始密码
        user.setEnableState(1);
        user.setId(id);
        //调用dao保存部门
        userDao.save(user);
    }

    /**
     * 2.更新用户
     */
    public void update(User user) {
        //1.根据id查询部门
        User target = userDao.findById(user.getId()).get();
        //2.设置部门属性
        target.setUsername(user.getUsername());
        target.setPassword(user.getPassword());
        target.setDepartmentId(user.getDepartmentId());
        target.setDepartmentName(user.getDepartmentName());
        //3.更新部门
        userDao.save(target);
    }

    /**
     * 3.根据id查询用户
     */
    public User findById(String id) {
        return userDao.findById(id).get();
    }

    /**
     * 4.查询全部用户列表
     *      参数：map集合的形式
     *          hasDept
     *          departmentId
     *          companyId
     *
     */
    public Page findAll(Map<String,Object> map,int page, int size) {
        //1.需要查询条件
        Specification<User> spec = new Specification<User>() {
            /**
             * 动态拼接查询条件
             * @return
             */
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> list = new ArrayList<>();
                //根据请求的companyId是否为空构造查询条件
                if(!StringUtils.isEmpty(map.get("companyId"))) {
                    list.add(criteriaBuilder.equal(root.get("companyId").as(String.class),(String)map.get("companyId")));
                }
                //根据请求的部门id构造查询条件
                if(!StringUtils.isEmpty(map.get("departmentId"))) {
                    list.add(criteriaBuilder.equal(root.get("departmentId").as(String.class),(String)map.get("departmentId")));
                }
                if(!StringUtils.isEmpty(map.get("hasDept"))) {
                    //根据请求的hasDept判断  是否分配部门 0未分配（departmentId = null），1 已分配 （departmentId ！= null）
                    if("0".equals((String) map.get("hasDept"))) {
                        list.add(criteriaBuilder.isNull(root.get("departmentId")));
                    }else {
                        list.add(criteriaBuilder.isNotNull(root.get("departmentId")));
                    }
                }
                return criteriaBuilder.and(list.toArray(new Predicate[list.size()]));
            }
        };

        //2.分页
        Page<User> pageUser = userDao.findAll(spec, new PageRequest(page-1, size));
        return pageUser;
    }

    /**
     * 5.根据id删除用户
     */
    public void deleteById(String id) {
        userDao.deleteById(id);
    }

    /**
     * 给用户赋予角色
     * @param id
     * @param roleIds
     */
    public void assignRoles(String id, List<String> roleIds) {
        User user = userDao.findById(id).get();
        Set<Role> set = new HashSet<>();
        for (String roleId : roleIds) {
            Role role = roleDao.findById(roleId).get();
            set.add(role);
        }
        user.setRoles(set);
        //更新用户
        userDao.save(user);
    }

    public User findByMobile(String mobile){
        return userDao.findByMobile(mobile);
    }

    //根据单元的数据类型获取内容
    public Object getCellValue(Cell cell){
        //获取单元格的数据类型
        CellType cellType = cell.getCellType();
        Object value = null;
        switch (cellType){
            case STRING://字符串
                value = cell.getStringCellValue();
                break;
            case BOOLEAN://布尔类型
                value = cell.getBooleanCellValue();
                break;
            case NUMERIC://数字类型，时间类型和整数类型以及小数类型都是数字类型
                if (DateUtil.isCellDateFormatted(cell)){
                    //日期类型
                    value = cell.getDateCellValue();
                }else{
                    //数字类型
                    value = cell.getNumericCellValue();
                }
                break;
            case FORMULA://获取公式
                value = cell.getCellFormula();
                break;
            default:
                break;
        }
        return value;
    }

    /**
     * 批量保存
     */
    @Transactional
    public void saveAll(List<User> list,String companyId,String companyName){
        for (User user : list) {
            user.setId(idWorker.nextId()+"");
            //基本属性
            user.setCompanyId(companyId);
            user.setCompanyName(companyName);
            user.setInServiceStatus(1);
            user.setEnableState(1);
            user.setLevel("user");
            user.setCreateTime(new Date());
            Department department = departmentFeignClient.findByCode(user.getDepartmentId());//此时部门id是存的code
            if (department!=null){
                user.setDepartmentId(department.getId());
                user.setDepartmentName(department.getName());
            }
            user.setPassword(new Md5Hash("123456",user.getMobile(),3).toString());
            userDao.save(user);
        }
    }

    /**
     * 保存用户头像
     * @param id
     * @param file
     * @return
     */
    /*public String upload(String id, MultipartFile file) throws IOException {
        User user = userDao.findById(id).get();
        String encode = Base64.encode(file.getBytes());
        String dataUrl = "data:image/png;base64,"+encode;
        user.setStaffPhoto(dataUrl);
        userDao.save(user);
        return dataUrl;
    }*/

    /**
     * 将图片data URL改成七牛云服务进行存储
     * @param id
     * @param file
     * @return
     * @throws IOException
     */
    /*public String upload(String id, MultipartFile file) throws IOException {
        User user = userDao.findById(id).get();
        String uploadUrl = new QiniuUploadUtil().upload(id, file.getBytes());
        user.setStaffPhoto(uploadUrl);
        userDao.save(user);
        return uploadUrl;
    }*/

    /**
     * 将用户头像保存到七牛云进行存储，并将头像
     * 注册更新到百度云AI人脸库中用于进行人脸识别，检测，登录
     * @param id
     * @param file
     * @return
     * @throws IOException
     */
    public String upload(String id, MultipartFile file) throws IOException {
        User user = userDao.findById(id).get();
        String uploadUrl = new QiniuUploadUtil().upload(id, file.getBytes());
        user.setStaffPhoto(uploadUrl);
        userDao.save(user);
        //保存头像到百度云人脸库中
        boolean flag = baiduAiUtil.faceExit(id);
        String imgBase64 = Base64.encode(file.getBytes());
        if (flag){
            //更新
            baiduAiUtil.faceUpdate(id,imgBase64);
        }else {
            //注册
            baiduAiUtil.faceRegister(id,imgBase64);
        }
        return uploadUrl;
    }
}
