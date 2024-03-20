package com.vbobot.web.dba.domain;

import com.vbobot.web.dba.service.datasource.DataSourceDTO;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@Component
public class DataSourceConnectionManager {
    private static final Map<Integer, HikariDataSource> DATA_SOURCES = new ConcurrentHashMap<>();

    public DataSource getDataSource(DataSourceDTO dataSourceConfig) {
        HikariDataSource hikariDataSource = DATA_SOURCES.get(dataSourceConfig.getId());
        if (hikariDataSource != null) {
            return hikariDataSource;
        }

        synchronized (dataSourceConfig.getId().toString().intern()) {
            hikariDataSource = DATA_SOURCES.get(dataSourceConfig.getId());
            if (hikariDataSource != null) {
                return hikariDataSource;
            }

            final HikariDataSource dataSource = createDataSource(dataSourceConfig);
            DATA_SOURCES.put(dataSourceConfig.getId(), dataSource);
            return dataSource;
        }
    }

    @NotNull
    private HikariDataSource createDataSource(DataSourceDTO dataSourceConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceConfig.getUrl());
        config.setUsername(dataSourceConfig.getUsername());
        config.setPassword(dataSourceConfig.getPassword());
        config.setDriverClassName(driverClassName(dataSourceConfig));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(5000);
        return new HikariDataSource(config);
    }

    public Connection getConnection(DataSourceDTO dataSourceConfig) throws SQLException {
        HikariDataSource hikariDataSource = DATA_SOURCES.get(dataSourceConfig.getId());
        if (hikariDataSource != null) {
            return hikariDataSource.getConnection();
        }

        synchronized (dataSourceConfig.getId().toString().intern()) {
            hikariDataSource = DATA_SOURCES.get(dataSourceConfig.getId());
            if (hikariDataSource != null) {
                return hikariDataSource.getConnection();
            }

            final HikariDataSource dataSource = createDataSource(dataSourceConfig);
            DATA_SOURCES.put(dataSourceConfig.getId(), dataSource);
            return dataSource.getConnection();
        }
    }

    private String driverClassName(DataSourceDTO dataSourceConfig) {
        return switch (dataSourceConfig.getType()) {
            case MYSQL -> "com.mysql.jdbc.Driver";
            case DM -> "dm.jdbc.driver.DmDriver";
        };
    }
}
