package com.vbobot.web.dba.service.datasource;

import com.vbobot.web.dba.dao.datasource.DataSourceDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Bobo
 * @date 2024/3/14
 */
@Mapper
public interface DataSourceConvertor {
    DataSourceConvertor INST = Mappers.getMapper(DataSourceConvertor.class);

    DataSourceDO toDataSourceEntity(DataSourceDTO dataSource);

    DataSourceDTO toDataSourceDTO(DataSourceDO result);
}
