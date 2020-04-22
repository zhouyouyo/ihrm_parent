package com.ihrm.audit.dao;

import com.ihrm.audit.entity.ProcTaskInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author itcast
 */
public interface ProcTaskInstanceDao extends JpaRepository<ProcTaskInstance,String>, JpaSpecificationExecutor<ProcTaskInstance> {
	/**
	 * 根据流程id查询
	 * 展示每个节点数据
	 *
	 */
	List<ProcTaskInstance> findByProcessId(String processId);
}
