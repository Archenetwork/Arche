package com.blockinsight.basefi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 推荐订单表
 * </p>
 *
 * @author Janin
 * @since 2021-01-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class RecommendOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 买方代币
     */
    private String buyerToken;

    /**
     * 卖方代币
     */
    private String sellerToken;

    /**
     * 买方代币地址
     */
    private String buyerTokenAddr;

    /**
     * 卖方代币名称
     */
    private String sellerTokenAddr;

    /**
     * 等待交割数量
     */
    private Integer waitDeliveryNum;

    /**
     * 等待生效数量
     */
    private Integer waitEffectNum;

    /**
     * 总数量
     */
    private Integer totalNum;

    /**
     * 链类型 1 火币 2 币安
     */
    private Integer chainType;

    @TableField(exist = false)
    private String orderNum;

    /**
     * 1 创建订单，增加待生效，总数量 2 增加待交割 3 减少待交割 4 减少待生效
     */
    @TableField(exist = false)
    private Integer type;

}
