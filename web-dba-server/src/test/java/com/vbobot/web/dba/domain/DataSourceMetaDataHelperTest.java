package com.vbobot.web.dba.domain;

import com.vbobot.web.dba.service.datasource.DataSourceDTO;
import com.vbobot.web.dba.service.datasource.DataSourceTypeEnum;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/15
 */
class DataSourceMetaDataHelperTest {

    @Test
    void columns() throws Exception {
        final Connection connection = getConnection();
        DataSourceMetaDataHelper dataSourceMetaDataHelper = new DataSourceMetaDataHelper();
        final List<ColumnDTO> columns = dataSourceMetaDataHelper.columns("web_dba", "sync_source", connection);
        System.out.println(columns);
    }

    @Test
    void schemas() throws SQLException {
        final Connection connection = getConnection();

        DataSourceMetaDataHelper dataSourceMetaDataHelper = new DataSourceMetaDataHelper();
        final List<String> schemas = dataSourceMetaDataHelper.schemas(connection);
        System.out.println(schemas);
    }

    @Test
    void tables() throws SQLException {
        final Connection connection = getConnection();

        DataSourceMetaDataHelper dataSourceMetaDataHelper = new DataSourceMetaDataHelper();
        final List<TableDTO> tables = dataSourceMetaDataHelper.tables(connection);
        System.out.println(tables);
    }

    private static Connection getConnection() throws SQLException {
        final DataSourceConnectionManager dataSourceConnectionManager = new DataSourceConnectionManager();
        return dataSourceConnectionManager.getConnection(new DataSourceDTO()
                .setId(20240315)
                .setUrl("jdbc:mysql://10.0.80.173:3306/web_dba?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowMultiQueries=true&allowPublicKeyRetrieval=true")
                .setType(DataSourceTypeEnum.MYSQL)
                .setUsername("root")
                .setPassword("Gzfin@2901"));
    }
}