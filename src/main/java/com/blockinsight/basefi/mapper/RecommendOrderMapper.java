package com.blockinsight.basefi.mapper;

import com.blockinsight.basefi.entity.RecommendOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 * 推荐订单表 Mapper 接口
 * </p>
 *
 * @author Janin
 * @since 2021-01-23
 */
public interface RecommendOrderMapper extends BaseMapper<RecommendOrder> {

    RecommendOrder getOrderNum(@Param("chainType") Integer chainType);
}
