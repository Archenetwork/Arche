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
        Web3j hbWeb3j = null;
        Web3j baWeb3j = null;
        Web3j ethWeb3j = null;
        List<Config> chainList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                .in(Config::getIndexName, "Hb_Chain", "Ba_Chain", "Eth_Chain"));
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30*1000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(30*1000, TimeUnit.MILLISECONDS);
        builder.readTimeout(30*1000, TimeUnit.MILLISECONDS);
        OkHttpClient httpClient = builder.build();
        for (Config config : chainList) {
            if ("Hb_Chain".equals(config.getIndexName())) {
                hbWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
            } else if ("Ba_Chain".equals(config.getIndexName())) {
                baWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
            } else if ("Eth_Chain".equals(config.getIndexName())) {
                ethWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
            }
        }
        // 获取当前节点最新区块号
        EthBlockNumber hbBlockNumber = hbWeb3j.ethBlockNumber().send();
        EthBlockNumber baBlockNumber = baWeb3j.ethBlockNumber().send();
        EthBlockNumber ethBlockNumber = ethWeb3j.ethBlockNumber().send();
        String hbEndBlockNumber = hbBlockNumber.getBlockNumber().toString();
        String baEndBlockNumber = baBlockNumber.getBlockNumber().toString();
        String ethEndBlockNumber = ethBlockNumber.getBlockNumber().toString();
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
            int count = iOrderService.count();
            Map<String, Object> orderNumber = new HashMap<>();
            orderNumber.put("totalNum", count);
            String splic = " contract_initialize_block_number + effective_height > ";
            String sql = " case chain_type when 1 then " + splic + hbEndBlockNumber +
                     " when 2 then " + splic + baEndBlockNumber +
                     " when 3 then" + splic + ethEndBlockNumber + " end ";
            int waitEffectNum = iOrderService.count(new LambdaUpdateWrapper<Order>()
                    .apply(sql));
            orderNumber.put("waitEffectNum", waitEffectNum);
            splic = " contract_initialize_block_number + effective_height < ";
            String splic2 = " and contract_initialize_block_number + delivery_height > ";
            sql = " case chain_type when 1 then " + splic + hbEndBlockNumber + splic2 + hbEndBlockNumber +
                    " when 2 then " + splic + baEndBlockNumber + splic2 + baEndBlockNumber +
                    " when 3 then" + splic + ethEndBlockNumber + splic2 + ethEndBlockNumber + " end ";
            int waitDeliveryNum = iOrderService.count(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode())
                    .apply(sql));
            orderNumber.put("waitDeliveryNum", waitDeliveryNum);
            result.put("orderNum", orderNumber);
            List<YesterDayOrderNum> ylist = iYesterDayOrderNumService.list(new LambdaUpdateWrapper<YesterDayOrderNum>()
                    .orderByDesc(BaseEntity::getCreateTime));
            YesterDayOrderNum yesterDayOrderNum;
            if (ylist.isEmpty()) {
                yesterDayOrderNum = new YesterDayOrderNum();
                yesterDayOrderNum.setTotalNum(0);
                yesterDayOrderNum.setWaitDeliveryNum(0);
                yesterDayOrderNum.setWaitEffectNum(0);
                yesterDayOrderNum.setChainType(BaseConstants.ChainType.HB.getCode());
                iYesterDayOrderNumService.save(yesterDayOrderNum);
                yesterDayOrderNum.setChainType(BaseConstants.ChainType.BA.getCode());
                iYesterDayOrderNumService.save(yesterDayOrderNum);
                yesterDayOrderNum.setChainType(BaseConstants.ChainType.ETH.getCode());
                iYesterDayOrderNumService.save(yesterDayOrderNum);
            } else {
                YesterDayOrderNum yesterDay = new YesterDayOrderNum();
                yesterDay.setWaitDeliveryNum(0);
                yesterDay.setWaitEffectNum(0);
                yesterDay.setTotalNum(0);
                for (int i = 0; i < ylist.size(); i++) {
                    if (i == 3)
                        break;
                    YesterDayOrderNum dayOrderNum = ylist.get(i);
                    yesterDay.setTotalNum(yesterDay.getTotalNum() + dayOrderNum.getTotalNum());
                    yesterDay.setWaitEffectNum(yesterDay.getWaitEffectNum() + dayOrderNum.getWaitEffectNum());
                    yesterDay.setWaitDeliveryNum(yesterDay.getWaitDeliveryNum() + dayOrderNum.getWaitDeliveryNum());
                }
                yesterDayOrderNum = yesterDay;
            }
            result.put("yesterDayOrderNum", yesterDayOrderNum);
            List<RevenuePool> list = iRevenuePoolService.list();
            if (!list.isEmpty()) {
                int revenuePool = 0;
                for (RevenuePool pool : list) {
                    revenuePool += Integer.parseInt(pool.getPrice());
                }
                result.put("revenuePool", revenuePool);
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
            int count = iOrderService.count(new LambdaUpdateWrapper<Order>().eq(Order::getChainType, chainType));
            String currentBlockNumber = "";
            if (chainType == BaseConstants.ChainType.HB.getCode()) {
                currentBlockNumber = hbEndBlockNumber;
            } else if (chainType == BaseConstants.ChainType.BA.getCode()) {
                currentBlockNumber = baEndBlockNumber;
            } else if (chainType == BaseConstants.ChainType.ETH.getCode()) {
                currentBlockNumber = ethEndBlockNumber;
            }
            Map<String, Object> orderNumber = new HashMap<>();
            orderNumber.put("totalNum", count);
            String sql = " contract_initialize_block_number + effective_height > " + currentBlockNumber;
            int waitEffectNum = iOrderService.count(new LambdaUpdateWrapper<Order>()
                    .apply(sql));
            orderNumber.put("waitEffectNum", waitEffectNum);
            sql = " contract_initialize_block_number + effective_height < " + currentBlockNumber +
            " and contract_initialize_block_number + delivery_height > " + currentBlockNumber;
            int waitDeliveryNum = iOrderService.count(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode())
                    .apply(sql));
            orderNumber.put("waitDeliveryNum", waitDeliveryNum);
            result.put("orderNum", orderNumber);
            List<YesterDayOrderNum> ylist = iYesterDayOrderNumService.list(new LambdaUpdateWrapper<YesterDayOrderNum>()
                    .eq(YesterDayOrderNum::getChainType, chainType).orderByDesc(BaseEntity::getCreateTime));
            YesterDayOrderNum yesterDayOrderNum;
            if (ylist.isEmpty()) {
                yesterDayOrderNum = new YesterDayOrderNum();
                yesterDayOrderNum.setTotalNum(0);
                yesterDayOrderNum.setWaitDeliveryNum(0);
                yesterDayOrderNum.setWaitEffectNum(0);
                yesterDayOrderNum.setChainType(chainType);
                iYesterDayOrderNumService.save(yesterDayOrderNum);
            } else {
                yesterDayOrderNum = ylist.get(0);
            }
            result.put("yesterDayOrderNum", yesterDayOrderNum);
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
