package com.vbobot.web.dba.service.sync;

import com.vbobot.web.dba.service.datasource.DataSourceDTO;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author Bobo
 * @date 2024/3/18
 */
@Data
public class DataSyncJobDTO {
    private Integer id;

    private String name;
    private String cron;
    private Integer sourceDataSourceId;
    private DataSourceDTO sourceDataSource;

    private Integer targetDataSourceId;
    private DataSourceDTO targetDataSource;

    private String sourceSchema;
    private String targetSchema;
    private String sourceTable;
    private String targetTable;
    private String orderByColumn;
    private Boolean running;
    private LocalDate lastRunTime;
    private Boolean lastRunSuccess;
    private LocalDate createTime;
    private LocalDate updateTime;
}
