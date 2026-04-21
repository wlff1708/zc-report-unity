package com.report.module.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.report.module.im.pojo.entity.ImSysDictEntity;

/**
 * 字典表 Service 接口
 */
public interface ImSysDictService extends IService<ImSysDictEntity> {

    /**
     * 根据字典类型查询字典值
     */
    String getValueByType(String type);
}
