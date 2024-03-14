package com.vbobot.web.dba.service.datasource;

import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/11
 */
public interface DataSourceService {
    TestConnectResultDTO testConnect(DataSourceDTO dataSource);

    DataSourceDTO save(DataSourceDTO dataSource);

    List<DataSourceDTO> getAll();
}
