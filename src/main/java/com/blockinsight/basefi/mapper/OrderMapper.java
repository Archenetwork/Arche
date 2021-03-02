package com.blockinsight.basefi.mapper;

import com.blockinsight.basefi.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
public interface OrderMapper extends BaseMapper<Order> {

    int updateOrderById(@Param("orderList") List<Order> orderList);
}
