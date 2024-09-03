package com.cq.accountmanger.controller;

import com.cq.accountmanger.service.ExportExcelService;
import com.cq.commons.model.domain.ResultInfo;
import com.cq.commons.utils.ResultInfoUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("export")
public class ExportExcelController {
    @Resource
    private ExportExcelService exportExcelService;

    @Resource
    private HttpServletRequest request;

    @GetMapping("exportExcel")
    public ResultInfo exportExcel() throws FileNotFoundException {
        String filePath = "D:\\temp\\test.xlsx";
        exportExcelService.exportBigData(filePath);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), filePath);
    }
}
