package com.blockinsight.basefi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 消息记录表
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class Message extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Integer messageId;

    /**
     * 消息内容
     */
    private String messageContext;

    /**
     * 消息类型id
     */
    private Integer messageTypeId;

    /**
     * 消息类型名称
     */
    private String messageTypeName;

    /**
     * 用户地址
     */
    private String userAddr;

    /**
     * 关联订单编号
     */
    private String orderNum;

    /**
     * 是否已读 0 否 1 是
     */
    private Integer isRead;

    /**
     * 链类型 1 火币 2 币安
     */
    private Integer chainType;

}
