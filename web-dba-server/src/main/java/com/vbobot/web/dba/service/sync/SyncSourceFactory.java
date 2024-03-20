package com.vbobot.web.dba.service.sync;

import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Bobo
 * @date 2024/3/19
 */
@Component
public class SyncSourceFactory {
    @Resource ApplicationContext applicationContext;

    public SyncSource create(DataSyncJobDTO syncJob) {
        final SyncSource bean = applicationContext.getBean(SyncSource.class, syncJob);
        bean.init();
        return bean;
    }

    public SyncTarget createSyncTarget(DataSyncJobDTO syncJob) {
        final SyncTarget bean = applicationContext.getBean(SyncTarget.class, syncJob);
        bean.init();
        return bean;
    }
}
