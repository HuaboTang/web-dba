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
        final List<ColumnDTO> columns = dataSourceMetaDataHelper.columns("WEB_DEV", "import_test", connection);
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
        final Connection connection = dataSourceConnectionManager.getConnection(new DataSourceDTO()
                .setId(20240315)
                .setUrl("jdbc:dm://10.207.136.10:5236")
                .setType(DataSourceTypeEnum.DM)
                .setUsername("SYSDBA")
                .setPassword("z#xXkSs47hE3"));
        return connection;
    }
}