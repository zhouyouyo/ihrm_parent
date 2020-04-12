package com.ihrm.atte.controller;

import com.ihrm.atte.service.ConfigurationService;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.atte.entity.AttendanceConfig;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cfg")
public class ConfigController extends BaseController {

    @Autowired
    private ConfigurationService configurationService;

    @RequestMapping(value = "/atte/item",method = RequestMethod.POST)
    public Result atteConfig(String departmentId){
        AttendanceConfig atteConfig = configurationService.getAtteConfig(companyId, departmentId);
        return new Result(ResultCode.SUCCESS,atteConfig);
    }

    @RequestMapping(value = "/atte",method = RequestMethod.PUT)
    public Result saveAtteConfig(@RequestBody AttendanceConfig atteConfig){
        atteConfig.setCompanyId(companyId);
        configurationService.saveAtteConfig(atteConfig);
        return new Result(ResultCode.SUCCESS);
    }
}
