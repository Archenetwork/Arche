package com.blockinsight.basefi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import com.blockinsight.basefi.common.parentclass.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
/**
 * <p>
 * 订单表
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("`order`")
public class Order extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer orderId;

    /**
     * 订单编号
     */
    private String orderNum;

    /**
     * 订单状态 0 等待初始化 1 初始化 2 等待买家  3 等待卖家 4 买卖家都确定
     *     5 买方领取 6 卖方领取 7 交割完成
     */
    private Integer orderStatus;

    /**
     * 保证金状态 0 双方未支付 1 买方支付 2 卖方支付 3 双方支付
     */
    private Integer earnestMoneyStatus;

    /**
     * 补足代币状态 0 双方未补 1 买方补足 2 卖方补足 3 双方补足
     */
    private Integer depositStatus;

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
     * 交割价格（一个卖方标的物对应买方标的物数量）
     */
    private String deliveryPrice;

    /**
     * 买方交割数量（买方标的物数量）
     */
    private String buyerDeliveryQuantity;

    /**
     * 卖方交割数量（卖方标的物数量）
     */
    private String sellerDeliveryQuantity;

    /**
     * 距离合约生效区块高度数量
     */
    private Integer effectiveHeight;

    /**
     * 订单生效时间（需要在这个时间前，支付足额的保证金）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderTakeEffectTime;

    /**
     * 距离交割区块高度数量
     */
    private Integer deliveryHeight;

    /**
     * 订单交割时间（在这个时间内可以交割，超出时间未交割视为违约）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderDeliveryTime;

    /**
     * 买方保证金
     */
    private String buyerEarnestMoney;

    /**
     * 卖方保证金
     */
    private String sellerEarnestMoney;

    /**
     * 买家已付（买家支付保证金数量）
     */
    private String buyerPaid;

    /**
     * 买方邀请人
     */
    private String buyerReferer;

    /**
     * 卖家已付（卖家支付保证金数量）
     */
    private String sellerPaid;

    /**
     * 卖方邀请人
     */
    private String sellerReferer;

    /**
     * 买家地址
     */
    private String buyerAddr;

    /**
     * 卖家地址
     */
    private String sellerAddr;

    /**
     * 买家权益代币名称
     */
    private String buyerTokenName;

    /**
     * 卖家权益代币名称
     */
    private String sellerTokenName;

    /**
     * 买家权益代币地址
     */
    private String buyerTokenAddr;

    /**
     * 卖家权益代币地址
     */
    private String sellerTokenAddr;

    /**
     * 买家权益代币总数量（权益凭证）
     */
    private String buyerTokenTotalQuantity;

    /**
     * 卖家权益代币总数量（权益凭证）
     */
    private String sellerTokenTotalQuantity;

    /**
     * 合约创建人地址
     */
    private String contractCreatorAddr;

    /**
     * 合约部署费用
     */
    private String contractDeploymentCost;

    /**
     * 合约创建人保证金
     */
    private String contractCreatorEarnestMoney;

    /**
     * 合约创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date contractCreateTime;

    /**
     * 合约创建区块高度
     */
    private Integer contractCreateBlockNumber;

    /**
     * 合约初始化时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date contractInitializeTime;

    /**
     * 合约初始化区块高度
     */
    private Integer contractInitializeBlockNumber;

    /**
     * 事件费（用户每次和合约交互时支付的费用）定值（收取ETH）
     */
    private String eventPrice;

    /**
     * 成单赏金；定值（收取ETH)
     */
    private String moneyReward;

    /**
     * 推荐值(事件费与成单赏金之和)
     */
    private String recommendedValue;
    /**
     * 买方需补足（交割数量 * 交割价格 - 交割数量 * 交割价格 * 保证金比例）
     */
    private String buyerNeedMakeUp;

    /**
     * 卖方需补足（交割数量 - 交割数量 * 保证金比例）
     */
    private String sellerNeedMakeUp;

    /**
     * 买家已补足（买家补足标的物数量）
     */
    private String buyerMakeUp;

    /**
     * 卖家已补足（卖家补足标的物数量）
     */
    private String sellerMakeUp;

    /**
     * 买家领取状态 1 未成对 2 已履行完成合约 3 自己履行而对手未履行 4 自己未履行
     */
    private Integer buyerWithdrawStatus;

    /**
     * 卖家领取状态 1 未成对 2 已履行完成合约 3 自己履行而对手未履行 4 自己未履行
     */
    private Integer sellerWithdrawStatus;

    /**
     * 订单约定状态 0 正常 1 保险金违约 2 代币违约
     */
    private Integer contractStatus;

    /**
     * 推荐订单状态 0 初始 1 待交割 2 已交割
     */
    private Integer recommendOrderStatus;

    /**
     * 链类型 1 火币 2 币安
     */
    private Integer chainType;


    @Data
    public static class OrderInpParam extends Page {
        /**
         * 搜索内容
         */
        private String orderNumber;
        /**
         * 卖家标的物
         */
        private String buyerSubject;
        /**
         * 买家标的物
         */
        private String sellerSubject;

        private Integer chainType;

    }

}
