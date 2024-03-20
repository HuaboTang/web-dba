package com.vbobot.web.dba.domain;

import lombok.Data;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@Data
public class ColumnDTO {
    private String name;
    private String type;
    private Boolean primaryKey;

    public ColumnDTO(String name, String type) {
        this.name = name;
        this.type = type;
        this.primaryKey = false;
    }

    public ColumnDTO(String name, String type, Boolean primaryKey) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
    }
}
