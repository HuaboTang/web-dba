package com.vbobot.web.dba.service.data;

import com.vbobot.common.utils.exception.Assert;
import com.vbobot.web.dba.domain.ColumnDTO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@Slf4j
public class DataInserter {
    private final Connection connection;
    private final ExcelContent excelContent;
    private final List<ColumnDTO> columns;
    private final String schema;
    private final String table;

    private final Map<String, ColumnDTO> columnByLowerCaseName;

    public DataInserter(Connection connection, ExcelContent excelContent,
                        String schema, String table,
                        List<ColumnDTO> columns) {
        this.connection = connection;
        this.excelContent = excelContent;
        this.columns = columns;
        this.schema = schema;
        this.table = table;

        checkHeaders();

        this.columnByLowerCaseName = columns.stream().collect(Collectors.toMap(item -> item.getName().toLowerCase(), item -> item));
    }

    private void checkHeaders() {
        Assert.notEmpty(excelContent.headers(), "headers must not be empty");
        Assert.notEmpty(columns, "columns must not be empty");

        final Set<String> lowCaseColumns = columns.stream()
                .map(item -> item.getName().toLowerCase())
                .collect(Collectors.toSet());
        final boolean allMatch = excelContent.headers().stream()
                .allMatch(header -> lowCaseColumns.contains(header.toLowerCase()));
        Assert.isTrue(allMatch, "some header not found match column");
    }

    public void doInsert() throws SQLException {
        connection.setAutoCommit(false);

        final List<String> headers = excelContent.headers();
        final String joinHeaders = String.join(", ", headers);
        final String joinedQuestionMarks = headers.stream().map(item -> "?").collect(Collectors.joining(", "));
        final String sql = "insert into %s.%s (%s) values (%s)".formatted(schema, table, joinHeaders, joinedQuestionMarks);
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        final Iterator<List<Object>> rowValues = excelContent.rowValues();
        while (rowValues.hasNext()) {
            final List<Object> rowValue = rowValues.next();
            for (int i = 0; i < rowValue.size(); i++) {
                final Object value = rowValue.get(i);
                preparedStatement.setObject(i + 1, dataBaseValue(value, columnByLowerCaseName.get(headers.get(i).toLowerCase())));
            }
            preparedStatement.addBatch();
        }
        final int[] ints = preparedStatement.executeBatch();
        connection.commit();
        log.info("insert {} rows", ints);
    }

    private Object dataBaseValue(Object value, ColumnDTO column) {
        return switch (column.getType().toLowerCase()) {
            case "varchar" -> value == null ? null : value.toString();
            case "integer" -> toInteger(value);
            case "double" -> value == null ? null : Double.parseDouble(value.toString());
            case "timestamp", "datetime", "date" -> toDate(value);
            default -> value;
        };
    }

    @SneakyThrows
    @Nullable
    private static Object toDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof java.util.Date) {
            return value;
        }

        return DateUtils.parseDate(value.toString(), "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd");
    }

    @Nullable
    private static Integer toInteger(Object value) {
        if (value == null)  {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return NumberUtils.toInt(value.toString());
    }
}
