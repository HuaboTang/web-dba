package com.vbobot.web.dba.web;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.google.common.collect.Lists;
import com.vbobot.common.utils.web.vo.CommonResult;
import com.vbobot.web.dba.service.data.DataService;
import com.vbobot.web.dba.service.data.ExcelReadListener;
import com.vbobot.web.dba.service.data.ExecuteSqlResultDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@Slf4j
@RestController
@RequestMapping("/data")
public class DataController {
    @Resource DataService dataService;

    @GetMapping("/import/template/{dataSourceId}/{schema}/{table}")
    public String importTemplate(@PathVariable("dataSourceId") Integer dataSourceId,
                                 @PathVariable("schema") String schema,
                                 @PathVariable("table") String table) {
        return "hello";
    }

    @PostMapping("/import/{dataSourceId}/{schema}/{table}")
    public CommonResult<Void> importData(@PathVariable("dataSourceId") Integer dataSourceId,
                                   @PathVariable("schema") String schema,
                                   @PathVariable("table") String table, MultipartFile file) {
        try {
            final ExcelReadListener readListener = new ExcelReadListener();
            final List<Object> objects = EasyExcel.read(file.getInputStream(), null, readListener)
                    .sheet()
                    .head(Lists.newArrayList())
                    .doReadSync();
            dataService.importData(readListener.getExcelContent(), dataSourceId, schema, table);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return CommonResult.success();
    }

    @PostMapping("/execute/sql")
    public CommonResult<ExecuteSqlResultDTO> executeSql(@RequestBody ExecuteSqlParamVO param) {
        final ExecuteSqlResultDTO result = dataService.executeSql(param.getSql(), param.getDataSourceId());
        log.info("execute result:{}", result);
        return CommonResult.success(result);
    }
}
