package com.cq.accountmanger.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.cq.accountmanger.domain.FakerDataEntity;
import com.cq.accountmanger.mapper.FakerDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ExportExcelService {
    @Resource
    private FakerDataMapper fakerDataMapper;

    public void exportBigData(String filePath) {
        int pageSize = 10000;
        int currentPage = 1;
        List<FakerDataEntity> data;
        ExcelWriter excelWriter = EasyExcel.write(filePath, FakerDataEntity.class).build();
        // 写入数据到第一个Sheet，并指定写入的起始行
        WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();
        for (int i = 0; i < 100; i++) {
            log.info("pageSize=====" + pageSize + "=====currentPage=====" +currentPage);
            data = fakerData(pageSize, (currentPage - 1) * pageSize);
            // 写入文件
            excelWriter.write(data, writeSheet);
            currentPage++;
        }
        excelWriter.finish();
    }

    private List<FakerDataEntity> fakerData(int pageSize, int offset) {
        return fakerDataMapper.getFakerData(pageSize, offset);
    }
}
