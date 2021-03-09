package com.blockinsight.basefi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blockinsight.basefi.common.constant.BaseConstants;
import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.TokenPrice;
import com.blockinsight.basefi.mapper.TokenPriceMapper;
import com.blockinsight.basefi.service.ITokenPriceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 代币价格 服务实现类
 * </p>
 *
 * @author Janin
 * @since 2021-02-03
 */
@Slf4j
@Service
public class TokenPriceServiceImpl extends ServiceImpl<TokenPriceMapper, TokenPrice> implements ITokenPriceService {

    @Override
    public R saveTokenPrice(String name, String tokenAddr, String img, Integer chainType) {
        log.warn("新增基础代币参数 name:{} tokenAddr:{} img:{} chainType:{}", name, tokenAddr, img, chainType);
        TokenPrice tokenPrice = this.getOne(new LambdaUpdateWrapper<TokenPrice>()
                .eq(TokenPrice::getTokenAddr, tokenAddr)
                .eq(TokenPrice::getChainType, chainType));
        if (tokenPrice == null) {
            tokenPrice = new TokenPrice();
            tokenPrice.setName(name);
            tokenPrice.setImg(img);
            tokenPrice.setTokenAddr(tokenAddr);
            tokenPrice.setPrice("0");
            tokenPrice.setChainType(chainType);
            this.save(tokenPrice);
        } else {
            tokenPrice.setImg(img);
            tokenPrice.setTokenAddr(tokenAddr);
            this.updateById(tokenPrice);
        }
        return R.ok();
    }

    @Override
    public R getTokenPrice(String addr, Integer chainType) {
        TokenPrice tokenPrice = this.getOne(new LambdaUpdateWrapper<TokenPrice>().eq(TokenPrice::getChainType, chainType).eq(TokenPrice::getTokenAddr, addr));
        if (tokenPrice == null) {
            tokenPrice = new TokenPrice();
            tokenPrice.setPrice("0");
        }
        return R.ok().put("data", tokenPrice);
    }
}
