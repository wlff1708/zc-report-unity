package com.report.module.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.report.module.im.pojo.bo.ImAlarmRecordBO;
import com.report.module.im.pojo.entity.ImAlarmAllRecorderEntity;

/**
 * 告警全量记录 Service 接口
 */
public interface ImAlarmAllRecorderService extends IService<ImAlarmAllRecorderEntity> {

    /**
     * 批量保存告警记录
     */
    void batchSaveRecords(java.util.List<ImAlarmRecordBO> boList);
}
