package com.vbobot.web.dba.service.datasource;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Bobo
 * @date 2024/3/14
 */
@Data
@Accessors(chain = true)
public class DataSourceDTO {
    private Integer id;

    @Column(name = "type")
    private DataSourceTypeEnum type;

    private String name;
    private String url;
    private String username;
    private String password;
}
