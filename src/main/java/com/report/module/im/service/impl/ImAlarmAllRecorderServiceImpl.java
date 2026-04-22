package com.report.module.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @Override
    public List<ImAlarmRecordBO> listByAlarmId(String alarmId) {
        LambdaQueryWrapper<ImAlarmAllRecorderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImAlarmAllRecorderEntity::getAlarmId, alarmId);
        return imAlarmRecordConverter.toBOList(list(wrapper));
    }

    @Override
    public void removeByAlarmId(String alarmId) {
        LambdaQueryWrapper<ImAlarmAllRecorderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImAlarmAllRecorderEntity::getAlarmId, alarmId);
        remove(wrapper);
    }
}
