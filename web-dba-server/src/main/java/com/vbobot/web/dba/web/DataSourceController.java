package com.vbobot.web.dba.web;

import com.vbobot.common.utils.web.vo.CommonResult;
import com.vbobot.web.dba.service.datasource.DataSourceDTO;
import com.vbobot.web.dba.service.datasource.DataSourceService;
import com.vbobot.web.dba.service.datasource.TestConnectResultDTO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/11
 */
@RestController
@RequestMapping("/datasource")
public class DataSourceController {
    @Resource DataSourceService dataSourceService;

    @GetMapping("/all")
    public CommonResult<List<DataSourceDTO>> getAll() {
        return CommonResult.success(dataSourceService.getAll());
    }

    @PostMapping("/test/connection")
    public CommonResult<TestConnectResultDTO> testConnection(@RequestBody DataSourceDTO dataSource) {
        return CommonResult.success(dataSourceService.testConnect(dataSource));
    }

    @PostMapping("/save")
    public CommonResult<DataSourceDTO> save(@RequestBody DataSourceDTO dataSource) {
        return CommonResult.success(dataSourceService.save(dataSource));
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Integer id) {
        dataSourceService.delete(id);
        return CommonResult.success();
    }
}
