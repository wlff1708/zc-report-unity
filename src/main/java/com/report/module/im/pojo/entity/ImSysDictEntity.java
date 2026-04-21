package com.report.module.im.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 字典表
 */
@Data
@TableName("sys_dict")
public class ImSysDictEntity {

    /** 编号 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 数据值 */
    private String value;

    /** 标签名 */
    private String label;

    /** 类型 */
    private String type;

    /** 描述 */
    private String description;

    /** 排序（升序） */
    private BigDecimal sort;

    /** 父级编号 */
    private String parentId;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createDate;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateDate;

    /** 备注信息 */
    private String remarks;

    /** 删除标记 */
    private String delFlag;
}
