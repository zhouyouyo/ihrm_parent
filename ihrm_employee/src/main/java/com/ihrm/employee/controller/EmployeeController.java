package com.ihrm.employee.controller;

import com.alibaba.fastjson.JSON;
import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
//import com.ihrm.common.poi.ExcelExportUtil;
import com.ihrm.common.poi.ExcelExportUtil;
import com.ihrm.common.utils.BeanMapUtils;
import com.ihrm.common.utils.DownloadUtils;
import com.ihrm.domain.employee.*;
import com.ihrm.domain.employee.response.EmployeeReportResult;
import com.ihrm.employee.service.*;
import io.jsonwebtoken.Claims;
import net.sf.jasperreports.engine.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@RestController
@RequestMapping("/employees")
@CrossOrigin
public class EmployeeController extends BaseController {
    @Autowired
    private UserCompanyPersonalService userCompanyPersonalService;
    @Autowired
    private UserCompanyJobsService userCompanyJobsService;
    @Autowired
    private ResignationService resignationService;
    @Autowired
    private TransferPositionService transferPositionService;
    @Autowired
    private PositiveService positiveService;
    @Autowired
    private ArchiveService archiveService;


    /**
     * 员工个人信息保存
     */
    @RequestMapping(value = "/{id}/personalInfo", method = RequestMethod.PUT)
    public Result savePersonalInfo(@PathVariable(name = "id") String uid, @RequestBody Map map) throws Exception {
        UserCompanyPersonal sourceInfo = BeanMapUtils.mapToBean(map, UserCompanyPersonal.class);
        if (sourceInfo == null) {
            sourceInfo = new UserCompanyPersonal();
        }
        sourceInfo.setUserId(uid);
        sourceInfo.setCompanyId(super.companyId);
        userCompanyPersonalService.save(sourceInfo);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 员工个人信息读取
     */
    @RequestMapping(value = "/{id}/personalInfo", method = RequestMethod.GET)
    public Result findPersonalInfo(@PathVariable(name = "id") String uid) throws Exception {
        UserCompanyPersonal info = userCompanyPersonalService.findById(uid);
        if (info == null) {
            info = new UserCompanyPersonal();
            info.setUserId(uid);
        }
        return new Result(ResultCode.SUCCESS, info);
    }

    /**
     * 员工岗位信息保存
     */
    @RequestMapping(value = "/{id}/jobs", method = RequestMethod.PUT)
    public Result saveJobsInfo(@PathVariable(name = "id") String uid, @RequestBody UserCompanyJobs sourceInfo) throws Exception {
        //更新员工岗位信息
        if (sourceInfo == null) {
            sourceInfo = new UserCompanyJobs();
            sourceInfo.setUserId(uid);
            sourceInfo.setCompanyId(super.companyId);
        }
        userCompanyJobsService.save(sourceInfo);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 员工岗位信息读取
     */
    @RequestMapping(value = "/{id}/jobs", method = RequestMethod.GET)
    public Result findJobsInfo(@PathVariable(name = "id") String uid) throws Exception {
        UserCompanyJobs info = userCompanyJobsService.findById(uid);
        if (info == null) {
            info = new UserCompanyJobs();
            info.setUserId(uid);
            info.setCompanyId(companyId);
        }
        return new Result(ResultCode.SUCCESS, info);
    }

    /**
     * 离职表单保存
     */
    @RequestMapping(value = "/{id}/leave", method = RequestMethod.PUT)
    public Result saveLeave(@PathVariable(name = "id") String uid, @RequestBody EmployeeResignation resignation) throws Exception {
        resignation.setUserId(uid);
        resignationService.save(resignation);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 离职表单读取
     */
    @RequestMapping(value = "/{id}/leave", method = RequestMethod.GET)
    public Result findLeave(@PathVariable(name = "id") String uid) throws Exception {
        EmployeeResignation resignation = resignationService.findById(uid);
        if (resignation == null) {
            resignation = new EmployeeResignation();
            resignation.setUserId(uid);
        }
        return new Result(ResultCode.SUCCESS, resignation);
    }

    /**
     * 导入员工
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public Result importDatas(@RequestParam(name = "file") MultipartFile attachment) throws Exception {
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 调岗表单保存
     */
    @RequestMapping(value = "/{id}/transferPosition", method = RequestMethod.PUT)
    public Result saveTransferPosition(@PathVariable(name = "id") String uid, @RequestBody EmployeeTransferPosition transferPosition) throws Exception {
        transferPosition.setUserId(uid);
        transferPositionService.save(transferPosition);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 调岗表单读取
     */
    @RequestMapping(value = "/{id}/transferPosition", method = RequestMethod.GET)
    public Result findTransferPosition(@PathVariable(name = "id") String uid) throws Exception {
        UserCompanyJobs jobsInfo = userCompanyJobsService.findById(uid);
        if (jobsInfo == null) {
            jobsInfo = new UserCompanyJobs();
            jobsInfo.setUserId(uid);
        }
        return new Result(ResultCode.SUCCESS, jobsInfo);
    }

    /**
     * 转正表单保存
     */
    @RequestMapping(value = "/{id}/positive", method = RequestMethod.PUT)
    public Result savePositive(@PathVariable(name = "id") String uid, @RequestBody EmployeePositive positive) throws Exception {
        positiveService.save(positive);
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 转正表单读取
     */
    @RequestMapping(value = "/{id}/positive", method = RequestMethod.GET)
    public Result findPositive(@PathVariable(name = "id") String uid) throws Exception {
        EmployeePositive positive = positiveService.findById(uid);
        if (positive == null) {
            positive = new EmployeePositive();
            positive.setUserId(uid);
        }
        return new Result(ResultCode.SUCCESS, positive);
    }

    /**
     * 历史归档详情列表
     */
    @RequestMapping(value = "/archives/details", method = RequestMethod.GET)
    public Result archives(@RequestParam(name = "month") String month, @RequestParam(name = "type") Integer type) throws Exception {
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 归档更新
     */
    @RequestMapping(value = "/archives/{month}", method = RequestMethod.PUT)
    public Result saveArchives(@PathVariable(name = "month") String month) throws Exception {
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 历史归档列表
     */
    @RequestMapping(value = "/archives", method = RequestMethod.GET)
    public Result findArchives(@RequestParam(name = "pagesize") Integer pagesize, @RequestParam(name = "page") Integer page, @RequestParam(name = "year") String year) throws Exception {
        Map map = new HashMap();
        map.put("year", year);
        map.put("companyId", companyId);
        Page<EmployeeArchive> searchPage = archiveService.findSearch(map, page, pagesize);
        PageResult<EmployeeArchive> pr = new PageResult(searchPage.getTotalElements(), searchPage.getContent());
        return new Result(ResultCode.SUCCESS, pr);
    }

    @RequestMapping(value = "/export/{month}", method = RequestMethod.GET)
    public void export(@PathVariable String month) throws Exception {

        //1.获取报表数据
        List<EmployeeReportResult> list = userCompanyPersonalService.findByReport(companyId, month);
        Collections.sort(list, new Comparator<EmployeeReportResult>() {
            @Override
            public int compare(EmployeeReportResult o1, EmployeeReportResult o2) {
                return Integer.parseInt(o1.getUserId()) - Integer.parseInt(o2.getUserId());
            }
        });
        //2.构造Excel
        //创建工作簿
//        Workbook wb = new XSSFWorkbook();
        //使用该对象模拟读取百万数据导入导出，当堆内存中的对象个数达到100之后就将多余的对象保存到磁盘中的临时文件中
        SXSSFWorkbook wb = new SXSSFWorkbook(120);
        //构造sheet
        Sheet sheet = wb.createSheet();
        //创建行
        //标题
        String[] titles = "编号,姓名,手机,最高学历,国家地区,护照号,籍贯,生日,属相,入职时间,离职类型,离职原因,离职时间".split(",");
        //处理标题

        Row row = sheet.createRow(0);

        int titleIndex = 0;
        for (String title : titles) {
            Cell cell = row.createCell(titleIndex++);
            cell.setCellValue(title);
        }

        int rowIndex = 1;
        Cell cell = null;
        for (int i = 0; i < 5000; i++) {
            for (EmployeeReportResult employeeReportResult : list) {
                row = sheet.createRow(rowIndex++);
                // 编号,
                cell = row.createCell(0);
                cell.setCellValue(employeeReportResult.getUserId());
                // 姓名,
                cell = row.createCell(1);
                cell.setCellValue(employeeReportResult.getUsername());
                // 手机,
                cell = row.createCell(2);
                cell.setCellValue(employeeReportResult.getMobile());
                // 最高学历,
                cell = row.createCell(3);
                cell.setCellValue(employeeReportResult.getTheHighestDegreeOfEducation());
                // 国家地区,
                cell = row.createCell(4);
                cell.setCellValue(employeeReportResult.getNationalArea());
                // 护照号,
                cell = row.createCell(5);
                cell.setCellValue(employeeReportResult.getPassportNo());
                // 籍贯,
                cell = row.createCell(6);
                cell.setCellValue(employeeReportResult.getNativePlace());
                // 生日,
                cell = row.createCell(7);
                cell.setCellValue(employeeReportResult.getBirthday());
                // 属相,
                cell = row.createCell(8);
                cell.setCellValue(employeeReportResult.getZodiac());
                // 入职时间,
                cell = row.createCell(9);
                cell.setCellValue(employeeReportResult.getTimeOfEntry());
                // 离职类型,
                cell = row.createCell(10);
                cell.setCellValue(employeeReportResult.getTypeOfTurnover());
                // 离职原因,
                cell = row.createCell(11);
                cell.setCellValue(employeeReportResult.getReasonsForLeaving());
                // 离职时间
                cell = row.createCell(12);
                cell.setCellValue(employeeReportResult.getResignationTime());
            }
        }
        //3.完成下载
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        new DownloadUtils().download(os, response, month + ".xlsx");
        System.out.println("nihao");
    }

    /**
     * 根据模板导出
     * @param month
     * @throws Exception
     */
    /*@RequestMapping(value = "/export/{month}", method = RequestMethod.GET)
    public void export(@PathVariable String month) throws Exception {
        List<EmployeeReportResult> list = userCompanyPersonalService.findByReport(companyId,month);
        Collections.sort(list, new Comparator<EmployeeReportResult>() {
            @Override
            public int compare(EmployeeReportResult o1, EmployeeReportResult o2) {
                return Integer.parseInt(o1.getUserId())-Integer.parseInt(o2.getUserId());
            }
        });
        //加载模板文件
        Resource resource =  new ClassPathResource("excel-template/hr-demo.xlsx");
        FileInputStream fis= new FileInputStream(resource.getFile());
        //通过工具类实现导出功能
        new ExcelExportUtil<EmployeeReportResult>(EmployeeReportResult.class,2,2)
                .export(response,fis,list,month+"人事报表.xlsx");
        //通过模板输入流得到工作簿
//        Workbook wb = new XSSFWorkbook(fis);
//        //获取第一个sheet对象
//        Sheet sheet = wb.getSheetAt(0);
//        //得到第一行的样式
//        Row firstRow = sheet.getRow(0);
//        Cell firstRowCell = firstRow.getCell(0);
//        firstRowCell.setCellValue(month+"人事报表");
//        //抽取公共样式
//        Row row = sheet.getRow(2);
//        CellStyle[] cellStyles=new CellStyle[row.getLastCellNum()];
//        for (int i = 0; i < row.getLastCellNum(); i++) {
//            Cell cell = row.getCell(i);
//            CellStyle cellStyle = cell.getCellStyle();
//            cellStyles[i] = cellStyle;
//        }
//        //拼装数据到单元格中
//        int rowIndex = 2;
//        Cell cell=null;
//        //获取报表数据
//
//        for (EmployeeReportResult employeeReportResult : list) {
//            row = sheet.createRow(rowIndex++);
//            // 编号,
//            cell = row.createCell(0);
//            cell.setCellValue(employeeReportResult.getUserId());
//            cell.setCellStyle(cellStyles[0]);
//            // 姓名,
//            cell = row.createCell(1);
//            cell.setCellValue(employeeReportResult.getUsername());
//            cell.setCellStyle(cellStyles[1]);
//            // 手机,
//            cell = row.createCell(2);
//            cell.setCellValue(employeeReportResult.getMobile());
//            cell.setCellStyle(cellStyles[2]);
//            // 最高学历,
//            cell = row.createCell(3);
//            cell.setCellValue(employeeReportResult.getTheHighestDegreeOfEducation());
//            cell.setCellStyle(cellStyles[3]);
//            // 国家地区,
//            cell = row.createCell(4);
//            cell.setCellValue(employeeReportResult.getNationalArea());
//            cell.setCellStyle(cellStyles[4]);
//            // 护照号,
//            cell = row.createCell(5);
//            cell.setCellValue(employeeReportResult.getPassportNo());
//            cell.setCellStyle(cellStyles[5]);
//            // 籍贯,
//            cell = row.createCell(6);
//            cell.setCellValue(employeeReportResult.getNativePlace());
//            cell.setCellStyle(cellStyles[6]);
//            // 生日,
//            cell = row.createCell(7);
//            cell.setCellValue(employeeReportResult.getBirthday());
//            cell.setCellStyle(cellStyles[7]);
//            // 属相,
//            cell = row.createCell(8);
//            cell.setCellValue(employeeReportResult.getZodiac());
//            cell.setCellStyle(cellStyles[8]);
//            // 入职时间,
//            cell = row.createCell(9);
//            cell.setCellValue(employeeReportResult.getTimeOfEntry());
//            cell.setCellStyle(cellStyles[9]);
//            // 离职类型,
//            cell = row.createCell(10);
//            cell.setCellValue(employeeReportResult.getTypeOfTurnover());
//            cell.setCellStyle(cellStyles[10]);
//            // 离职原因,
//            cell = row.createCell(11);
//            cell.setCellValue(employeeReportResult.getReasonsForLeaving());
//            cell.setCellStyle(cellStyles[11]);
//            // 离职时间
//            cell = row.createCell(12);
//            cell.setCellValue(employeeReportResult.getResignationTime());
//            cell.setCellStyle(cellStyles[12]);
//        }
//        //3.完成下载
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        wb.write(os);
//        new DownloadUtils().download(os,response,month+".xlsx");
//        System.out.println("nihao");
    }*/

    /**
     *     * 打印员工pdf报表x
     *    
     */
    @RequestMapping(value = "/{id}/pdf", method = RequestMethod.GET)
    public void pdf(@PathVariable String id) throws IOException {
        //引入Jasper文件
        Resource resource = new ClassPathResource("template/profile.jasper");
        FileInputStream fis = new FileInputStream(resource.getFile());
        ServletOutputStream os = response.getOutputStream();
        try {
           Map map = new HashMap<>();
           //获取员工详情信息
            UserCompanyPersonal userCompanyPersonal = userCompanyPersonalService.findById(id);
            //获取员工岗位信息
            UserCompanyJobs userCompanyJobs = userCompanyJobsService.findById(id);
            //获取员工头像信息http://q85ylm9bq.bkt.clouddn.com/1063705989926227968
            String staffPhoto = "http://q85ylm9bq.bkt.clouddn.com/"+id;
            Map<String, Object> map1 = BeanMapUtils.beanToMap(userCompanyPersonal);
            Map<String, Object> map2 = BeanMapUtils.beanToMap(userCompanyJobs);
            map.putAll(map1);
            map.putAll(map2);
            map.put("staffPhoto",staffPhoto);
            JasperPrint jasperPrint = JasperFillManager.fillReport(fis,map, new JREmptyDataSource());
            //将JasperPrint以PDF的形式输出
            JasperExportManager.exportReportToPdfStream(jasperPrint,os);
        } catch (JRException e) {
            e.printStackTrace();
        }finally {
            os.close();
        }
    }
}
