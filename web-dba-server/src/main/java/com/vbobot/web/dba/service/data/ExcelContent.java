package com.vbobot.web.dba.service.data;

import com.alibaba.excel.metadata.data.ReadCellData;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Bobo
 * @date 2024/3/15
 */
public class ExcelContent {
    private final Map<Integer, ReadCellData<?>> headMap;
    private final List<Map<Integer, Object>> datas;

    private Integer maxIndex;

    public ExcelContent(Map<Integer, ReadCellData<?>> headMap, List<Map<Integer, Object>> datas) {
        if (headMap == null || headMap.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.headMap = headMap;
        this.datas = datas;
    }

    public List<String> headers() {
        List<String> headers = Lists.newArrayList();
        final int maxIndex = getMaxIndex();
        for (int i = 0; i <= maxIndex; i++) {
            headers.add(headMap.get(i).getStringValue());
        }
        return headers;
    }

    private int getMaxIndex() {
        if (maxIndex != null) {
            return maxIndex;
        }
        return (maxIndex = headMap.keySet().stream().max(Integer::compareTo).get());
    }

    public Iterator<List<Object>> rowValues() {
        return new RowValuesIterator();
    }

    private class RowValuesIterator extends AbstractIterator<List<Object>> {
        private final Iterator<Map<Integer, Object>> in;

        public RowValuesIterator() {
            this.in = datas.iterator();
        }

        @Nullable
        @Override
        protected List<Object> computeNext() {
            while (in.hasNext()) {
                final Map<Integer, Object> next = in.next();
                if (next != null) {
                    final List<Object> result = Lists.newArrayList();
                    for (int i = 0; i <= getMaxIndex(); i++) {
                        result.add(next.get(i));
                    }
                    return result;
                }
            }
            return endOfData();
        }
    }
}
