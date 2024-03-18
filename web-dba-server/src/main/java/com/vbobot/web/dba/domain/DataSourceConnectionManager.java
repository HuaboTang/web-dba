package com.vbobot.web.dba.domain;

import com.vbobot.web.dba.service.datasource.DataSourceDTO;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

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

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dataSourceConfig.getUrl());
            config.setUsername(dataSourceConfig.getUsername());
            config.setPassword(dataSourceConfig.getPassword());
            config.setDriverClassName(driverClassName(dataSourceConfig));
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(300000);
            config.setIdleTimeout(120000);
            config.setLeakDetectionThreshold(300000);
            final HikariDataSource dataSource = new HikariDataSource(config);
            DATA_SOURCES.put(dataSourceConfig.getId(), dataSource);
            return dataSource.getConnection();
        }
    }

    private String driverClassName(DataSourceDTO dataSourceConfig) {
        return switch (dataSourceConfig.getType()) {
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case DM -> "dm.jdbc.driver.DmDriver";
            default -> throw new RuntimeException("Unsupported driver class name");
        };
    }
}
