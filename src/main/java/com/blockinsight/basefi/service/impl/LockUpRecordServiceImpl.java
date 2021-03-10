package com.blockinsight.basefi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blockinsight.basefi.common.constant.BaseConstants;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.*;
import com.blockinsight.basefi.mapper.LockUpRecordMapper;
import com.blockinsight.basefi.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.models.auth.In;
import net.bytebuddy.asm.Advice;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;

import javax.swing.*;
import javax.xml.ws.WebEndpoint;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Janin
 * @since 2021-02-02
 */
@Service
public class LockUpRecordServiceImpl extends ServiceImpl<LockUpRecordMapper, LockUpRecord> implements ILockUpRecordService {

    @Autowired
    private IRecommendOrderService iRecommendOrderService;
    @Autowired
    private IYesterDayOrderNumService iYesterDayOrderNumService;
    @Autowired
    private IRevenuePoolService iRevenuePoolService;
    @Autowired
    private IConfigService iConfigService;
    @Autowired
    private IOrderService iOrderService;
    @Override
    public R homePage(Integer chainType) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (chainType == BaseConstants.ChainType.ALL.getCode()) {
            Map<String, Object> sum = baseMapper.getSumPriceAndCurrentPrice(null);
            if (sum != null) {
                result.putAll(sum);
            } else {
                result.put("sumPrice", "0");
                result.put("currentPrice", "0");
            }
            List<Map<String, Object>> topThreeCurrentPriceToken = baseMapper.getTopThreeCurrentPriceToken(null);
            result.put("topThree", topThreeCurrentPriceToken);
            List<RevenuePool> list = iRevenuePoolService.list();
            if (!list.isEmpty()) {
                BigDecimal revenuePool = new BigDecimal("0");
                for (RevenuePool pool : list) {
                    revenuePool = revenuePool.add(new BigDecimal(pool.getPrice()));
                }
                result.put("revenuePool", revenuePool.toString());
            } else {
                result.put("revenuePool", "0");
            }
        } else {
            Map<String, Object> sum = baseMapper.getSumPriceAndCurrentPrice(chainType);
            if (sum != null) {
                result.putAll(sum);
            } else {
                result.put("sumPrice", "0");
                result.put("currentPrice", "0");
            }
            List<Map<String, Object>> topThreeCurrentPriceToken = baseMapper.getTopThreeCurrentPriceToken(chainType);
            result.put("topThree", topThreeCurrentPriceToken);
            List<RevenuePool> list = iRevenuePoolService.list(new LambdaUpdateWrapper<RevenuePool>().eq(RevenuePool::getChainType, chainType));
            if (!list.isEmpty()) {
                result.put("revenuePool", list.get(0).getPrice());
            } else {
                result.put("revenuePool", "0");
            }
        }
        return R.ok().put("data", result);
    }
}
