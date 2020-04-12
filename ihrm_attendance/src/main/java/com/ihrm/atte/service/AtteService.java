package com.ihrm.atte.service;


import com.ihrm.atte.dao.*;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.utils.DateUtil;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.atte.bo.*;
import com.ihrm.domain.atte.entity.ArchiveMonthlyInfo;
import com.ihrm.domain.atte.entity.Attendance;


import com.ihrm.domain.atte.entity.CompanySettings;
import com.ihrm.domain.system.User;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AtteService  {

	@Autowired
	private IdWorker idWorker;

    @Autowired
    private AttendanceDao attendanceDao;

    @Autowired
    private DeductionDictDao deductionDictDao;

	
    @Autowired
    private UserDao userDao;

    @Autowired
    private AttendanceConfigDao attendanceConfigDao;

    @Autowired
    private CompanySettingsDao companySettingsDao;

    /**
     * 分页查询企业的考勤数据
     * @param companyId
     * @param page
     * @param pageSize
     * @return
     */
    public Map findPage(String companyId, int page, int pageSize)throws Exception {
        //获取考勤月
        CompanySettings companySettings = companySettingsDao.findById(companyId).get();
        String dataMonth = companySettings.getDataMonth();
        //分页查询用户信息
        Page<User> userPage = userDao.findPage(companyId, new PageRequest(page - 1, pageSize));
        List<AtteItemBO> list = new ArrayList<>();
        for (User user : userPage.getContent()) {
            AtteItemBO bo = new AtteItemBO();
            BeanUtils.copyProperties(user,bo);
            List<Attendance> attendanceList = new ArrayList<>();
            //获取考勤月的天数
            String[] days = DateUtil.getDaysByYearMonth(dataMonth);
            for (String day : days) {
                //获取当天的考勤
                Attendance attendance = attendanceDao.findByUserIdAndDay(user.getId(), day);
                if (attendance == null){
                    attendance = new Attendance();
                    attendance.setId(idWorker.nextId()+"");
                    attendance.setDay(dataMonth);
                    attendance.setAdtStatu(2);//如果当天不存在考勤记录,旷工
                }
                attendanceList.add(attendance);
            }
            bo.setAttendanceRecord(attendanceList);
            list.add(bo);
        }
        PageResult pr = new PageResult(userPage.getTotalElements(),list);
        Map map = new HashMap();
        map.put("data",pr);//1.数据,分页对象
        map.put("tobeTaskCount",0);//2.待处理的考勤数量
        map.put("monthOfReport",dataMonth.substring(6));//3.当前考勤的月份
        return map;
    }

    /**
     * 保存用户考勤信息
     * @param attendance
     */
    public void save(Attendance attendance) {
        Attendance one = attendanceDao.findByUserIdAndDay(attendance.getUserId(), attendance.getDay());
        if (one == null){
            attendance.setId(idWorker.nextId()+"");
        }else {
            attendance.setId(one.getId());
        }
        attendanceDao.save(attendance);
    }

    /**
     * 获取企业当月用户的未归档报表
     * @param companyId
     * @param atteDate
     * @return
     */
    public List<ArchiveMonthlyInfo> getReport(String companyId, String atteDate) {
        List<ArchiveMonthlyInfo> list = new ArrayList<>();
        //获取企业下的所有用户
        List<User> userList = userDao.findByCompanyId(companyId);
        for (User user : userList) {
            ArchiveMonthlyInfo info = new ArchiveMonthlyInfo(user);
            //统计每个用户的考勤记录
            Map map = attendanceDao.statisByUser(user.getId(),atteDate+"%");
            info.setStatisData(map);
            list.add(info);
        }
        return list;
    }

    /**
     * 新建报表
     * @param yearMonth
     * @param companyId
     */
    public void newReports(String yearMonth, String companyId) {
        //根据企业id查询考勤设置信息
        CompanySettings companySettings = companySettingsDao.findById(companyId).get();
        companySettings.setDataMonth(yearMonth);
        companySettingsDao.save(companySettings);
    }
}
