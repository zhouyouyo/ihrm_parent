package com.ihrm.audit;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ProcessTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    /**
     * 查询已经定义了的流程信息
     */
    @Test
    public void findAll(){
        //获取流程定义查询对象
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        //添加查询条件，根据租户id进行查询
        processDefinitionQuery.processDefinitionTenantId("中信银行");
        List<ProcessDefinition> list = processDefinitionQuery.latestVersion().list();
        System.out.println(list.size());
    }
    /**
     * 测试流程的挂起与激活
     * 	 *      流程定义表:act_re_procdef
     *          状态字段 :SUSPENSION_STATE_
     *                  1.激活状态
     *                  2.挂起状态
     */
    @Test
    public void testSuspend(){
//        runtimeService.deleteProcessInstance("be1f1e09-8084-11ea-a6ce-e8d0fcfd0c9a","");
        //挂起流程
//        repositoryService.suspendProcessDefinitionById("process_leave:1:a8bbc279-7d59-11ea-86ed-e8d0fcfd0c9a");
        //激活流程
//        repositoryService.activateProcessDefinitionById("process_leave:1:a8bbc279-7d59-11ea-86ed-e8d0fcfd0c9a");
    }

    @Test
    public void deleteProcess(){
        repositoryService.deleteDeployment("4c472e3a-7fb6-11ea-aeec-e8d0fcfd0c9a",true);
    }
}
