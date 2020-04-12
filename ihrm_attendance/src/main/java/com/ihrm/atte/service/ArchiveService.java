package com.ihrm.atte.service;

import com.alibaba.fastjson.JSONObject;
import com.ihrm.atte.dao.*;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.atte.entity.ArchiveMonthly;
import com.ihrm.domain.atte.entity.ArchiveMonthlyInfo;
import com.ihrm.domain.atte.bo.AtteReportMonthlyBO;
import com.ihrm.domain.atte.vo.ArchiveInfoVO;
import com.ihrm.domain.atte.vo.ArchiveItemVO;
import com.ihrm.domain.atte.vo.ArchiveVO;
import com.ihrm.domain.atte.vo.ReportVO;
import com.ihrm.domain.system.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class ArchiveService {

	@Autowired
	private AttendanceDao attendanceDao;

	@Autowired
	private ArchiveMonthlyDao atteArchiveMonthlyDao;

	@Autowired
	private ArchiveMonthlyInfoDao archiveMonthlyInfoDao;


	@Autowired
	private UserDao userDao;

	@Autowired
	private IdWorker idWorkker;

	/**
	 * 考勤归档
	 * @param archiveDate
	 * @param companyId
	 */
	public void saveArchive(String archiveDate, String companyId) {
		//获取企业下的所有用户
		List<User> userList = userDao.findByCompanyId(companyId);
		//主表归档ArchiveMonthly
		ArchiveMonthly archiveMonthly = new ArchiveMonthly();
		archiveMonthly.setId(idWorkker.nextId()+"");
		archiveMonthly.setCompanyId(companyId);
		archiveMonthly.setArchiveYear(archiveDate.substring(0,4));
		archiveMonthly.setArchiveMonth(archiveDate.substring(5));
		archiveMonthly.setIsArchived(0);
		//详情表归档

		List<ArchiveMonthlyInfo> list = new ArrayList<>();

		for (User user : userList) {
			ArchiveMonthlyInfo info = new ArchiveMonthlyInfo(user);
			//统计每个用户的考勤记录
			Map map = attendanceDao.statisByUser(user.getId(),archiveDate+"%");
			info.setStatisData(map);
			info.setId(idWorkker.nextId()+"");
			info.setAtteArchiveMonthlyId(archiveMonthly.getId());
			archiveMonthlyInfoDao.save(info);
		}
		//总人数
		archiveMonthly.setTotalPeopleNum(userList.size());
		archiveMonthly.setFullAttePeopleNum(userList.size());
		atteArchiveMonthlyDao.save(archiveMonthly);
	}

}
