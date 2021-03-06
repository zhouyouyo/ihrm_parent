package com.ihrm.system.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;

import com.ihrm.common.exception.CommonException;
import com.ihrm.common.poi.ExcelImportUtil;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.common.utils.JwtUtils;
import com.ihrm.common.utils.PermissionConstants;
import com.ihrm.domain.company.Department;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.ProfileResult;
import com.ihrm.domain.system.response.UserResult;
import com.ihrm.system.client.DepartmentFeignClient;
import com.ihrm.system.service.PermissionService;
import com.ihrm.system.service.UserService;
import io.jsonwebtoken.Claims;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

//1.解决跨域
@CrossOrigin
//2.声明restContoller
@RestController
//3.设置父路径
@RequestMapping(value = "/sys")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private DepartmentFeignClient departmentFeignClient;

    //远程调用企业服务
    @RequestMapping(value = "/test/{id}", method = RequestMethod.GET)
    public Result findByDepId(@PathVariable("id") String id) throws Exception{
        return departmentFeignClient.findById(id);
    }

    /**
     * 保存
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public Result save(@RequestBody User user) {
        String id = idWorker.nextId()+"";
        //1.设置保存的企业id
        user.setCompanyId(companyId);
        user.setCompanyName(companyName);
        //对密码加密
        String password = new Md5Hash("123456",user.getMobile(),3).toString();//1.密码，盐，加密次数;
        user.setPassword(password);
        user.setLevel("user");
        user.setEnableState(1);
        user.setId(id);
        //2.调用service完成保存企业
        userService.save(user);
        //3.构造返回结果
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 查询企业的部门列表
     * 指定企业id
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Result findAll(int page, int size, @RequestParam Map map) {
        //1.获取当前的企业id
        map.put("companyId", companyId);
        //2.完成查询
        Page<User> pageUser = userService.findAll(map, page, size);
        //3.构造返回结果
        PageResult pageResult = new PageResult(pageUser.getTotalElements(), pageUser.getContent());
        return new Result(ResultCode.SUCCESS, pageResult);
    }

    /**
     * 根据ID查询user
     */
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable(value = "id") String id) {
        User user = userService.findById(id);
        UserResult userResult = new UserResult(user);
        return new Result(ResultCode.SUCCESS, userResult);
    }

    /**
     * 修改User
     */
    @RequestMapping(value = "/user/{id}", method = RequestMethod.PUT)
    public Result update(@PathVariable(value = "id") String id, @RequestBody User user) {
        //1.设置修改的部门id
        user.setId(id);
        //2.调用service更新
        userService.update(user);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据id删除
     */
    @RequiresPermissions(value = "API-USER-DELETE")//加上该注解，就会执行realm自定义的类中的doGetAuthorizationInfo授权的方法
    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE,name = "API-USER-DELETE")
    public Result delete(@PathVariable(value = "id") String id) {
        userService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 给用户赋予角色
     *
     * @param map
     */
    @RequestMapping(value = "/user/assignRoles", method = RequestMethod.PUT)
    public Result assignRoles(@RequestBody Map<String, Object> map) {
        //获取用户id，
        String id = (String) map.get("id");
        //获取勾选的角色id
        List<String> roleIds = (List<String>) map.get("roleIds");
        userService.assignRoles(id, roleIds);
        return new Result(ResultCode.SUCCESS);
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result login(@RequestBody Map<String, Object> loginMap) {
        String mobile = (String) loginMap.get("mobile");
        String password = (String) loginMap.get("password");
        try {
            //构造安全数据
            //对密码加密,以mobile作为盐
            password = new Md5Hash(password,mobile,3).toString();//1.密码，盐，加密次数
            //获取subject
            Subject subject = SecurityUtils.getSubject();
            //构造token令牌数据
            UsernamePasswordToken upToken = new UsernamePasswordToken(mobile, password);
            //登录,进入realm完成认证
            subject.login(upToken);
            //获取sessionId
            String id = (String) subject.getSession().getId();
            return new Result(ResultCode.SUCCESS,id);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return new Result(ResultCode.MOBILEORPASSWORDERROR);
        }
        /*User user = userService.findByMobile(mobile);
        if (user == null || !user.getPassword().equals(password)) {
            return new Result(ResultCode.MOBILEORPASSWORDERROR);
        }
        Map<String, Object> map = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        //将该用户这个角色的所有权限放入到jwt令牌中，然后在访问某个api之前校验权限信息中是否有访问该api的权限
        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
            Set<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                if (permission.getType() == PermissionConstants.PY_API){
                    builder.append(permission.getCode()).append(",");
                }
            }
        }
        map.put("apis",builder.toString());
        map.put("companyId", user.getCompanyId());
        map.put("companyName", user.getCompanyName());
        String jwt = jwtUtils.createJwt(user.getId(), user.getUsername(), map);
        return new Result(ResultCode.SUCCESS, jwt);*/
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public Result profile(HttpServletRequest request) throws CommonException {
        //获取session中的安全数据
        Subject subject = SecurityUtils.getSubject();
        //1.subject获取所有的安全数据集合
        PrincipalCollection principals = subject.getPrincipals();
        //2.获取安全数据
        ProfileResult profileResult = (ProfileResult) principals.getPrimaryPrincipal();
        //获取用户id，用户名，手机号等
        /*String id =  claims.getId();//用户id
        User user = userService.findById(id);
        ProfileResult profileResult = null;
        //根据用户等级获取响应的权限
        if ("user".equals(user.getLevel())){
            //获取企业用户的所有权限
            profileResult = new ProfileResult(user);
        }else {
            Map<String,Object> map = new HashMap<>();
            //获取saas管理员或者企业管理员的所有权限
            if ("coAdmin".equals(user.getLevel())){
                //如果是企业管理员则只能查saas管理员指定的权限信息，即只能是可见的菜单及下面的所有权限信息
                map.put("enVisible","1");//设置为可见
            }
            List<Permission> list = permissionService.findAll(map);//无条件表示查询的所有权限，即saas管理员的权限
            profileResult = new ProfileResult(user,list);
        }*/
        return new Result(ResultCode.SUCCESS,profileResult);
    }

    @RequestMapping(value = "/user/import", method = RequestMethod.POST)
    public Result userImport(@RequestParam("file") MultipartFile file) throws Exception{
        //构造所有用户user对象集合
//        List<User> userList = new ArrayList<>();
        //创建工作簿  HSSFWorkbook -- 2003
        /*Workbook wb = new XSSFWorkbook(file.getInputStream()); //2007版本
        //获取sheet对象
        Sheet sheet = wb.getSheetAt(0);//根据索引获取sheet对象
        //循环所有的行,sheet.getLastRowNum()得到是最后一行的索引
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            //得到每一行
            Row row = sheet.getRow(rowNum);
            //得到每个单元格数据
            Object[] objects = new Object[row.getLastCellNum()];
            //循环每一行的所有列，row.getLastCellNum()得到最后一列，不是索引
            for (int cellNum = 1; cellNum < row.getLastCellNum(); cellNum++) {
                //得到每一个单元格
                Cell cell = row.getCell(cellNum);
                //由于每个单元格数据类型不同，因此要根据不同的数据类型获取数据
                Object cellValue = userService.getCellValue(cell);
               objects[cellNum] = cellValue;
            }
            //构造每一条user对象
            User user = new User(objects);
            userList.add(user);
        }*/
        //通过工具类实现导入的功能
        List<User> userList = new ExcelImportUtil<User>(User.class).readExcel(file.getInputStream(), 1, 1);
        //批量保存user集合
        userService.saveAll(userList,companyId,companyName);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 用户头像上传
     * @param id
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/user/upload/{id}", method = RequestMethod.POST)
    public Result upload(@PathVariable("id") String id,@RequestParam("file") MultipartFile file) throws IOException {
        String dataUrl = userService.upload(id,file);
        return new Result(ResultCode.SUCCESS,dataUrl);
    }
}
