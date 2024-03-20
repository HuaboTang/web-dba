package com.vbobot.web.dba.service.sync;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vbobot.common.utils.exception.Assert;
import com.vbobot.web.dba.dao.datasource.DataSourceDO;
import com.vbobot.web.dba.dao.datasource.DataSourceRepository;
import com.vbobot.web.dba.domain.ColumnDTO;
import com.vbobot.web.dba.domain.DataSourceConnectionManager;
import com.vbobot.web.dba.service.datasource.DataSourceConvertor;
import com.vbobot.web.dba.service.datasource.DataSourceDTO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Bobo
 * @date 2024/3/19
 */
@Slf4j
@Component
@Scope("prototype")
public class SyncTarget {
    public static final int BUFFER_SIZE = 500;
    @Resource DataSourceRepository dataSourceRepository;
    @Resource DataSourceConnectionManager dataSourceConnectionManager;

    private final DataSyncJobDTO syncJob;
    private List<ColumnDTO> dataColumns;
    private DataSourceDTO dataSource;
    private List<List<Object>> buffer;
    private List<Object> primaryKeyBuffer;
    private ColumnDTO primaryKey;
    private int primaryKeyIndex;

    public SyncTarget(DataSyncJobDTO syncJob) {
        this.syncJob = syncJob;
    }

    public void init() {
        final Optional<DataSourceDO> oDataSource = dataSourceRepository.findById(syncJob.getTargetDataSourceId());
        Assert.isTrue(oDataSource.isPresent(), "数据源不存在");
        assert oDataSource.isPresent();
        this.dataSource = DataSourceConvertor.INST.toDataSourceDTO(oDataSource.get());
    }

    @SneakyThrows
    public Object maxOrderByValue() {
        final String orderByColumn = syncJob.getOrderByColumn();
        final String sql = "select max(%s) from %s.%s".formatted(orderByColumn,
                syncJob.getTargetSchema(), syncJob.getTargetTable());
        try (final Connection connection = dataSourceConnectionManager.getConnection(dataSource)) {
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getObject(1);
            } else {
                return null;
            }
        }
    }

    public void setDataColumns(List<ColumnDTO> dataColumns) {
        this.dataColumns = dataColumns;
        this.primaryKey = dataColumns.stream()
                .filter(item -> item.getPrimaryKey() != null && item.getPrimaryKey())
                .findFirst()
                .orElse(null);
        Assert.notNull(primaryKey, "主键为空");
        this.primaryKeyIndex = dataColumns.indexOf(this.primaryKey);
    }

    public void syncData(List<Object> rowData) throws SQLException {
        if (dataColumns == null) {
            throw new IllegalStateException("数据列为空");
        }

        if (buffer == null) {
            buffer = Lists.newArrayListWithCapacity(BUFFER_SIZE);
        }
        if (primaryKeyBuffer == null) {
            primaryKeyBuffer = Lists.newArrayListWithCapacity(BUFFER_SIZE);
        }
        buffer.add(rowData);
        primaryKeyBuffer.add(rowData.get(primaryKeyIndex));

        if (buffer.size() >= BUFFER_SIZE) {
            flush();
        }
    }

    public void flush() throws SQLException {
        if (buffer == null || buffer.isEmpty()) {
            log.info("buffer is empty");
            return;
        }


        try (final Connection connection = dataSourceConnectionManager.getConnection(dataSource)) {
            connection.setAutoCommit(false);
            flush(connection);
            connection.commit();
        }
    }

    private void flush(Connection connection) throws SQLException {
        final List<List<Object>> needUpdateRows = Lists.newArrayList();

        flushInsertAndSeparateUpdates(connection, needUpdateRows);

        if (!needUpdateRows.isEmpty()) {
            flushUpdate(connection, needUpdateRows);
        }

        primaryKeyBuffer.clear();
        buffer.clear();
    }

    private void flushUpdate(Connection connection, List<List<Object>> needUpdateRows) throws SQLException {
        log.info("needUpdateRows:{}", needUpdateRows.size());

        final String columnValueSet = String.join(",", dataColumns.stream()
                .filter(c -> !c.getName().equalsIgnoreCase(primaryKey.getName()))
                .map(c -> c.getName() + " = ?")
                .toArray(String[]::new));
        final String updateSql = "update %s.%s set %s where %s = ?".formatted(
                syncJob.getTargetSchema(), syncJob.getTargetTable(),
                columnValueSet, primaryKey.getName());
        log.info("update-sql:{}", updateSql);

        try (final PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
            for (List<Object> objects : needUpdateRows) {
                final AtomicInteger setIndex = new AtomicInteger(1);
                for (int i = 0; i < objects.size(); i++) {
                    if (i == primaryKeyIndex) {
                        continue;
                    }
                    preparedStatement.setObject(setIndex.getAndIncrement(), objects.get(i));
                }
                preparedStatement.setObject(dataColumns.size(), objects.get(primaryKeyIndex));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private void flushInsertAndSeparateUpdates(Connection connection, List<List<Object>> needUpdateRows) throws SQLException {
        final String selectByIdIn = "select %s from %s.%s where %s in (%s)".formatted(
                primaryKey.getName(), syncJob.getTargetSchema(),
                syncJob.getTargetTable(), primaryKey.getName(),
                String.join(",", primaryKeyBuffer.stream().map(Object::toString).toArray(String[]::new)));

        final String insertSql = "insert into %s.%s (%s) values (%s)".formatted(
                syncJob.getTargetSchema(), syncJob.getTargetTable(),
                String.join(",", dataColumns.stream().map(ColumnDTO::getName).toArray(String[]::new)),
                String.join(",", dataColumns.stream().map(c -> "?").toArray(String[]::new)));
        try (final PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            final AtomicBoolean anyAddedBatch = new AtomicBoolean(false);
            final Set<Object> existsPrimaryKeys = findPrimaryKeys(connection, selectByIdIn);
            for (List<Object> objects : this.buffer) {
                final Object primaryKey = objects.get(primaryKeyIndex);
                if (existsPrimaryKeys.contains(primaryKey)) {
                    log.debug("{} exists, need update", primaryKey);
                    needUpdateRows.add(objects);
                    continue;
                }

                anyAddedBatch.set(true);
                for (int i = 1; i <= dataColumns.size(); i++) {
                    preparedStatement.setObject(i, objects.get(i - 1));
                }
                preparedStatement.addBatch();
            }

            if (anyAddedBatch.get()) {
                preparedStatement.executeBatch();
            }
        }
    }

    @NotNull
    private static Set<Object> findPrimaryKeys(Connection connection, String selectByIdIn) throws SQLException {
        final Set<Object> existsPrimaryKeys = Sets.newHashSet();
        final ResultSet resultSet = connection.createStatement().executeQuery(selectByIdIn);
        while (resultSet.next()) {
            existsPrimaryKeys.add(resultSet.getObject(1));
        }
        return existsPrimaryKeys;
    }
}
