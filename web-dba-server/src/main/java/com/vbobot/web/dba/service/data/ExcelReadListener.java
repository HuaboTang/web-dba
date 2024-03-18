package com.vbobot.web.dba.service.data;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * @author Bobo
 * @date 2024/3/15
 */
public class ExcelReadListener implements ReadListener<Object> {
    private Map<Integer, ReadCellData<?>> headMap;
    private List<Map<Integer, Object>> datas = Lists.newArrayList();

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        this.headMap = headMap;
    }

    @Override
    public void invoke(Object data, AnalysisContext context) {
        datas.add((Map<Integer, Object>) data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

    public ExcelContent getExcelContent() {
        return new ExcelContent(headMap, datas);
    }
}
