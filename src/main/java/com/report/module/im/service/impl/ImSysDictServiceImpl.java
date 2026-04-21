package com.report.module.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.report.module.im.mapper.ImSysDictMapper;
import com.report.module.im.pojo.entity.ImSysDictEntity;
import com.report.module.im.service.ImSysDictService;
import org.springframework.stereotype.Service;

/**
 * 字典表 Service 实现类
 */
@Service
public class ImSysDictServiceImpl extends ServiceImpl<ImSysDictMapper, ImSysDictEntity> implements ImSysDictService {
}
