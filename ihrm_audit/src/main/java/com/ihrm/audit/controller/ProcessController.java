package com.ihrm.audit.controller;

import com.ihrm.audit.entity.ProcInstance;
import com.ihrm.audit.entity.ProcTaskInstance;
import com.ihrm.audit.service.AuditService;
import com.ihrm.audit.service.ProcessService;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 流程处理controller
 */
@CrossOrigin//解决跨域问题
@RestController
@RequestMapping(value="/user/process")
public class ProcessController extends BaseController {

    @Autowired
    private ProcessService processService;

    @Autowired
    private AuditService auditService;

    /**
     * 流程部署
     * @param file bpmn文件
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/deploy",method = RequestMethod.POST)
    public Result deployProcess(@RequestParam("file") MultipartFile file)throws Exception{
        processService.deployProcess(file,companyId);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 获取所有的流程定义
     * @return
     */
    @RequestMapping(value = "/definition",method = RequestMethod.GET)
    public Result definitionList(){
        List list = processService.getDefinitionList(companyId);
        return new Result(ResultCode.SUCCESS,list);
    }

    /**
     * 流程挂起与激活
     * @param processKey
     * @return
     */
    @RequestMapping(value = "/suspend/{processKey}",method = RequestMethod.GET)
    public Result suspendProcess(@PathVariable("processKey") String processKey){
        processService.suspendProcess(processKey,companyId);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 查询申请列表
     *  参数:
     *      page,size
     *  业务参数:
     *      审批类型
     *      审批状态(多个,每个状态之间使用","隔开)
     *      当前节点的待处理人
     */
    @RequestMapping(value = "/instance/{page}/{size}",method = RequestMethod.PUT)
    public Result instanceList(@RequestBody ProcInstance instance,@PathVariable int page,@PathVariable int size){
        Page<ProcInstance> pages= auditService.getInstanceList(instance,page,size);
        PageResult pageResult = new PageResult(pages.getTotalElements(),pages.getContent());
        return new Result(ResultCode.SUCCESS,pageResult);
    }

    /**
     * 查询申请的详情数据
     *  参数 : 申请对象的id
     */
    @RequestMapping(value = "/instance/{id}",method = RequestMethod.GET)
    public Result instanceDetail(@PathVariable String id) throws IOException {
        ProcInstance procInstance =  auditService.findInstanceDetail(id);
        return new Result(ResultCode.SUCCESS,procInstance);
    }

    //启动流程
    @RequestMapping(value = "/startProcess", method = RequestMethod.POST)
    public Result startProcess(@RequestBody Map map) {
        auditService.startProcess(map,companyId);
        return new Result(ResultCode.SUCCESS);
    }
    //提交审核
    @RequestMapping(value = "/instance/commit",method = RequestMethod.PUT)
    public Result commit(@RequestBody ProcTaskInstance taskInstance){
        auditService.commit(taskInstance,companyId);
        return new Result(ResultCode.SUCCESS);
    }


}
