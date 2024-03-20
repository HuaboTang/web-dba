package com.vbobot.web.dba.dao.sync;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/18
 */
@Repository
public interface DataSyncJobRepository extends JpaRepository<DataSyncJobDO, Integer>, JpaSpecificationExecutor<DataSyncJobDO> {
    List<DataSyncJobDO> findAllByRunning(Boolean running);
}
