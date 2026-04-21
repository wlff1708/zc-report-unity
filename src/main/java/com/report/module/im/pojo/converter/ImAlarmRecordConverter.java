package com.report.module.im.pojo.converter;

import com.report.module.im.pojo.bo.ImAlarmRecordBO;
import com.report.module.im.pojo.entity.ImAlarmAllRecorderEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 告警记录 BO ↔ Entity 转换器
 */
@Mapper(componentModel = "spring")
public interface ImAlarmRecordConverter {

    ImAlarmAllRecorderEntity toEntity(ImAlarmRecordBO bo);

    List<ImAlarmAllRecorderEntity> toEntityList(List<ImAlarmRecordBO> boList);
}
