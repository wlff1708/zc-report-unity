package com.report.module.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.report.module.im.mapper.ImAlarmAllRecorderMapper;
import com.report.module.im.pojo.bo.ImAlarmRecordBO;
import com.report.module.im.pojo.converter.ImAlarmRecordConverter;
import com.report.module.im.pojo.entity.ImAlarmAllRecorderEntity;
import com.report.module.im.service.ImAlarmAllRecorderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 告警全量记录 Service 实现类
 */
@Service
public class ImAlarmAllRecorderServiceImpl extends ServiceImpl<ImAlarmAllRecorderMapper, ImAlarmAllRecorderEntity> implements ImAlarmAllRecorderService {

    @Resource
    private ImAlarmRecordConverter imAlarmRecordConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveRecords(List<ImAlarmRecordBO> boList) {
        saveBatch(imAlarmRecordConverter.toEntityList(boList), 1024);
    }
}
