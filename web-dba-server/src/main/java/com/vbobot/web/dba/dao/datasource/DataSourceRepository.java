package com.vbobot.web.dba.dao.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Bobo
 * @date 2024/3/14
 */
@Repository
public interface DataSourceRepository extends JpaRepository<DataSourceDO, Integer> {
}
