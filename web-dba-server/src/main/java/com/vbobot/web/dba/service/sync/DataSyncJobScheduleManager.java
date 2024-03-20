package com.vbobot.web.dba.service.sync;

import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Bobo
 * @date 2024/3/20
 */
@Component
@Scope("singleton")
public class DataSyncJobScheduleManager {
    @Resource TaskScheduler syncDataTaskScheduler;

    private static final Map<Integer, ScheduledFuture<?>> FUTURE_HOLDER = Maps.newHashMap();

    public synchronized void initSchedule(DataSyncJobDTO syncJob, Runnable runnable) {
        if (FUTURE_HOLDER.containsKey(syncJob.getId())) {
            FUTURE_HOLDER.get(syncJob.getId()).cancel(true);
        }
        final ScheduledFuture<?> schedule = syncDataTaskScheduler.schedule(runnable, new CronTrigger(syncJob.getCron()));
        FUTURE_HOLDER.put(syncJob.getId(), schedule);
    }
}
