package com.blockinsight.basefi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 消息类型
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MessageType extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "message_type_id", type = IdType.AUTO)
    private Integer messageTypeId;

    /**
     * 消息类型名称
     */
    private String messageTypeName;


}
