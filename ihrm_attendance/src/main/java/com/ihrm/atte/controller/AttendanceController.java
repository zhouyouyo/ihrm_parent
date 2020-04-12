package com.ihrm.atte.controller;

import com.ihrm.atte.service.ArchiveService;
import com.ihrm.atte.service.AtteService;
import com.ihrm.atte.service.ExcelImportService;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.atte.entity.ArchiveMonthlyInfo;
import com.ihrm.domain.atte.entity.Attendance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendances")
public class AttendanceController extends BaseController {

    @Autowired
    private ExcelImportService excelImportService;

    @Autowired
    private AtteService atteService;

    @Autowired
    private ArchiveService archiveService;


    /**
     * 导入用户考勤
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/import",method = RequestMethod.POST)
    public Result importAttendances(@RequestParam("file") MultipartFile file)throws Exception{
        excelImportService.importAttendanceExcel(file,companyId);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 分页查询用户考勤列表
     * @param page
     * @param pagesize
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "",method = RequestMethod.GET)
    public Result atteList(int page,int pagesize)throws Exception{
        Map map = atteService.findPage(companyId,page,pagesize);
        return new Result(ResultCode.SUCCESS,map);
    }

    /**
     * 保存
     * @param attendance
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    public Result saveAtte(@RequestBody Attendance attendance)throws Exception{
        atteService.save(attendance);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 获取月报表归档数据
     * @param atteDate
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/reports" ,method = RequestMethod.GET)
    public Result reports(String atteDate)throws Exception{
        List<ArchiveMonthlyInfo> list= atteService.getReport(companyId,atteDate);
        return new Result(ResultCode.SUCCESS,list);
    }

    /**
     * 获取月报表归档数据
     * /attendances/archive/item?archiveDate=201907
     */
    @RequestMapping(value = "/archive/item" ,method = RequestMethod.GET)
    public Result item(String archiveDate){
        archiveService.saveArchive(archiveDate,companyId);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 获取月报表归档数据
     * http://localhost:8080/api/attendances/reports?atteDate=2020007
     */
    @RequestMapping(value = "/newReports" ,method = RequestMethod.GET)
    public Result newReports(String atteDate){
        atteService.newReports(atteDate,companyId);
        return new Result(ResultCode.SUCCESS);
    }
}
