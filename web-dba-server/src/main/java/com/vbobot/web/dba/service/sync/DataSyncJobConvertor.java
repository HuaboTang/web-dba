package com.vbobot.web.dba.service.sync;

import com.vbobot.web.dba.dao.sync.DataSyncJobDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Bobo
 * @date 2024/3/18
 */
@Mapper
public interface DataSyncJobConvertor {
    DataSyncJobConvertor  INST = Mappers.getMapper(DataSyncJobConvertor.class);

    DataSyncJobDTO toDto(DataSyncJobDO dataSyncJob);

    DataSyncJobDO toDO(DataSyncJobDTO param);
}
