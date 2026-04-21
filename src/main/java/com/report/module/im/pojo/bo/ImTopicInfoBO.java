package com.report.module.im.pojo.bo;


/**
 * Kafka topic 元信息
 *
 * @param topic       Kafka topic 名称
 * @param partition   分区数
 * @param replication 副本数
 */
public record ImTopicInfoBO(String topic, int partition, int replication) {
}
