package com.ihrm.social.service;

import com.ihrm.common.entity.PageResult;
import com.ihrm.domain.social_security.CompanySettings;
import com.ihrm.social.dao.CompanySettingsDao;
import com.ihrm.social.dao.UserSocialSecurityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class CompanySettingsService {
    @Autowired
    private CompanySettingsDao companySettingsDao;

    @Autowired
    private UserSocialSecurityDao userSocialSecurityDao;

    //根据企业id查询
	public CompanySettings findById(String companyId) {
		Optional<CompanySettings> optional = companySettingsDao.findById(companyId);
		return optional.isPresent() ? optional.get() : null;
	}

	//保存企业设置
	public void save(CompanySettings companySettings) {
		companySettings.setIsSettings(1);//已经完成当月设置
		companySettingsDao.save(companySettings);
	}

	/**
	 * 分页查询当前企业的所有员工社保信息
	 * @param page
	 * @param pageSize
	 * @param companyId
	 * @return
	 */
	public PageResult findAll(Integer page, Integer pageSize, String companyId) {
		Page<Map> one = userSocialSecurityDao.findPage(companyId, new PageRequest(page-1, pageSize));
		return new PageResult(one.getTotalElements(),one.getContent());
	}
}
