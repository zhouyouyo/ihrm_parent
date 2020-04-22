package com.ihrm.audit.service;

import lombok.extern.log4j.Log4j2;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 流程部署service
 */
@Service
public class ProcessService {

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 进行流程部署
     * @param file 流程文件bpmn
     * @param companyId
     */
    public void deployProcess(MultipartFile file,String companyId)throws Exception{
        //文件名称
        String fileName = file.getOriginalFilename();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().addBytes(fileName, file.getBytes())
                .tenantId(companyId);//设置租户idcompanyID,判断部署的当前流程属于哪个租户使用

        Deployment deployment = deploymentBuilder.deploy();
        System.out.println("deployment = " + deployment);
    }

    /**
     * 获取所有的流程定义
     * @param companyId
     * @return
     */
    public List getDefinitionList(String companyId) {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(companyId)
                .list();
        return list;
    }

    /**
     * 流程挂起与激活
     * @param processKey
     * @param companyId
     */
    public void suspendProcess(String processKey, String companyId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
                processDefinitionTenantId(companyId).processDefinitionKey(processKey).
                latestVersion().singleResult();
        if (processDefinition!=null){
            if (processDefinition.isSuspended()) {//挂起
                repositoryService.activateProcessDefinitionById(processDefinition.getId());
            }else {
                repositoryService.suspendProcessDefinitionById(processDefinition.getId());
            }
        }
    }
}
