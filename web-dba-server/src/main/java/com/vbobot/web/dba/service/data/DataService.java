package com.vbobot.web.dba.service.data;

/**
 * @author Bobo
 * @date 2024/3/15
 */
public interface DataService {
    void importData(ExcelContent excelContent, Integer dataSourceId, String schema, String table);

    ExecuteSqlResultDTO executeSql(String sql, Integer dataSourceId);
}
