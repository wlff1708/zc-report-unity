package com.report.common.util.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 批量分片串行处理工具类
 */
@Slf4j
public class BatchSplitProcessUtil {

    /**
     * 分片批量处理方法
     * @param businessType 业务类型
     * @param dataList 待处理数据集合
     * @param batchSize 每批处理数量
     * @param batchProcessor 分片处理逻辑（函数式接口）
     * @param <T> 数据类型
     */
    public static <T> void batchProcess(String businessType, List<T> dataList, int batchSize, Consumer<List<T>> batchProcessor) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        String taskId = UUID.randomUUID().toString();

        log.info("开始【{}】批量处理数据，任务id{}， 总条数：{}", businessType, taskId, dataList.size());

        int totalSize = dataList.size();
        if (totalSize > batchSize) {
            int num = totalSize / batchSize;
            int remainder = totalSize % batchSize;

            // 处理完整批次
            for (int i = 1; i <= num; i++) {
                List<T> subList = dataList.subList((i - 1) * batchSize, i * batchSize);
                batchProcessor.accept(subList);
            }

            // 处理剩余数据
            if (remainder > 0) {
                List<T> remainderList = dataList.subList(num * batchSize, totalSize);
                batchProcessor.accept(remainderList);
            }
        } else {
            batchProcessor.accept(dataList);
        }

        log.info("完成【{}】批量处理数据，任务id{}", businessType, taskId);
    }
}
