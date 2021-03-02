package com.blockinsight.basefi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户代币记录表
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class UserTokenRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "user_token_record_id", type = IdType.AUTO)
    private Integer userTokenRecordId;

    /**
     * 用户公钥
     */
    private String userNum;

    /**
     * 订单编号
     */
    private String orderNum;

    /**
     * 订单状态 1 等待买家  2 等待卖家 3 待支付保证金 4 已支付保证金 5 等待交割 6 已交割
     */
    private Integer orderStatus;

    /**
     * 权益代币名称
     */
    private String tokenName;

    /**
     * 权益代币地址
     */
    private String tokenAddr;
}
