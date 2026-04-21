package com.report.module.im.service.impl;

import com.report.module.im.pojo.bo.ImFileDealBO;
import com.report.module.im.service.ImAlarmFileStandardStrategy;
import com.report.module.im.service.ImDefaultAlarmFileStandardStrategy;
import org.springframework.stereotype.Component;

/**
 * 告警文件默认流程处理老标准策略实现
 */
@Component
public class ImDefaultAlarmFileOldStrategy extends ImDefaultAlarmFileStandardStrategy implements ImAlarmFileStandardStrategy {

    @Override
    public void dealFile(ImFileDealBO deal) {

    }
}