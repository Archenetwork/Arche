package com.blockinsight.basefi.service;

import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.TokenPrice;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 代币价格 服务类
 * </p>
 *
 * @author Janin
 * @since 2021-02-03
 */
public interface ITokenPriceService extends IService<TokenPrice> {

    R saveTokenPrice(String name, String tokenAddr, String img, Integer chainType);

    R getTokenPrice(String name, Integer chainType);
}
