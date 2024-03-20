package com.vbobot.web.dba.service.sync;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.vbobot.common.utils.exception.Assert;
import com.vbobot.web.dba.dao.datasource.DataSourceDO;
import com.vbobot.web.dba.dao.datasource.DataSourceRepository;
import com.vbobot.web.dba.domain.ColumnDTO;
import com.vbobot.web.dba.domain.DataSourceConnectionManager;
import com.vbobot.web.dba.domain.DataSourceMetaDataHelper;
import com.vbobot.web.dba.service.datasource.DataSourceConvertor;
import com.vbobot.web.dba.service.datasource.DataSourceDTO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Bobo
 * @date 2024/3/19
 */
@Component
@Scope("prototype")
public class SyncSource {
    @Resource DataSourceRepository dataSourceRepository;
    @Resource DataSourceMetaDataHelper dataSourceMetaDataHelper;
    @Resource DataSourceConnectionManager dataSourceConnectionManager;

    private final DataSyncJobDTO syncJob;
    private DataSourceDTO dataSource;
    private List<ColumnDTO> columns;
    private int orderByColumnIndex;

    public SyncSource(DataSyncJobDTO syncJob) {
        this.syncJob = syncJob;
    }

    public void init() {
        final Optional<DataSourceDO> oDataSource = dataSourceRepository.findById(syncJob.getSourceDataSourceId());
        Assert.isTrue(oDataSource.isPresent(), "数据源不存在");
        assert oDataSource.isPresent();

        this.dataSource = DataSourceConvertor.INST.toDataSourceDTO(oDataSource.get());
        this.columns = dataSourceMetaDataHelper.columns(
                this.syncJob.getSourceSchema(), this.syncJob.getSourceTable(),
                dataSourceConnectionManager.getDataSource(this.dataSource));
        for (int i = 0; i < this.columns.size(); i++) {
            if (this.columns.get(i).getName().toLowerCase().equals(syncJob.getOrderByColumn())) {
                this.orderByColumnIndex = i;
                break;
            }
        }
    }

    public List<ColumnDTO> columns() {
        return this.columns;
    }

    public Iterator<List<Object>> syncDatas(Object maxOrderByValue) {
        return new SyncDataIterator(maxOrderByValue);
    }

    private class SyncDataIterator extends AbstractIterator<List<Object>> {
        private Iterator<List<Object>> iterator = null;
        private Object lastOrderByValue;

        public SyncDataIterator(Object maxOrderByValue) {
            this.lastOrderByValue = maxOrderByValue;
        }

        @SneakyThrows
        @Nullable
        @Override
        protected List<Object> computeNext() {
            if (iterator == null) {
                final List<List<Object>> list = selectBatchData();
                if (CollectionUtils.isEmpty(list)) {
                    return endOfData();
                }
                iterator = list.iterator();
            }

            if (iterator.hasNext() || (iterator = selectBatchData().iterator()).hasNext()) {
                final List<Object> next = iterator.next();
                this.lastOrderByValue = next.get(orderByColumnIndex);
                return next;
            }
            return endOfData();
        }

        private List<List<Object>> selectBatchData() throws SQLException {
            String sql;
            if (lastOrderByValue == null) {
                sql = "select %s from %s.%s order by %s asc".formatted(
                        columns.stream().map(ColumnDTO::getName).collect(Collectors.joining(",")),
                        syncJob.getSourceSchema(), syncJob.getSourceTable(),
                        syncJob.getOrderByColumn());
            } else {
                sql = "select %s from %s.%s where %s > ? order by %s asc".formatted(
                        columns.stream().map(ColumnDTO::getName).collect(Collectors.joining(",")),
                        syncJob.getSourceSchema(), syncJob.getSourceTable(),
                        syncJob.getOrderByColumn(), syncJob.getOrderByColumn());
            }

            try (final Connection connection = dataSourceConnectionManager.getConnection(dataSource);
                 final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                if (lastOrderByValue != null) {
                    preparedStatement.setObject(1, lastOrderByValue);
                }
                final ResultSet resultSet = preparedStatement.executeQuery();
                final List<List<Object>> result = Lists.newArrayList();
                while (resultSet.next()) {
                    final List<Object> rowData = Lists.newArrayList();
                    for (int i = 0; i < columns.size(); i++) {
                        rowData.add(resultSet.getObject(i + 1));
                    }
                    result.add(rowData);
                }
                return result;
            }
        }
    }
}
