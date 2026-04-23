package com.report.module.im.pojo.converter;

import com.report.module.im.pojo.bo.ImDataTopicMsgBO;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Kafka 数据消息 DTO → BO 转换器
 */
@Mapper(componentModel = "spring")
public interface ImKafkaDataConverter {

    @Mapping(source = "moduleType", target = "module")
    @Mapping(source = "subModuleType", target = "subModule")
    ImDataTopicMsgBO toDataTopicMsgBO(ImKafkaListenDataDTO dto);

    List<ImDataTopicMsgBO> toDataTopicMsgBOList(List<ImKafkaListenDataDTO> dtoList);
}
