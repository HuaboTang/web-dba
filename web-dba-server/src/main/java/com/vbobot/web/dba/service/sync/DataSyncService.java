package com.vbobot.web.dba.service.sync;

import com.vbobot.common.utils.web.vo.PagingResult;

/**
 * @author Bobo
 * @date 2024/3/18
 */
public interface DataSyncService {
    PagingResult<DataSyncJobDTO> page(DataSyncPagingParamDTO param);

    boolean check(DataSyncJobDTO param);

    DataSyncJobDTO save(DataSyncJobDTO param);

    void initJob();

    DataSyncJobDTO get(Integer jobId);
}
