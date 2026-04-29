package com.report.module.im.pojo.converter;

import com.report.module.im.pojo.bo.ImAlarmJs2CorrelationBO;
import com.report.module.im.pojo.entity.ImAlarmJs2CorrelationEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 指调告警关联 BO ↔ Entity 转换器
 */
@Mapper(componentModel = "spring")
public interface ImAlarmJs2CorrelationConverter {

    ImAlarmJs2CorrelationEntity toEntity(ImAlarmJs2CorrelationBO bo);

    List<ImAlarmJs2CorrelationEntity> toEntityList(List<ImAlarmJs2CorrelationBO> boList);

    ImAlarmJs2CorrelationBO toBO(ImAlarmJs2CorrelationEntity entity);

    List<ImAlarmJs2CorrelationBO> toBOList(List<ImAlarmJs2CorrelationEntity> entityList);
}