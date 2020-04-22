package com.ihrm.audit.service;

import com.alibaba.fastjson.JSON;
import com.ihrm.audit.client.FeignClientService;
import com.ihrm.audit.dao.ProcInstanceDao;
import com.ihrm.audit.dao.ProcTaskInstanceDao;
import com.ihrm.audit.dao.ProcUserGroupDao;
import com.ihrm.audit.entity.ProcInstance;
import com.ihrm.audit.entity.ProcTaskInstance;
import com.ihrm.audit.entity.ProcUserGroup;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.system.User;
import lombok.extern.log4j.Log4j2;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

@Service
public class AuditService {

    @Autowired
    private ProcInstanceDao procInstanceDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private FeignClientService feignClientService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcTaskInstanceDao procTaskInstanceDao;

    @Autowired
    private ProcUserGroupDao procUserGroupDao;

    @Autowired
    private EntityManager entityManager;

    /**
     * 查询申请列表
     * 参数:
     * page,size
     * 业务参数:
     * 审批类型
     * 审批状态(多个,每个状态之间使用","隔开)
     * 当前节点的待处理人
     */
    public Page<ProcInstance> getInstanceList(ProcInstance instance, int page, int size) {
        //1.使用Specification查询,构造Specification
        Specification<ProcInstance> spec = new Specification<ProcInstance>() {
            //2.构造查询条件 (根据传入参数判断,构造)
            public Predicate toPredicate(Root<ProcInstance> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<>();
                //审批类型      -- processKey
                if (!StringUtils.isEmpty(instance.getProcessKey())) {
                    list.add(cb.equal(root.get("processKey").as(String.class), instance.getProcessKey()));
                }
                //审批状态(多个,每个状态之间使用","隔开)        --processState
                if (!StringUtils.isEmpty(instance.getProcessState())) {
                    Expression<String> exp = root.<String>get("processState");
                    list.add(exp.in(instance.getProcessState().split(",")));
                }
                //当前节点的待处理人     --procCurrNodeUserId
                if (!StringUtils.isEmpty(instance.getProcCurrNodeUserId())) {
                    list.add(cb.like(root.get("procCurrNodeUserId").as(String.class), "%" + instance.getProcCurrNodeUserId() + "%"));
                }
                //发起人 -- userId
                if (!StringUtils.isEmpty(instance.getUserId())) {
                    list.add(cb.equal(root.get("userId").as(String.class), instance.getUserId()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return procInstanceDao.findAll(spec, new PageRequest(page - 1, size));
//        return procInstanceDao.findAll(new PageRequest(page - 1, size));
    }

    /**
     * 查询申请的详情数据
     * 参数 : 申请对象的id
     */
    public ProcInstance findInstanceDetail(String id) {
        return procInstanceDao.findById(id).get();
    }

    /**
     * 发起请假流程
     *
     * @param map
     * @param companyId
     */
    @Transactional
    public void startProcess(Map map, String companyId) {
        String processKey = (String) map.get("processKey");//流程id
        String processName = (String) map.get("processName");//流程名称
        String userId = (String) map.get("userId");//申请人ID
        User user = feignClientService.getUserInfoByUserId(userId);
        //构造业务数据
        ProcInstance procInstance = new ProcInstance();
        procInstance.setProcessId(idWorker.nextId() + "");
        procInstance.setProcessName(processName);
        procInstance.setProcessState("1");//审批中
        procInstance.setUserId(userId);
        procInstance.setUsername(user.getUsername());
        procInstance.setDepartmentId(user.getDepartmentId());
        procInstance.setDepartmentName(user.getDepartmentName());
        procInstance.setTimeOfEntry(user.getTimeOfEntry());
        procInstance.setProcApplyTime(new Date());
        String data = JSON.toJSONString(map);
        procInstance.setProcData(data);
        //查询流程定义信息
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey)
                .processDefinitionTenantId(companyId).latestVersion().singleResult();
        //开启流程，
        Map var = new HashMap();
        if ("process_leave".equals(processKey)){
            var.put("days",map.get("duration"));
        }
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(definition.getId(), procInstance.getProcessId(), var);//流程定义的id,业务数据id,内置的参数
        //查询即将执行的下一个任务节点
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        //自动触发第一个任务节点
        taskService.complete(task.getId());
        //再次查询即将执行的下一个任务节点，主要目的设置这个任务的当前任务节点审批人
        Task next = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        if(next != null) {
            List<User> users = findCurrUsers(next, user);//获取审批的用户
            String usernames = "", userIdS = "";
            for (User user1 : users) {
                usernames += user1.getUsername() + " ";
                userIdS += user1.getId();
            }
            procInstance.setProcCurrNodeUserId(userIdS);
            procInstance.setProcCurrNodeUserName(usernames);
        }
        //保存数据
        procInstanceDao.save(procInstance);
        ProcTaskInstance taskInstance = new ProcTaskInstance();
        taskInstance.setTaskId(idWorker.nextId()+"");
        taskInstance.setHandleOpinion("请假没意见");
        taskInstance.setHandleTime(new Date());
        taskInstance.setHandleType("2");//审批通过
        taskInstance.setHandleUserId(userId);
        taskInstance.setHandleUserName(user.getUsername());
        taskInstance.setProcessId(task.getProcessInstanceId());
        taskInstance.setTaskKey(task.getTaskDefinitionKey());
        taskInstance.setTaskName(task.getName());
        procTaskInstanceDao.save(taskInstance);
    }

    /**
     * 获取即将执行的下一个任务的审批人
     * @param nextTask
     * @param user
     * @return
     */
    private List<User> findCurrUsers(Task nextTask,User user) {
        //查询任务的节点数据(候选人组)
        List<IdentityLink> list = taskService.getIdentityLinksForTask(nextTask.getId());
        List<User> users = new ArrayList<>();
        for (IdentityLink identityLink : list) {
            String groupId = identityLink.getGroupId(); //候选人组id
            ProcUserGroup userGroup = procUserGroupDao.findById(groupId).get();//查询userGroup
            String param = userGroup.getParam();
            String paramValue = null;
            if ("user_id".equals(param)) {
                paramValue = user.getId();
            }
            else if ("department_id".equals(param)) {
                paramValue = user.getDepartmentId();
            }
            else if ("company_id".equals(param)) {
                paramValue = user.getCompanyId();
            }
            String sql = userGroup.getIsql().replaceAll("\\$\\{" + param + "\\}", paramValue);
            Query query = entityManager.createNativeQuery(sql);//直接通过sql语句进行执行，不需要某个dao来执行
            query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.aliasToBean(User.class));
            users.addAll(query.getResultList());
        }
        return users;
    }

    /**
     * 提交审核
     * @param taskInstance
     * @param companyId
     */
    public void commit(ProcTaskInstance taskInstance, String companyId) {
        //查询流程实例
        String processId = taskInstance.getProcessId();//流程实例id,也是业务id
        ProcInstance procInstance = procInstanceDao.findById(processId).get();
        //设置流程状态
        procInstance.setProcessState(taskInstance.getHandleType());
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(processId).singleResult();
        User user = feignClientService.getUserInfoByUserId(taskInstance.getHandleUserId());
        if ("2".equals(taskInstance.getHandleType())){
            //审批通过，完成当前的任务
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            taskService.complete(task.getId());
            Task next = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
            if (next != null){
                //则设置该流程实例的状态为审批中
                List<User> users = findCurrUsers(next, user);//获取审批的用户
                String usernames = "", userIdS = "";
                for (User user1 : users) {
                    usernames += user1.getUsername() + " ";
                    userIdS += user1.getId();
                }
                procInstance.setProcCurrNodeUserId(userIdS);
                procInstance.setProcCurrNodeUserName(usernames);
                procInstance.setProcessState("1");//审批中
            }else {
                //否则设置为审批通过,即审批结束
                procInstance.setProcessState("2");
            }
        }else {
            //审批不通过，则删除当前流程实例
            runtimeService.deleteProcessInstance(processInstance.getId(),taskInstance.getHandleOpinion());
        }
        //执行保存或更新
        procInstanceDao.save(procInstance);
        taskInstance.setTaskId(idWorker.nextId()+"");
        taskInstance.setHandleUserName(user.getUsername());
        taskInstance.setHandleTime(new Date());
        procTaskInstanceDao.save(taskInstance);
    }
}
