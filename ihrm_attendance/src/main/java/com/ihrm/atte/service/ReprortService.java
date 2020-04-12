package com.ihrm.atte.service;


import com.ihrm.atte.dao.ArchiveMonthlyDao;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.domain.atte.entity.ArchiveMonthly;
import com.ihrm.domain.atte.entity.ReportItemVO;
import com.ihrm.domain.atte.bo.AtteReportMonthlyBO;
import com.ihrm.domain.atte.vo.ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReprortService {

    @Autowired
    private ArchiveMonthlyDao archiveMonthlyDao;

}
