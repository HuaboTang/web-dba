package com.vbobot.web.dba.dao.sync;

import com.vbobot.web.dba.dao.datasource.DataSourceDO;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author Bobo
 * @date 2024/3/18
 */
@Data
@Entity
@Table(name = "data_sync_job")
public class DataSyncJobDO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String cron;

    @Column(name = "source_data_source_id")
    private Integer sourceDataSourceId;

    @ManyToOne
    @JoinColumn(name = "source_data_source_id", insertable = false, updatable = false)
    private DataSourceDO sourceDataSource;

    @Column(name = "target_data_source_id")
    private Integer targetDataSourceId;

    @ManyToOne
    @JoinColumn(name = "target_data_source_id", insertable = false, updatable = false)
    private DataSourceDO targetDataSource;

    private String sourceSchema;
    private String targetSchema;
    private String sourceTable;
    private String targetTable;
    private String orderByColumn;
    private Boolean running;
    private LocalDate lastRunTime;

    @Column(name = "last_run_success", insertable = false)
    private Boolean lastRunSuccess;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDate createTime;

    @Column(name = "update_time", insertable = false)
    private LocalDate updateTime;
}
