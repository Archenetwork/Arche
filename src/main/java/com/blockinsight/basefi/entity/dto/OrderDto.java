package com.blockinsight.basefi.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;


import java.util.Date;

/**
 * <p>
 * 消息列表
 * </p>
 *
 * @author YanLong
 * @since 2021-01-05
 */
@Data
public class OrderDto {

    /**
     * 订单编号
     */
    private String orderNum;

    /**
     * 订单状态 1 等待买家  2 等待卖家 3 待支付保证金 4 已支付保证金 5 等待交割 6 已交割
     */
    private Integer orderStatus;

    /**
     * 买家标的物（交易使用的币）
     */
    private String buyerSubjectMatter;

    /**
     * 买家标的物地址
     */
    private String buyerSubjectMatterAddr;

    /**
     * 买家标的物图标
     */
    private String buyerSubjectMatterImg;

    /**
     * 卖家标的物（交易使用的币）
     */
    private String sellerSubjectMatter;

    /**
     * 卖家标的物地址
     */
    private String sellerSubjectMatterAddr;

    /**
     * 卖家标的物图标
     */
    private String sellerSubjectMatterImg;

    /**
     * 买方交割数量（买方标的物数量）
     */
    private String buyerDeliveryQuantity;

    /**
     * 卖方交割数量（卖方标的物数量）
     */
    private String sellerDeliveryQuantity;

    /**
     * 订单交割时间（在这个时间内可以交割，超出时间未交割视为违约）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderDeliveryTime;

    /**
     * 交割区块高度（在这个区块可以交割）
     */
    private Integer deliveryHeight;

    /**
     * 合约创建区块高度
     */
    private Integer contractCreateBlockNumber;

    /**
     * 买家地址
     */
    private String buyerAddr;

    /**
     * 卖家地址
     */
    private String sellerAddr;

    /**
     * 保证金状态 0 双方未支付 1 买方支付 2 卖方支付 3 双方支付
     */
    private Integer earnestMoneyStatus;

    /**
     * 订单生效时间（需要在这个时间前，支付足额的保证金）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderTakeEffectTime;


}
