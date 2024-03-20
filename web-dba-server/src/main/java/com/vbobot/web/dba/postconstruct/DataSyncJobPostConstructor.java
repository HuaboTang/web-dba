package com.vbobot.web.dba.postconstruct;

import com.vbobot.web.dba.service.sync.DataSyncService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Bobo
 * @date 2024/3/20
 */
@Component
public class DataSyncJobPostConstructor {
    @Resource DataSyncService dataSyncService;

    @PostConstruct
    public void init() {
        dataSyncService.initJob();
    }
}
