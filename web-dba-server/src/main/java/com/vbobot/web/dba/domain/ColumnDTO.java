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

    public ColumnDTO(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
