package com.blockinsight.basefi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 配置表
 * </p>
 *
 * @author Janin
 * @since 2021-01-09
 */
@Data
@Accessors(chain = true)
public class Config {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 索引名
     */
    private String indexName;

    /**
     * 索引值
     */
    private String indexValue;


}
