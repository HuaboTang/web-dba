package com.vbobot.web.dba.service.metadata;

import com.vbobot.web.dba.domain.TableDTO;

import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/15
 */
public interface MetadataService {
    List<TableDTO> schemaTables(Integer dataSourceId);
}
