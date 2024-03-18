package com.vbobot.web.dba.service.data;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/18
 */
@Data
@Accessors(chain = true)
public class ExecuteSqlResultDTO {
    private Boolean success;
    private String message;
    private List<String> headers;
    private List<List<Object>> datas;
}
