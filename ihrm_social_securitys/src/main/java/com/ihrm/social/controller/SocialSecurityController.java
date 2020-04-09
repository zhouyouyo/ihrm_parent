package com.ihrm.social.controller;

import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.social_security.*;
import com.ihrm.social.client.SystemFeignClient;
import com.ihrm.social.service.ArchiveService;
import com.ihrm.social.service.CompanySettingsService;
import com.ihrm.social.service.PaymentItemService;
import com.ihrm.social.service.UserSocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/social_securitys")
public class SocialSecurityController extends BaseController {

    @Autowired
    private CompanySettingsService companySettingsService;

    @Autowired
    private SystemFeignClient systemFeignClient;

    @Autowired
    private UserSocialService userSocialService;

    @Autowired
    private PaymentItemService paymentItemService;

    @Autowired
    private ArchiveService archiveService;

    /**
     * 根据企业id查询社保配置信息
     * @return
     */
    @RequestMapping(value = "/settings",method = RequestMethod.GET)
    public Result settings(){
        CompanySettings companySettings = companySettingsService.findById(companyId);
        return new Result(ResultCode.SUCCESS,companySettings);
    }

    /**
     * 保存企业社保设置信息
     * @return
     */
    @RequestMapping(value = "/settings",method = RequestMethod.POST)
    public Result saveSettings(@RequestBody CompanySettings companySettings){
        companySettings.setCompanyId(companyId);
        companySettingsService.save(companySettings);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 分页查询当前企业的所有员工社保信息
     * @param map page 当前页码
     *             pageSize 每页显示条数
     * @return
     */
    @RequestMapping(value = "/list",method = RequestMethod.POST)
    public Result list(@RequestBody Map map){
        Integer page = (Integer) map.get("page");
        Integer pageSize = (Integer) map.get("pageSize");
        PageResult pageResult = companySettingsService.findAll(page,pageSize,companyId);
        return new Result(ResultCode.SUCCESS,pageResult);
    }

    /**
     *
     * @param id 根据用户id查询用户信息以及社保信息
     * @return
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public Result findById(@PathVariable("id") String id){
        Map map = new HashMap();
        //查询用户信息
        Object data = systemFeignClient.findById(id).getData();
        map.put("user",data);
        //查询用户的社保信息
        UserSocialSecurity uss =userSocialService.findById(id);
        map.put("userSocialSecurity",uss);
        return new Result(ResultCode.SUCCESS,map);
    }

    /**
     * 根据城市id查询参保城市的参保项目
     * @param id 城市id
     * @return
     */
    @RequestMapping(value = "/payment_item/{id}",method = RequestMethod.GET)
    public Result findPaymentItem(@PathVariable("id") String id){
        List<CityPaymentItem> list= paymentItemService.findAllByCityId(id);
        return new Result(ResultCode.SUCCESS,list);
    }

    /**
     * 保存或更新用户社保
     * @param uss
     * @return
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    public Result saveUserSocialSecurity(@RequestBody UserSocialSecurity uss){
        userSocialService.save(uss);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 根据月份和企业id查询月份数据报表
     * @param yearMonth
     * @param opType
     * @return
     */
    @RequestMapping(value = "/historys/{yearMonth}",method = RequestMethod.GET)
    public Result historysDetail(@PathVariable("yearMonth") String yearMonth,int opType) throws Exception{
        List<ArchiveDetail> list = new ArrayList<>();
        if(opType == 1){
            //未归档，查询当月的详细数据
            list = archiveService.getReports(yearMonth, companyId);
            System.out.println("list = " + list);
        }else {
            //已归档
            //查询归档历史信息
            Archive archive = archiveService.findArchive(companyId,yearMonth);
            //如果归档历史存在，查询归档详细信息
            if (archive!=null){
                list = archiveService.findAllDetailByArchiveId(archive.getId());
            }
        }
        return new Result(ResultCode.SUCCESS,list);
    }

    /**
     * 进行归档
     * @param yearMonth
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/historys/{yearMonth}/archive",method = RequestMethod.POST)
    public Result archive(@PathVariable("yearMonth") String yearMonth) throws Exception{
        archiveService.archive(yearMonth,companyId);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 新建下个月的报表
     * @param yearMonth
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/historys/{yearMonth}/newReport",method = RequestMethod.PUT)
    public Result newReport(@PathVariable("yearMonth") String yearMonth) throws Exception{
        //查询当月报表
        CompanySettings cs = companySettingsService.findById(companyId);
        if (cs == null){
            cs = new CompanySettings();
        }
        cs.setCompanyId(companyId);
        cs.setDataMonth(yearMonth);
        companySettingsService.save(cs);
        return new Result(ResultCode.SUCCESS);
    }
    @RequestMapping(value = "/historys/{year}/list",method = RequestMethod.GET)
    public Result historysList(@PathVariable("year") String year){
        List<Archive> list = archiveService.findArchiveByYear(companyId,year);
        return new Result(ResultCode.SUCCESS,list);
    }

    /**
     * 根据用户id和年月查询社保归档详情
     * @param userId
     * @param yearMonth
     * @return
     */
    @RequestMapping(value = "/historys/data",method = RequestMethod.GET)
    public ArchiveDetail histories(String userId,String yearMonth){
        return archiveService.findUserArchiveDetail(userId,yearMonth);
    }
}
