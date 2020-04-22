package com.ihrm.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * (DepartmentApprover)实体类
 */
@Entity
@Table(name = "proc_user_group")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcUserGroup {
	private static final long serialVersionUID = -9084332495284489553L;
	@Id
	@Column(name ="id")
	private String id;
	@Column(name ="name")
	private String name;
	@Column(name ="param")
	private String param;
	@Column(name ="isql")
	private String isql;
}
