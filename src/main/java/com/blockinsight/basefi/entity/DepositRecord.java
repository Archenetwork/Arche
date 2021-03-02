package com.blockinsight.basefi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 保证金记录
 * </p>
 *
 * @author Janin
 * @since 2021-01-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DepositRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单编号
     */
    private String orderNum;

    /**
     * 用户地址
     */
    private String userAddr;

    /**
     * 类型 1 买方 2 卖方
     */
    private Integer type;

    /**
     * 本次抵押数量
     */
    private String amount;

    /**
     * 总抵押数量
     */
    private String depositedAmount;

    /**
     * 区块高度
     */
    private Integer blockNumber;

    /**
     * 交易哈希
     */
    private String transactionHash;

    /**
     * 事件触发时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date eventTime;

    /**
     * 链类型 1 火币 2 币安
     */
    private Integer chainType;

    @Data
    public static class DepositRecordParam {

        private String orderNum;

        private String userAddr;

        private String amount;

        private String depositedAmount;

        private Date eventTime;

        private Integer blockNumber;

        private String transactionHash;

        private Integer chainType;
    }
}
