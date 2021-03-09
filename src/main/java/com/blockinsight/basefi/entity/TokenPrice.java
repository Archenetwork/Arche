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
 * 代币价格
 * </p>
 *
 * @author Janin
 * @since 2021-02-03
 */
@Data
@Accessors(chain = true)
public class TokenPrice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 代币名称
     */
    private String name;

    private String realName;

    /**
     * 代币价格 转换USDT
     */
    private String price;

    /**
     * 代币图片
     */
    private String img;

    /**
     * 代币地址
     */
    private String tokenAddr;

    /**
     * 链类型 1 火币 2 币安
     */
    private Integer chainType;

    /**
     *  类型 0 手动维护 1 自动维护
     */
    private Integer type;


}
