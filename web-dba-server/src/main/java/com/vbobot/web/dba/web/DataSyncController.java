package com.vbobot.web.dba.web;

import com.vbobot.common.utils.web.vo.CommonResult;
import com.vbobot.common.utils.web.vo.PagingResult;
import com.vbobot.web.dba.service.sync.DataSyncJobDTO;
import com.vbobot.web.dba.service.sync.DataSyncPagingParamDTO;
import com.vbobot.web.dba.service.sync.DataSyncService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author Bobo
 * @date 2024/3/18
 */
@RestController
@RequestMapping("/data/sync")
public class DataSyncController {
    @Resource DataSyncService dataSyncService;

    @PostMapping("/page")
    public CommonResult<PagingResult<DataSyncJobDTO>> pageDataSyncJobs(@RequestBody DataSyncPagingParamDTO param) {
        return CommonResult.success(dataSyncService.page(param));
    }

    @PostMapping("/check")
    public CommonResult<Boolean> checkDataSyncJob(@RequestBody DataSyncJobDTO param) {
        return CommonResult.success(dataSyncService.check(param));
    }

    @PostMapping("/save")
    public CommonResult<DataSyncJobDTO> saveDataSyncJob(@RequestBody DataSyncJobDTO param) {
        return CommonResult.success(dataSyncService.save(param));
    }

    @GetMapping("/{jobId}")
    public CommonResult<DataSyncJobDTO> get(@PathVariable("jobId") Integer jobId)  {
        return CommonResult.success(dataSyncService.get(jobId));
    }
}
