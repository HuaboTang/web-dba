package com.vbobot.web.dba.service.data;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@Slf4j
@Service
public class DataServiceImpl implements DataService {
    @Resource DataSourceRepository dataSourceRepository;
    @Resource DataSourceConnectionManager dataSourceConnectionManager;
    @Resource DataSourceMetaDataHelper metaDataHelper;

    @SneakyThrows
    @Override
    public void importData(ExcelContent excelContent, Integer dataSourceId, String schema, String table) {
        final Iterator<List<Object>> listIterator = excelContent.rowValues();
        while (listIterator.hasNext()) {
            List<Object> row = listIterator.next();
            System.out.println(row);
        }

        final Optional<DataSourceDO> oDataSource = dataSourceRepository.findById(dataSourceId);
        Assert.isTrue(oDataSource.isPresent(), "数据库配置不存在");
        assert oDataSource.isPresent();

        final DataSourceDTO dataSource = DataSourceConvertor.INST.toDataSourceDTO(oDataSource.get());
        try (Connection connection = getConnection(dataSource)) {
            DataInserter dataInsert = new DataInserter(connection, excelContent, schema, table, getColumns(schema, table, dataSource));
            dataInsert.doInsert();
        }
    }

    @SneakyThrows
    @Override
    public ExecuteSqlResultDTO executeSql(String sql, Integer dataSourceId) {
        final Optional<DataSourceDO> oDataSource = dataSourceRepository.findById(dataSourceId);
        Assert.isTrue(oDataSource.isPresent(), "数据库配置不存在");
        assert oDataSource.isPresent();

        final DataSourceDTO dataSource = DataSourceConvertor.INST.toDataSourceDTO(oDataSource.get());
        try (Connection connection = getConnection(dataSource);
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            final ResultSetMetaData metaData = resultSet.getMetaData();

            final int columnCount = metaData.getColumnCount();
            final List<String> headers = Lists.newArrayList();
            for (int i = 1; i <= columnCount; i++) {
                final String columnName = metaData.getColumnName(i);
                headers.add(columnName);
            }

            final List<List<Object>> datas = Lists.newArrayList();
            while (resultSet.next()) {
                final List<Object> row = Lists.newArrayList();
                datas.add(row);
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getObject(i));
                }
            }
            return new ExecuteSqlResultDTO().setSuccess(true).setHeaders(headers).setDatas(datas);
        } catch (Exception e) {
            log.warn("execute sql throw error, sql:{}, message:{}", sql, e.getMessage(), e);
            return new ExecuteSqlResultDTO().setSuccess(false).setMessage(e.getMessage());
        }
    }

    private List<ColumnDTO> getColumns(String schema, String table, DataSourceDTO dataSource) throws SQLException {
        try (Connection connection = getConnection(dataSource)) {
            return metaDataHelper.columns(schema, table, connection);
        }
    }

    private Connection getConnection(DataSourceDTO dataSource) throws SQLException {
        return dataSourceConnectionManager.getConnection(dataSource);
    }
}
