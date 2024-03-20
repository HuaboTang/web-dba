package com.vbobot.web.dba.service.sync;

import com.google.common.collect.Lists;
import com.vbobot.common.utils.data.PageableUtils;
import com.vbobot.common.utils.exception.Assert;
import com.vbobot.common.utils.web.vo.PagingResult;
import com.vbobot.web.dba.dao.datasource.DataSourceRepository;
import com.vbobot.web.dba.dao.sync.DataSyncJobDO;
import com.vbobot.web.dba.dao.sync.DataSyncJobRepository;
import com.vbobot.web.dba.domain.ColumnDTO;
import com.vbobot.web.dba.domain.DataSourceConnectionManager;
import com.vbobot.web.dba.domain.DataSourceMetaDataHelper;
import com.vbobot.web.dba.service.datasource.DataSourceConvertor;
import com.vbobot.web.dba.service.datasource.DataSourceDTO;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Bobo
 * @date 2024/3/18
 */
@Slf4j
@Service
public class DataSyncServiceImpl implements DataSyncService {
    @Resource DataSyncJobRepository dataSyncJobRepository;
    @Resource DataSourceRepository  dataSourceRepository;
    @Resource DataSourceMetaDataHelper dataSourceMetaDataHelper;
    @Resource DataSourceConnectionManager dataSourceConnectionManager;
    @Resource DataSyncJobScheduleManager dataSyncJobScheduleManager;

    @Resource SyncSourceFactory syncSourceFactory;

    @Resource PlatformTransactionManager platformTransactionManager;

    @Override
    public PagingResult<DataSyncJobDTO> page(DataSyncPagingParamDTO param) {
        final Page<DataSyncJobDO> pageResult = dataSyncJobRepository.findAll((root, query, criteriaBuilder) -> {
            final List<Predicate> predicates = Lists.newArrayList();
            if (param.getName() != null) {
                Predicate predicate = criteriaBuilder.like(root.get("name"), "%" + param.getName() + "%");
                predicates.add(predicate);
            }

            if (param.getTargetDataSourceId() != null) {
                Predicate predicate = criteriaBuilder.equal(root.get("targetDataSourceId"), param.getTargetDataSourceId());
                predicates.add(predicate);
            }

            if (param.getSourceDataSourceId() != null) {
                Predicate predicate = criteriaBuilder.equal(root.get("sourceDataSourceId"), param.getSourceDataSourceId());
                predicates.add(predicate);
            }

            if (predicates.isEmpty()) {
                return null;
            } else {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        }, PageableUtils.toPageable(param, Sort.by(Sort.Direction.DESC, "updateTime")));
        return PageableUtils.toPagingResult(pageResult, DataSyncJobConvertor.INST::toDto);
    }

    @Override
    public boolean check(DataSyncJobDTO param) {
        /* 要检查什么？
        * 1.  表存在不存在
        * 2. 表结构是不是不一致
        * 3. 有没有主键
        */
        final DataSourceDTO sourceDataSource = DataSourceConvertor.INST.toDataSourceDTO(
                dataSourceRepository.findById(param.getSourceDataSourceId()).orElse(null));
        final DataSourceDTO targetDataSource = DataSourceConvertor.INST.toDataSourceDTO(
                dataSourceRepository.findById(param.getTargetDataSourceId()).orElse(null));
        Assert.notNull(sourceDataSource, "源数据源不存在");
        Assert.notNull(targetDataSource, "目标数据源不存在");

        final DataSource sourceConnectionManagerDataSource = dataSourceConnectionManager.getDataSource(sourceDataSource);
        final DataSource targetConnectionManagerDataSource = dataSourceConnectionManager.getDataSource(targetDataSource);

        final List<ColumnDTO> sourceColumns = dataSourceMetaDataHelper.columns(
                param.getSourceSchema(), param.getSourceTable(), sourceConnectionManagerDataSource);
        final List<ColumnDTO> targetColumns = dataSourceMetaDataHelper.columns(
                param.getTargetSchema(), param.getTargetTable(), targetConnectionManagerDataSource);
        Assert.isTrue(isMatch(sourceColumns, targetColumns), "表结构不一致");
        final boolean sourceHasPrimaryKey = sourceColumns.stream()
                .anyMatch(item -> item.getPrimaryKey() != null && item.getPrimaryKey());
        final boolean targetHasPrimaryKey = targetColumns.stream()
                .anyMatch(item -> item.getPrimaryKey() != null && item.getPrimaryKey());
        Assert.isTrue(sourceHasPrimaryKey, "源表没有主键");
        Assert.isTrue(targetHasPrimaryKey, "目标表没有主键");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataSyncJobDTO save(DataSyncJobDTO param) {
        check(param);
        final DataSyncJobDO dataSyncJobDO = DataSyncJobConvertor.INST.toDO(param);
        dataSyncJobDO.setRunning(true);
        dataSyncJobDO.setUpdateTime(LocalDate.now());
        final DataSyncJobDO save = dataSyncJobRepository.save(dataSyncJobDO);
        final DataSyncJobDTO dto = DataSyncJobConvertor.INST.toDto(save);

        dataSyncJobScheduleManager.initSchedule(dto, createRunnable(dto));

        return dto;
    }

    @Override
    public void initJob() {
        log.info("init-data-sync-jobs");
        final List<DataSyncJobDO> allByRunning = dataSyncJobRepository.findAllByRunning(true);
        log.info("size: {}", allByRunning == null ? 0 : allByRunning.size());
        if (CollectionUtils.isEmpty(allByRunning)) {
            return;
        }

        allByRunning.forEach(job -> {
            final DataSyncJobDTO syncJob = DataSyncJobConvertor.INST.toDto(job);
            check(syncJob);
            dataSyncJobScheduleManager.initSchedule(syncJob, createRunnable(syncJob));
            log.info("add schedule:{}", syncJob);
        });
    }

    @Override
    public DataSyncJobDTO get(Integer jobId) {
        return DataSyncJobConvertor.INST.toDto(dataSyncJobRepository.findById(jobId).orElse(null));
    }

    private Runnable createRunnable(DataSyncJobDTO param) {
        return () -> this.doSync(param);
    }

    private void doSync(DataSyncJobDTO param) {
        log.info("do-sync, job:{}", param);
        final long begin = System.currentTimeMillis();
        try {
            final SyncSource syncSource = syncSourceFactory.create(param);
            final SyncTarget syncTarget = syncSourceFactory.createSyncTarget(param);

            Object maxOrderByValue = syncTarget.maxOrderByValue();
            Iterator<List<Object>> syncDatas = syncSource.syncDatas(maxOrderByValue);
            syncTarget.setDataColumns(syncSource.columns());
            final AtomicInteger count = new AtomicInteger(0);
            while (syncDatas.hasNext()) {
                final List<Object> rowData = syncDatas.next();
                syncTarget.syncData(rowData);
                count.incrementAndGet();
            }
            syncTarget.flush();
            updateLastRunStatus(param);
            log.info("do-sync, count:{}, cost:{}", count.get(), System.currentTimeMillis() - begin);
        } catch (Exception e) {
            log.error("同步数据失败, syncJob:{}, error:{}", param, e.getMessage(), e);
        }
    }

    private void updateLastRunStatus(DataSyncJobDTO param) {
        new TransactionTemplate(platformTransactionManager).execute(status -> {
            final Optional<DataSyncJobDO> oDataSyncJob = dataSyncJobRepository.findById(param.getId());
            final DataSyncJobDO dataSyncJob = oDataSyncJob.get();
            dataSyncJob.setLastRunTime(LocalDate.now());
            dataSyncJob.setLastRunSuccess(true);
            dataSyncJobRepository.save(dataSyncJob);
            return null;
        });
    }

    private boolean isMatch(List<ColumnDTO> sourceColumns, List<ColumnDTO> targetColumns) {
        final String source = sourceColumns.stream()
                .map(item -> item.getName() + "_" + item.getType())
                .collect(Collectors.joining(","));
        final String target = targetColumns.stream()
                .map(item -> item.getName() + "_" + item.getType())
                .collect(Collectors.joining(","));
        return source.equals(target);
    }
}
