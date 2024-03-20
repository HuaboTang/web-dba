package com.vbobot.web.dba.domain;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@Component
public class DataSourceMetaDataHelper {

    public static final String MYSQL_PRODUCT_NAME = "mysql";

    @SneakyThrows
    public List<String> schemas(DataSource dataSource) {
        try (final Connection connection = dataSource.getConnection()) {
            return schemas(connection);
        }
    }

    @SneakyThrows
    public List<String> schemas(Connection connection) {
        final DatabaseMetaData metaData = connection.getMetaData();

        final ResultSet schemas = isMysql(metaData) ? connection.getMetaData().getCatalogs()
                : connection.getMetaData().getSchemas();
        if (!schemas.next()) {
            return Lists.newArrayList();
        }
        final List<String> result = Lists.newArrayList();
        String schemeColumnName = isMysql(metaData) ? "TABLE_CAT" : "TABLE_SCHEM";
        while (schemas.next()) {
            final String schemaName = schemas.getString(schemeColumnName);
            result.add(schemaName);
        }
        return result;
    }

    @SneakyThrows
    public List<TableDTO> tables(DataSource dataSource) {
        try (final Connection connection = dataSource.getConnection()) {
            return tables(connection);
        }
    }

    @SneakyThrows
    public List<TableDTO> tables(Connection connection) {
        final DatabaseMetaData metaData = connection.getMetaData();
        final ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        if (!tables.next()) {
            return Lists.newArrayList();
        }
        final List<TableDTO> result = Lists.newArrayList();
        while (tables.next()) {
            final String schema = isMysql(metaData)
                    ? tables.getString("TABLE_CAT") : tables.getString("TABLE_SCHEM");
            final String tableName = tables.getString("TABLE_NAME");
            result.add(new TableDTO().setSchema(schema).setTable(tableName));
        }
        return result;
    }

    private static boolean isMysql(DatabaseMetaData metaData) throws SQLException {
        final String databaseProductName = metaData.getDatabaseProductName();
        return databaseProductName.equalsIgnoreCase(MYSQL_PRODUCT_NAME);
    }

    @SneakyThrows
    public List<ColumnDTO> columns(String schema, String table, DataSource  dataSource) {
        try (final Connection connection = dataSource.getConnection()) {
            return columns(schema, table, connection);
        }
    }

    @SneakyThrows
    public List<ColumnDTO> columns(String schema, String table, Connection connection) {
        final DatabaseMetaData metaData = connection.getMetaData();
        final ResultSet columns = metaData.getColumns(null, schema, table, "%");
        final List<ColumnDTO> result = Lists.newArrayList();

        final ResultSet primaryKeys = metaData.getPrimaryKeys(null, schema, table);
        final List<String> primaryKeyColumnNames = Lists.newArrayList();
        while (primaryKeys.next()) {
            final String primaryKeyColumnName = primaryKeys.getString("COLUMN_NAME");
            primaryKeyColumnNames.add(primaryKeyColumnName);
        }

        while (columns.next()) {
            final String columnName = columns.getString("COLUMN_NAME");
            final String columnType = columns.getString("TYPE_NAME");
            result.add(new ColumnDTO(columnName, columnType, primaryKeyColumnNames.contains(columnName)));
        }

        return result;
    }
}
