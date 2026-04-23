package com.report.module.im.pojo.bo;

import java.util.List;

/**
 * 对ImDataTopicMsgBO封装，以父模块作为分组
 *
 * @param module       模块类型
 * @param dataTopicMsgBOList   数据消息列表
 */
public record ImDataDealBO(String module, List<ImDataTopicMsgBO> dataTopicMsgBOList) {
}
