package com.vbobot.web.dba.service.datasource;

import com.vbobot.web.dba.dao.datasource.DataSourceDO;
import com.vbobot.web.dba.dao.datasource.DataSourceRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/11
 */
@Slf4j
@Service
public class DataSourceServiceImpl implements DataSourceService {
    @Resource DataSourceRepository dataSourceRepository;

    @Override
    public TestConnectResultDTO testConnect(DataSourceDTO dataSource) {
        try (final Connection ignored = DriverManager.getConnection(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword())) {
            return new TestConnectResultDTO().setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new TestConnectResultDTO().setMessage(e.getMessage()).setSuccess(false);
        }
    }

    @Override
    public DataSourceDTO save(DataSourceDTO dataSource) {
        final DataSourceDO entity = DataSourceConvertor.INST.toDataSourceEntity(dataSource);
        final DataSourceDO result = dataSourceRepository.save(entity);
        return DataSourceConvertor.INST.toDataSourceDTO(result);
    }

    @Override
    public List<DataSourceDTO> getAll() {
        return dataSourceRepository.findAll().stream().map(DataSourceConvertor.INST::toDataSourceDTO).toList();
    }
}
