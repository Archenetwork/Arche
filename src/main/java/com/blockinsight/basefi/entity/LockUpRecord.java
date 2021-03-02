package com.blockinsight.basefi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Janin
 * @since 2021-02-02
 */
@Data
@Accessors(chain = true)
public class LockUpRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 代币名称
     */
    private String tokenName;

    /**
     * 标的物地址
     */
    private String tokenAddr;

    /**
     * 已交割数量
     */
    private String deliveryCount;

    /**
     * 当前数量
     */
    private String currentCount;

    /**
     * 总数量
     */
    private String sumCount;

    /**
     * 价格
     */
    private String price;

    /**
     * 链类型 1 火币 2 币安
     */
    private Integer chainType;

    @Data
    public static class TokenSumAddParam {
        private String tokenAddr;
        private String count;
        private String uuid;
        // 1 增加 2 减少
        private Integer type;
        private Integer chainType;
    }

}
