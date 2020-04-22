package com.ihrm.audit.dao;

import com.ihrm.audit.entity.ProcInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author itcast
 */
public interface ProcInstanceDao extends JpaRepository<ProcInstance,String>,
		JpaSpecificationExecutor<ProcInstance> {
}
