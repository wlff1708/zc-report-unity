package com.report.module.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.report.module.im.pojo.bo.ImAlarmRecordBO;
import com.report.module.im.pojo.entity.ImAlarmAllRecorderEntity;

import java.util.List;

/**
 * 告警全量记录 Service 接口
 */
public interface ImAlarmAllRecorderService extends IService<ImAlarmAllRecorderEntity> {

    /**
     * 批量保存告警记录
     */
    void batchSaveRecords(java.util.List<ImAlarmRecordBO> boList);

    /**
     * 根据告警ID查询记录
     */
    List<ImAlarmRecordBO> listByAlarmId(String alarmId);

    /**
     * 根据告警ID删除记录
     */
    void removeByAlarmId(String alarmId);
}
