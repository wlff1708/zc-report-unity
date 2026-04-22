package com.report.module.im.pojo.converter;

import com.report.module.im.pojo.bo.ImFileTopicMsgBO;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Kafka 文件消息 DTO → BO 转换器
 */
@Mapper(componentModel = "spring")
public interface ImKafkaFileConverter {

    @Mapping(source = "moduleType", target = "module")
    @Mapping(source = "subModuleType", target = "subModule")
    @Mapping(source = "filePath", target = "sourceFilePath")
    @Mapping(source = "data", target = "fileDesc")
    ImFileTopicMsgBO toFileTopicMsgBO(ImKafkaListenDataDTO dto);

    List<ImFileTopicMsgBO> toFileTopicMsgBOList(List<ImKafkaListenDataDTO> dtoList);
}
