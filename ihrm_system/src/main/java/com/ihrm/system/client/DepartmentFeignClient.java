package com.ihrm.system.client;

import com.ihrm.common.entity.Result;
import com.ihrm.domain.company.Department;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

//@FeignClient注解用于指定从哪个服务中调用功能 ，注意里面的名称与被调用的服务名保持一致
@FeignClient(value = "ihrm-company")
public interface DepartmentFeignClient {
    //@RequestMapping注解用于对被调用的微服务进行地址映射
    @RequestMapping(value = "/company/department/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable("id") String id) throws Exception;

    @RequestMapping(value="/company/department/search",method = RequestMethod.POST)
    public Department findByCode(@RequestParam("code")String code);
}
