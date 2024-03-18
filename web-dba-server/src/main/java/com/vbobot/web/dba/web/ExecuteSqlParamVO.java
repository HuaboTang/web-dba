package com.vbobot.web.dba.web;

import lombok.Data;

/**
 * @author Bobo
 * @date 2024/3/18
 */
@Data
public class ExecuteSqlParamVO {
    private String sql;
    private Integer dataSourceId;
}
