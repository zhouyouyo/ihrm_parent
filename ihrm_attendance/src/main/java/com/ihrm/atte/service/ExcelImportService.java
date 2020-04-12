package com.ihrm.atte.service;

import com.ihrm.atte.dao.AttendanceConfigDao;
import com.ihrm.atte.dao.AttendanceDao;
import com.ihrm.atte.dao.UserDao;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.poi.ExcelImportUtil;
import com.ihrm.common.utils.DateUtil;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.atte.entity.Attendance;
import com.ihrm.domain.atte.entity.AttendanceConfig;
import com.ihrm.domain.atte.vo.AtteUploadVo;
import com.ihrm.domain.system.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Log4j2
@Service
public class ExcelImportService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AttendanceDao attendanceDao;

    @Autowired
    private AttendanceConfigDao attendanceConfigDao;

    @Autowired
    private IdWorker idWorker;

    @Value("attendance.workingDays")
    private String workingDays;

	@Value("attendance.holidays")
	private String holidays;

    /**
     * 将考勤表导入对应的企业中
     * @param file
     * @param companyId
     */
	public void importAttendanceExcel(MultipartFile file,String companyId) throws Exception{
        //转换file(Excel)为集合数据
        List<AtteUploadVo> atteUploadVos = new ExcelImportUtil<AtteUploadVo>(AtteUploadVo.class)
                .readExcel(file.getInputStream(), 1, 0);
        for (AtteUploadVo atteUploadVo : atteUploadVos) {
            //根据用户手机号查询用户
            User user = userDao.findByMobile(atteUploadVo.getMobile());
            //构造考勤数据
            Attendance attendance = new Attendance(atteUploadVo,user);
            String attDate = atteUploadVo.getAttDate();//考勤日
            attendance.setDay(attDate);//设置考勤日
            //判断是否休息
            if(holidays.contains(attDate)){
                attendance.setAdtStatu(23);//休息
            }else if(DateUtil.isWeekend(attDate) && !workingDays.contains(attDate)){
                attendance.setAdtStatu(23);//休息
            }else{//上班
                //获取考勤设置信息
                AttendanceConfig ac = attendanceConfigDao.
                        findByCompanyIdAndDepartmentId(companyId, user.getDepartmentId());
                //判断迟到早退状态
                //第一个参数 : 规定时间 , 第二参数 : 打卡时间
                if (!DateUtil.comparingDate(ac.getAfternoonStartTime(),atteUploadVo.getInTime())){//打卡时间晚于规定时间即为迟到
                    attendance.setAdtStatu(3);//迟到
                }else if(DateUtil.comparingDate(ac.getAfternoonEndTime(),atteUploadVo.getOutTime())){//打卡时间早于规定时间即为早退
                    attendance.setAdtStatu(4);//早退
                }else {
                    attendance.setAdtStatu(1);//正常
                }
            }
            //判断当前用户当月考勤是否已经记录
            Attendance one = attendanceDao.findByUserIdAndDay(user.getId(),attDate);
            if (one == null){
                attendance.setId(idWorker.nextId()+"");
                attendanceDao.save(attendance);
            }
        }
    }
}
