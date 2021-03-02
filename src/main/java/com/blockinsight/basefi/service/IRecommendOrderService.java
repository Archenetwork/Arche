package com.blockinsight.basefi.service;

import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.RecommendOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 推荐订单表 服务类
 * </p>
 *
 * @author Janin
 * @since 2021-01-23
 */
public interface IRecommendOrderService extends IService<RecommendOrder> {

    R recommendedOrder(Integer pageNumber, Integer pageSize);

    RecommendOrder getOrderNum(Integer chainType);
}
