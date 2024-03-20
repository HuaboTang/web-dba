package com.vbobot.web.dba.service.sync;

import com.vbobot.common.utils.web.vo.PagingParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Bobo
 * @date 2024/3/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataSyncPagingParamDTO extends PagingParam {
    private String name;
    private Integer sourceDataSourceId;
    private Integer targetDataSourceId;
}
