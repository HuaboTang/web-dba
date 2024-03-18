package com.vbobot.web.dba.service.metadata;

import com.vbobot.common.utils.exception.Assert;
import com.vbobot.web.dba.dao.datasource.DataSourceDO;
import com.vbobot.web.dba.dao.datasource.DataSourceRepository;
import com.vbobot.web.dba.domain.DataSourceConnectionManager;
import com.vbobot.web.dba.domain.DataSourceMetaDataHelper;
import com.vbobot.web.dba.domain.TableDTO;
import com.vbobot.web.dba.service.datasource.DataSourceConvertor;
import com.vbobot.web.dba.service.datasource.DataSourceDTO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@Service
public class MetadataServiceImpl implements MetadataService {
    @Resource DataSourceRepository dataSourceRepository;
    @Resource DataSourceConnectionManager dataSourceConnectionManager;
    @Resource DataSourceMetaDataHelper dataSourceMetaDataHelper;

    @SneakyThrows
    @Override
    public List<TableDTO> schemaTables(Integer dataSourceId) {
        final Optional<DataSourceDO> dataSource = dataSourceRepository.findById(dataSourceId);
        Assert.isTrue(dataSource.isPresent(), "数据源不存在");
        assert dataSource.isPresent();

        final DataSourceDTO dDataSource = DataSourceConvertor.INST.toDataSourceDTO(dataSource.get());
        final Connection connection = dataSourceConnectionManager.getConnection(dDataSource);
        return dataSourceMetaDataHelper.tables(connection);
    }
}
