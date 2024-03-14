package com.vbobot.web.dba.dao.datasource;

import com.vbobot.web.dba.service.datasource.DataSourceTypeEnum;
import jakarta.persistence.*;
import lombok.Data;

/**
 * @author Bobo
 * @date 2024/3/14
 */
@Entity
@Table(name = "datasource")
@Data
public class DataSourceDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private DataSourceTypeEnum type;
    private String name;
    private String url;
    private String username;

    private String password;

    @Column(name = "create_time", insertable = false, updatable = false)
    private String createTime;
}
