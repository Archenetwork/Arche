package com.blockinsight.basefi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.RecommendOrder;
import com.blockinsight.basefi.mapper.RecommendOrderMapper;
import com.blockinsight.basefi.service.IRecommendOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 推荐订单表 服务实现类
 * </p>
 *
 * @author Janin
 * @since 2021-01-23
 */
@Slf4j
@Service
public class RecommendOrderServiceImpl extends ServiceImpl<RecommendOrderMapper, RecommendOrder> implements IRecommendOrderService {

    @Override
    public R recommendedOrder(Integer pageNumber, Integer pageSize) {
        log.warn("获取推荐交易对参数 pageNumber:{} pageSize:{}", pageNumber, pageSize);
        IPage<RecommendOrder> iPage = new Page<>(pageNumber, pageSize);
        return R.ok().put("data", this.page(iPage, new LambdaUpdateWrapper<RecommendOrder>().orderByDesc(RecommendOrder::getTotalNum)));
    }

    @Override
    public RecommendOrder getOrderNum(Integer chainType) {
        return baseMapper.getOrderNum(chainType);
    }
}
