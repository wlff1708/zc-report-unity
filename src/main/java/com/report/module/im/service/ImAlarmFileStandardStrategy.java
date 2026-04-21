package com.report.module.im.service;

import com.report.module.im.pojo.bo.ImFileDealBO;

/**
 * 告警文件处理标准策略接口
 */
public interface ImAlarmFileStandardStrategy {

    /**
     * 处理告警文件消息
     *
     * @param deal 文件消息BO
     */
    void dealFile(ImFileDealBO deal);
}
