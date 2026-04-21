package com.report.module.im.pojo.bo;

import java.util.List;

/**
 * 对IMFileTopicMsgBO封装，封装了模块类型和文件处理信息，以父模块作为分组
 *
 * @param module       模块类型
 * @param fileTopicMsgBOList   文件处理信息
 */
public record ImFileDealBO(String module, List<ImFileTopicMsgBO> fileTopicMsgBOList) {
}
