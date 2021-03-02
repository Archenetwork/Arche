package com.blockinsight.basefi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 昨日订单数量
 * </p>
 *
 * @author Janin
 * @since 2021-02-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class YesterDayOrderNum extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

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


}
