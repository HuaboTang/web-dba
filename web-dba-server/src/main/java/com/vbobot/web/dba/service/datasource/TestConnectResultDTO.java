package com.vbobot.web.dba.service.datasource;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Bobo
 * @date 2024/3/14
 */
@Data
@Accessors(chain = true)
public class TestConnectResultDTO {
    private Boolean success;
    private String message;
}
