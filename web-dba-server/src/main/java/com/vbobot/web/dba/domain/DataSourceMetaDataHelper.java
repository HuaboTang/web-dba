package com.vbobot.web.dba.domain;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@Component
public class DataSourceMetaDataHelper {

    @SneakyThrows
    public List<String> schemas(Connection connection) {
        final DatabaseMetaData metaData = connection.getMetaData();
        final ResultSet schemas = metaData.getSchemas();
        if (!schemas.next()) {
            return Lists.newArrayList();
        }
        final List<String> result = Lists.newArrayList();
        while (schemas.next()) {
            final String schemaName = schemas.getString("TABLE_SCHEM");
            result.add(schemaName);
        }
        return result;
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
            final String schema = tables.getString("TABLE_SCHEM");
            final String tableName = tables.getString("TABLE_NAME");
            result.add(new TableDTO().setSchema(schema).setTable(tableName));
        }
        return result;
    }

    @SneakyThrows
    public List<ColumnDTO> columns(String schema, String table, Connection connection) {
        final DatabaseMetaData metaData = connection.getMetaData();
        final ResultSet columns = metaData.getColumns(null, schema, table, "%");
        final List<ColumnDTO> result = Lists.newArrayList();
        while (columns.next()) {

            final String columnName = columns.getString("COLUMN_NAME");
            final String columnType = columns.getString("TYPE_NAME");
            final int columnSize = columns.getInt("COLUMN_SIZE");
            final int decimalDigits = columns.getInt("DECIMAL_DIGITS");
            final int nullable = columns.getInt("NULLABLE");
            final String remarks = columns.getString("REMARKS");
            final int columnTypeScale = columns.getInt("NUM_PREC_RADIX");
            final int columnTypePrecision = columns.getInt("COLUMN_SIZE");

            result.add(new ColumnDTO(columnName, columnType));
        }

        return result;
    }
}
