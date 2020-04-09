package com.ihrm.social.service;

import com.alibaba.fastjson.JSON;
import com.ihrm.common.entity.Result;
import com.ihrm.common.utils.BeanMapUtils;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.employee.UserCompanyPersonal;
import com.ihrm.domain.social_security.*;
import com.ihrm.social.client.EmployeeFeignClient;
import com.ihrm.social.dao.ArchiveDao;
import com.ihrm.social.dao.ArchiveDetailDao;
import com.ihrm.social.dao.UserSocialSecurityDao;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ArchiveService {

	@Autowired
	private ArchiveDao archiveDao;

	@Autowired
	private ArchiveDetailDao archiveDetailDao;

	@Autowired
	private UserSocialSecurityDao userSocialSecurityDao;

	@Autowired
	private EmployeeFeignClient employeeFeignClient;

	@Autowired
	private UserSocialService userSocialService;

	@Autowired
	private PaymentItemService paymentItemService;

	@Autowired
	private IdWorker idWorker;

	/**
	 * 根据月份和企业id查询归档数据
	 * @param companyId
	 * @param yearMonth
	 * @return
	 */
    public Archive findArchive(String companyId, String yearMonth) {
		return archiveDao.findByCompanyIdAndYearsMonth(companyId, yearMonth);
	}

	/**
	 * 根据归档id查询归档明细
	 * @param id
	 * @return
	 */
	public List<ArchiveDetail> findAllDetailByArchiveId(String id) {
		return archiveDetailDao.findByArchiveId(id);
	}

	public List<ArchiveDetail> getReports(String yearMonth,String companyId) throws Exception {
		//查询用户的社保列表 (用户和基本社保数据)
		Page<Map> userSocialSecurityItemPage = userSocialSecurityDao.findPage(companyId,null);

		List<ArchiveDetail> list = new ArrayList<>();

		for (Map map : userSocialSecurityItemPage) {
			String userId = (String)map.get("userId");
			String mobile = (String)map.get("mobile");
			String username = (String)map.get("username");
			String departmentName = (String)map.get("departmentName");
			ArchiveDetail vo = new ArchiveDetail(userId,mobile,username,departmentName);
			vo.setTimeOfEntry((Date) map.get("timeOfEntry"));
			//获取个人信息
			Result personalResult = employeeFeignClient.findPersonalInfo(vo.getUserId());
			if (personalResult.isSuccess()) {
				UserCompanyPersonal userCompanyPersonal = JSON.parseObject(JSON.toJSONString(personalResult.getData()), UserCompanyPersonal.class);
				vo.setUserCompanyPersonal(userCompanyPersonal);
			}
			//社保相关信息
			getOtherData(vo, yearMonth);
			list.add(vo);
		}
		return list;
	}

	public void getOtherData(ArchiveDetail vo, String yearMonth) {

		UserSocialSecurity userSocialSecurity = userSocialService.findById(vo.getUserId());
		if(userSocialSecurity == null) {
			return;
		}

		BigDecimal socialSecurityCompanyPay = new BigDecimal(0);

		BigDecimal socialSecurityPersonalPay = new BigDecimal(0);

		List<CityPaymentItem> cityPaymentItemList = paymentItemService.findAllByCityId(userSocialSecurity.getProvidentFundCityId());

		for (CityPaymentItem cityPaymentItem : cityPaymentItemList) {
			if (cityPaymentItem.getSwitchCompany()) {
				BigDecimal augend;
				if (cityPaymentItem.getPaymentItemId().equals("4") && userSocialSecurity.getIndustrialInjuryRatio() != null) {
					augend = userSocialSecurity.getIndustrialInjuryRatio().multiply(userSocialSecurity.getSocialSecurityBase());
				} else {
					augend = cityPaymentItem.getScaleCompany().multiply(userSocialSecurity.getSocialSecurityBase());
				}
				BigDecimal divideAugend = augend.divide(new BigDecimal(100));
				socialSecurityCompanyPay = socialSecurityCompanyPay.add(divideAugend);
			}
			if (cityPaymentItem.getSwitchPersonal()) {
				BigDecimal augend = cityPaymentItem.getScalePersonal().multiply(userSocialSecurity.getSocialSecurityBase());
				BigDecimal divideAugend = augend.divide(new BigDecimal(100));
				socialSecurityPersonalPay = socialSecurityPersonalPay.add(divideAugend);
			}
		}

		vo.setSocialSecurity(socialSecurityCompanyPay.add(socialSecurityPersonalPay));
		vo.setSocialSecurityEnterprise(socialSecurityCompanyPay);
		vo.setSocialSecurityIndividual(socialSecurityPersonalPay);
		vo.setUserSocialSecurity(userSocialSecurity);
		vo.setSocialSecurityMonth(yearMonth);
		vo.setProvidentFundMonth(yearMonth);
	}

	/**
	 * 归档当月的数据
	 * @param yearMonth
	 * @param companyId
	 */
	public void archive(String yearMonth, String companyId) throws Exception{
		//构造用于归档的详情数据
		List<ArchiveDetail> details = this.getReports(yearMonth, companyId);
		//计算当月,企业与员工支出的所有社保金额
		BigDecimal enterMoney = new BigDecimal(0);
		BigDecimal personMoney = new BigDecimal(0);
		for (ArchiveDetail archiveDetail : details) {
			BigDecimal t1 = archiveDetail.getProvidentFundEnterprises() == null ? new BigDecimal(0): archiveDetail.getProvidentFundEnterprises();
			BigDecimal t2 = archiveDetail.getSocialSecurityEnterprise() == null ? new BigDecimal(0): archiveDetail.getSocialSecurityEnterprise();
			BigDecimal t3 = archiveDetail.getProvidentFundIndividual() == null ? new BigDecimal(0): archiveDetail.getProvidentFundIndividual();
			BigDecimal t4 = archiveDetail.getSocialSecurityIndividual() == null ? new BigDecimal(0): archiveDetail.getSocialSecurityIndividual();
			enterMoney = enterMoney.add(t1).add(t2);
			personMoney = enterMoney.add(t3).add(t4);
		}
		//查询当月是否已经归档
		Archive archive = this.findArchive(companyId, yearMonth);
		if (archive ==null){//不存在，就保存
			archive = new Archive(companyId,yearMonth);
			archive.setCompanyId(companyId);
			archive.setYearsMonth(yearMonth);
			archive.setId(idWorker.nextId()+"");
		}
		//4.如果存在已归档数据,覆盖
		archive.setEnterprisePayment(enterMoney);
		archive.setPersonalPayment(personMoney);
		archive.setTotal(enterMoney.add(personMoney));
		archiveDao.save(archive);
		for (ArchiveDetail archiveDetail : details) {
			archiveDetail.setId(idWorker.nextId() + "");
			archiveDetail.setArchiveId(archive.getId());
			archiveDetailDao.save(archiveDetail);
		}
	}

	/**
	 * 查询当年的历史归档数据
	 * @param companyId
	 * @param year
	 * @return
	 */
    public List<Archive> findArchiveByYear(String companyId, String year) {
		return archiveDao.findByCompanyIdAndYearsMonthLike(companyId,year+"%");
    }

	/**
	 * 根据用户id和年月查询社保归档详情
	 * @param userId
	 * @param yearMonth
	 * @return
	 */
	public ArchiveDetail findUserArchiveDetail(String userId, String yearMonth) {
		return archiveDetailDao.findByUserIdAndYearsMonth(userId, yearMonth);
	}
}
