package com.vbobot.web.dba.web;

import com.vbobot.common.utils.web.vo.CommonResult;
import com.vbobot.web.dba.domain.TableDTO;
import com.vbobot.web.dba.service.metadata.MetadataService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Bobo
 * @date 2024/3/15
 */
@RestController
@RequestMapping("/metadata")
public class MetadataController {
    @Resource MetadataService metadataService;

    @GetMapping("/schema-tables/{dataSourceId}")
    public CommonResult<List<TableDTO>> schemaTables(@PathVariable("dataSourceId") Integer dataSourceId) {
        List<TableDTO> tables = metadataService.schemaTables(dataSourceId);
        return CommonResult.success(tables);
    }
}
