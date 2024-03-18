package com.vbobot.web.dba.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@Data
@Accessors(chain = true)
public class TableDTO {
    private String schema;
    private String table;
}
