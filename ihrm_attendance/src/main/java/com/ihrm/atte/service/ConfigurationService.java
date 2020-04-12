package com.ihrm.atte.service;

import com.ihrm.atte.dao.*;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.atte.entity.*;
import com.ihrm.domain.atte.enums.DeductionEnum;
import com.ihrm.domain.atte.enums.LeaveTypeEnum;
import com.ihrm.domain.atte.vo.ConfigVO;
import com.ihrm.domain.atte.vo.ExtDutyVO;
import com.ihrm.domain.atte.vo.ExtWorkVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;


@Service
public class ConfigurationService{

    @Autowired
    private AttendanceConfigDao attendanceConfigDao;

    @Autowired
    private LeaveConfigDao leaveConfigDao;

    @Autowired
    private DeductionDictDao deductionDictDao;

    @Autowired
    private ExtraDutyConfigDao extraDutyConfigDao;

    @Autowired
    private ExtraDutyRuleDao extraDutyRuleDao;

    @Autowired
    private DayOffConfigDao dayOffConfigDao;

    @Autowired
    private IdWorker idWorker;

    /**
     * 根据企业id和部门id获取考情配置信息
     * @param companyId
     * @param departmentId
     * @return
     */
    public AttendanceConfig getAtteConfig(String companyId,String departmentId){
        return attendanceConfigDao.findByCompanyIdAndDepartmentId(companyId, departmentId);
    }

    //保存或更新出勤设置
    public void saveAtteConfig(AttendanceConfig atteConfig) {
        AttendanceConfig vo = attendanceConfigDao.findByCompanyIdAndDepartmentId
                (atteConfig.getCompanyId(), atteConfig.getDepartmentId());
        if (vo != null){
            atteConfig.setId(vo.getId());
        }else {
            atteConfig.setId(idWorker.nextId()+"");
        }
        attendanceConfigDao.save(atteConfig);
    }
}
