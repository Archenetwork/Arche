package com.blockinsight.basefi.job;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blockinsight.basefi.common.constant.BaseConstants;
import com.blockinsight.basefi.common.constant.MessageContext;
import com.blockinsight.basefi.common.rabbitmq.provider.RabbitmqProvider;
import com.blockinsight.basefi.common.util.*;
import com.blockinsight.basefi.common.vo.DataDto;
import com.blockinsight.basefi.common.vo.Ticker;
import com.blockinsight.basefi.entity.*;
import com.blockinsight.basefi.service.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnExpression("'${spring.profiles.active}'.equals('test') || '${spring.profiles.active}'.equals('dev') || '${spring.profiles.active}'.equals('prod')")
public class BaseFiJob {

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IMessageService iMessageService;
    @Autowired
    private IRecommendOrderService iRecommendOrderService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private RabbitmqProvider rabbitmqProvider;
    @Autowired
    private ITokenPriceService iTokenPriceService;
    @Autowired
    private IYesterDayOrderNumService iYesterDayOrderNumService;
    @Autowired
    private IRevenuePoolService iRevenuePoolService;
    @Autowired
    private IConfigService iConfigService;

//    @Scheduled(cron = "0 */10 * * * ?")
    @Scheduled(cron = "*/5 * * * * ?")
    @Async
    public void sumEarnings() {
        String lockName = "tickersLock";
        try {
            // 没有获取锁 收益池
            if (!RedisLockUtils.lock(lockName, 30)) {
                log.error("更新资金池累计收益获取锁");
                return;
            }
            List<Type> inputs = new ArrayList<>();
            List<TypeReference<?>> outputs = new ArrayList<>();
            outputs.add(new TypeReference<Uint256>() {
            });
            List<Config> contractAddrList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                    .in(Config::getIndexName, "Hb_Swap_Contract_Address", "Ba_Swap_Contract_Address", "Eth_Swap_Contract_Address"));
            List<Config> chainList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                    .in(Config::getIndexName, "Hb_Chain", "Ba_Chain", "Eth_Chain"));
            Web3j hbWeb3j = null;
            Web3j baWeb3j = null;
            Web3j ethWeb3j = null;
            Web3j currentWeb3j = null;
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
            for (Config config : contractAddrList) {
                int chainType = 0;
                if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                    chainType = BaseConstants.ChainType.HB.getCode();
                    currentWeb3j = hbWeb3j;
                } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                    chainType = BaseConstants.ChainType.BA.getCode();
                    currentWeb3j = baWeb3j;
                } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                    chainType = BaseConstants.ChainType.ETH.getCode();
                    currentWeb3j = ethWeb3j;
                }
                String swapContractAddress = config.getIndexValue();
                inputs.clear();
                inputs.add(new Address(swapContractAddress));
                List<TokenPrice> list = iTokenPriceService.list(new LambdaUpdateWrapper<TokenPrice>()
                        .isNotNull(TokenPrice::getTokenAddr).eq(TokenPrice::getChainType, chainType));
                if (!list.isEmpty()) {
                    BigDecimal bigDecimal = new BigDecimal("0");
                    for (TokenPrice tokenPrice : list) {
                        List<Type> balanceOf = Web3JUtils.getEthCallValue("balanceOf", inputs, outputs, currentWeb3j, null, tokenPrice.getTokenAddr());
                        if (!balanceOf.isEmpty()) {
                            String price = tokenPrice.getPrice();
                            if (StringUtils.isNotBlank(price)) {
                                bigDecimal = bigDecimal.add(new BigDecimal(balanceOf.get(0).getValue().toString()).multiply(new BigDecimal(price)));
                            }
                        }
                    }
                    List<RevenuePool> revenuePools = iRevenuePoolService.list(new LambdaUpdateWrapper<RevenuePool>()
                            .eq(RevenuePool::getChainType, chainType));
                    if (revenuePools.isEmpty()) {
                        RevenuePool revenuePool = new RevenuePool();
                        revenuePool.setPrice(bigDecimal.toString());
                        revenuePool.setChainType(chainType);
                        iRevenuePoolService.save(revenuePool);
                    } else {
                        RevenuePool revenuePool = revenuePools.get(0);
                        revenuePool.setPrice(bigDecimal.toString());
                        iRevenuePoolService.updateById(revenuePool);
                    }
                }
            }
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            RedisLockUtils.unLock(lockName);
            log.error("更新资金池累计收益异常", e);
        }
    }

    @Scheduled(cron = "0 */20 * * * ?")
//    @Scheduled(cron = "*/5 * * * * ?")
    @Async
    public void tickers() {
        String lockName = "tickersLock";
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.error("更新代币价格未获取锁");
                return;
            }
            String json = HttpUtil.get("https://api-aws.huobi.pro/market/tickers");
//            log.warn("火币交易对:{}", json);
            Ticker ticker = JSONObject.parseObject(json, Ticker.class);
            if ("ok".equals(ticker.getStatus())) {
                List<DataDto> data = ticker.getData();
                List<DataDto> usdt = data.stream().filter(e -> e.getSymbol().substring(e.getSymbol().length() - 4).contains("usdt")).collect(Collectors.toList());
                log.warn("usdt交易对:{} usdtSize:{}", usdt.toString(), usdt.size());
                if (!usdt.isEmpty()) {
                    for (DataDto dataDto : usdt) {
                        String name = dataDto.getSymbol().substring(0, dataDto.getSymbol().length() - 4);
                        List<TokenPrice> tokenPrices = iTokenPriceService.list(new LambdaUpdateWrapper<TokenPrice>()
                                .eq(TokenPrice::getName, name).or().eq(TokenPrice::getRealName, name));
                        if (tokenPrices.isEmpty()) {
                            TokenPrice tokenPrice = new TokenPrice();
                            tokenPrice.setPrice(dataDto.getClose());
                            tokenPrice.setName(name);
                            tokenPrice.setChainType(BaseConstants.ChainType.HB.getCode());
                            iTokenPriceService.save(tokenPrice);
                        }
                        for (TokenPrice price : tokenPrices) {
                            if (price.getType() == 0) {
                                price.setPrice(dataDto.getClose());
                                iTokenPriceService.updateById(price);
                            }
                        }
                    }
                }
            }
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            RedisLockUtils.unLock(lockName);
            log.error("更新代币价格异常", e);
        }

    }

    @Scheduled(cron = "0 0 8 * * ?")
//    @Scheduled(cron = "*/5 * * * * ?")
    @Async
    public void yesterDayHomePage() {
        String lockName = "yesterDayHomePage";
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.error("新增昨日首页数据未获取锁");
                return;
            }
            List<Integer> list = new ArrayList<>();
            list.add(BaseConstants.ChainType.HB.getCode());
            list.add(BaseConstants.ChainType.BA.getCode());
            list.add(BaseConstants.ChainType.ETH.getCode());
            for (Integer chainType : list) {
                RecommendOrder orderNum = iRecommendOrderService.getOrderNum(chainType);
                log.warn("昨日订单数据 chainType:{} orderNum:{}", chainType, orderNum.toString());
                YesterDayOrderNum yesterDayOrderNum = new YesterDayOrderNum();
                BeanCopyUtils.copyNotNullProperties(orderNum, yesterDayOrderNum);
                iYesterDayOrderNumService.save(yesterDayOrderNum);
            }
            RedisLockUtils.unLock(lockName);
        }catch (Exception e) {
            RedisLockUtils.unLock(lockName);
            log.error("新增昨日首页数据异常", e);
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
//    @Scheduled(cron = "*/5 * * * * ?")
    @Async
    public void saveMessage() throws Exception {
        String lockName = "saveMessage";
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.error("定时发送短信未获取锁");
                return;
            }
            List<Config> contractAddrList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                    .in(Config::getIndexName, "Hb_Swap_Contract_Address", "Ba_Swap_Contract_Address", "Eth_Swap_Contract_Address"));
            List<Config> chainList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                    .in(Config::getIndexName, "Hb_Chain", "Ba_Chain", "Eth_Chain"));
            Web3j hbWeb3j = null;
            Web3j baWeb3j = null;
            Web3j ethWeb3j = null;
            Web3j currentWeb3j = null;
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
            for (Config config : contractAddrList) {
                int chainType = 0;
                long math = 0;
                if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                    currentWeb3j = hbWeb3j;
                    chainType = BaseConstants.ChainType.HB.getCode();
                    math = 15 * 600 / BaseConstants.hbBlockNumberTime;
                } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                    currentWeb3j = baWeb3j;
                    chainType = BaseConstants.ChainType.BA.getCode();
                    math = 15 * 600 / BaseConstants.baBlockNumberTime;
                } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                    currentWeb3j = ethWeb3j;
                    chainType = BaseConstants.ChainType.ETH.getCode();
                    math = 15 * 600 / BaseConstants.ethBlockNumberTime;
                }
                EthBlockNumber ethBlockNumber = currentWeb3j.ethBlockNumber().send();
                // 计算15分钟多少个区块 15 * 60 / 3 = 300
                int blockNumber = (int) (ethBlockNumber.getBlockNumber().intValue() - math);
                // 查询到期未支付保证金的订单
                List<Order> earnestMoneyList = iOrderService.list(new LambdaUpdateWrapper<Order>()
                        .eq(Order::getContractStatus, 0).apply(" contract_initialize_block_number + effective_height < " +
                                blockNumber).ne(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode())
                        .eq(Order::getChainType, chainType));
                for (Order order : earnestMoneyList) {
                    String orderNum = getOrderNum(order.getOrderNum());
                    if (order.getBuyerPaid().compareTo(order.getBuyerEarnestMoney()) < 0) {
                        createMessage(chainType, order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.TRANSACTION.getMes(),
                                MessageContext.noPayEarnestMoneyOrderInvalidation(orderNum, DateUtils.formatDetailDate(order.getOrderTakeEffectTime())));
                    }
                    if (order.getSellerPaid().compareTo(order.getSellerEarnestMoney()) < 0) {
                        createMessage(chainType, order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.TRANSACTION.getMes(),
                                MessageContext.noPayEarnestMoneyOrderInvalidation(orderNum, DateUtils.formatDetailDate(order.getOrderTakeEffectTime())));
                    }
                    order.setContractStatus(1);
                }
                // 查询到期代币的订单
                List<Order> deliveryList = iOrderService.list(new LambdaUpdateWrapper<Order>()
                        .eq(Order::getContractStatus, 0).apply(" contract_initialize_block_number + delivery_height <" +
                                blockNumber).ne(Order::getDepositStatus, BaseConstants.DepositStatus.ALL_MAKE_UP.getCode()));
                for (Order order : deliveryList) {
                    String orderNum = getOrderNum(order.getOrderNum());
                    if (order.getBuyerMakeUp().compareTo(order.getBuyerDeliveryQuantity()) < 0) {
                        createMessage(chainType, order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.TRANSACTION.getMes(),
                                MessageContext.noMakeUpTokenInvalidation(orderNum, DateUtils.formatDetailDate(order.getOrderDeliveryTime())));
                    }
                    if (order.getSellerMakeUp().compareTo(order.getSellerDeliveryQuantity()) < 0) {
                        createMessage(chainType, order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.TRANSACTION.getMes(),
                                MessageContext.noMakeUpTokenInvalidation(orderNum, DateUtils.formatDetailDate(order.getOrderDeliveryTime())));
                    }
                    order.setContractStatus(2);
                }
                if (!earnestMoneyList.isEmpty()) {
                    iOrderService.updateOrderById(earnestMoneyList);
                }
                if (!deliveryList.isEmpty()) {
                    iOrderService.updateOrderById(deliveryList);
                }
            }
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            RedisLockUtils.unLock(lockName);
            log.error("定时发送短信异常", e);
        }
    }

    @Scheduled(cron = "0 */10 * * * ?")
//    @Scheduled(cron = "*/59 * * * * ?")
    @Async
    public void updateRecommendOrder() throws Exception {
        String lockName = "updateRecommendOrder";
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.error("推荐交易对未获取锁");
                return;
            }
            log.warn("推荐交易对定时任务");
            List<Config> chainList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                    .in(Config::getIndexName, "Hb_Chain", "Ba_Chain", "Eth_Chain"));
            Web3j currentWeb3j = null;
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30*1000, TimeUnit.MILLISECONDS);
            builder.writeTimeout(30*1000, TimeUnit.MILLISECONDS);
            builder.readTimeout(30*1000, TimeUnit.MILLISECONDS);
            OkHttpClient httpClient = builder.build();
            for (Config config : chainList) {
                int chainType = 0;
                long math = 0;
                if ("Hb_Chain".equals(config.getIndexName())) {
                    currentWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                    chainType = BaseConstants.ChainType.HB.getCode();
                    math = 15 * 600 / BaseConstants.hbBlockNumberTime;
                } else if ("Ba_Chain".equals(config.getIndexName())) {
                    currentWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                    chainType = BaseConstants.ChainType.BA.getCode();
                    math = 15 * 600 / BaseConstants.baBlockNumberTime;
                } else if ("Eth_Chain".equals(config.getIndexName())) {
                    currentWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                    chainType = BaseConstants.ChainType.ETH.getCode();
                    math = 15 * 600 / BaseConstants.ethBlockNumberTime;
                }
                EthBlockNumber ethBlockNumber = currentWeb3j.ethBlockNumber().send();
                // 计算15分钟多少个区块 15 * 60 / 3 = 300
                int blockNumber = (int) (ethBlockNumber.getBlockNumber().intValue() - math);
                // 获取订单生效时间过去的成单订单
                List<Order> earnestMoneyList = iOrderService.list(new LambdaUpdateWrapper<Order>()
                        .eq(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode()).apply(" contract_initialize_block_number + effective_height <" +
                                blockNumber).eq(Order::getRecommendOrderStatus, 0)
                        .eq(Order::getChainType, chainType));
                log.warn("获取订单生效时间过去的成单订单 size:{}", earnestMoneyList.size());
                // 获取订单生效时间过去的未成单订单
                List<Order> earnestMoneyList2 = iOrderService.list(new LambdaUpdateWrapper<Order>()
                        .ne(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode()).apply(" contract_initialize_block_number + effective_height <" +
                                blockNumber).eq(Order::getRecommendOrderStatus, 0)
                        .eq(Order::getChainType, chainType));
                log.warn("获取订单生效时间过去的未成单订单 size:{}", earnestMoneyList2.size());
                // 获取已交割订单
                List<Order> deliveryList = iOrderService.list(new LambdaUpdateWrapper<Order>()
                        .eq(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode()).apply(" contract_initialize_block_number + delivery_height <" +
                                blockNumber).eq(Order::getRecommendOrderStatus, 1)
                        .eq(Order::getChainType, chainType));
                log.warn("获取已交割订单 size:{}", deliveryList.size());

                for (Order order : earnestMoneyList2) {
                    RecommendOrder one = iRecommendOrderService.getOne(new LambdaUpdateWrapper<RecommendOrder>().
                            apply("(buyer_token_addr = '" + order.getBuyerSubjectMatterAddr() + "' and seller_token_addr = '" +
                                    order.getSellerSubjectMatterAddr() + "') or (buyer_token_addr = '" +
                                    order.getSellerSubjectMatterAddr() + "' and seller_token_addr = '" + order.getBuyerSubjectMatterAddr() + "')")
                            .eq(RecommendOrder::getChainType, chainType));
                    if (one != null) {
                        one.setType(4);
                        rabbitmqProvider.recommendOrderAdd(one);
                        order.setRecommendOrderStatus(3);
                    }
                    tokenSumCountDel(order, BaseConstants.del2, chainType);
                }
                for (Order order : earnestMoneyList) {
                    RecommendOrder one = iRecommendOrderService.getOne(new LambdaUpdateWrapper<RecommendOrder>().
                            apply("(buyer_token_addr = '" + order.getBuyerSubjectMatterAddr() + "' and seller_token_addr = '" +
                                    order.getSellerSubjectMatterAddr() + "') or (buyer_token_addr = '" +
                                    order.getSellerSubjectMatterAddr() + "' and seller_token_addr = '" + order.getBuyerSubjectMatterAddr() + "')")
                            .eq(RecommendOrder::getChainType, chainType));
                    if (one != null) {
                        one.setType(2);
                        rabbitmqProvider.recommendOrderAdd(one);
                        order.setRecommendOrderStatus(1);
                    }
                }

                for (Order order : deliveryList) {
                    RecommendOrder one = iRecommendOrderService.getOne(new LambdaUpdateWrapper<RecommendOrder>().
                            apply("(buyer_token_addr = '" + order.getBuyerSubjectMatterAddr() + "' and seller_token_addr = '" +
                                    order.getSellerSubjectMatterAddr() + "') or (buyer_token_addr = '" +
                                    order.getSellerSubjectMatterAddr() + "' and seller_token_addr = '" + order.getBuyerSubjectMatterAddr() + "')")
                            .eq(RecommendOrder::getChainType, chainType));
                    if (one != null) {
                        one.setType(3);
                        rabbitmqProvider.recommendOrderAdd(one);
                        order.setRecommendOrderStatus(2);
                    }
                    tokenSumCountDel(order, BaseConstants.del, chainType);
                }
                if (!earnestMoneyList.isEmpty()) {
                    iOrderService.updateOrderById(earnestMoneyList);
                }
                if (!deliveryList.isEmpty()) {
                    iOrderService.updateOrderById(deliveryList);
                }

                if (!earnestMoneyList2.isEmpty()) {
                    iOrderService.updateOrderById(earnestMoneyList2);
                }
            }
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            RedisLockUtils.unLock(lockName);
            log.error("定时更新推荐订单表异常", e);
        }
    }

    private void tokenSumCountDel(Order order, int type, int chainType) {
        LockUpRecord.TokenSumAddParam tokenSumAddParam = new LockUpRecord.TokenSumAddParam();
        tokenSumAddParam.setTokenAddr(order.getBuyerSubjectMatterAddr());
        tokenSumAddParam.setCount(MathUtils.weiToEth(new BigDecimal(order.getBuyerMakeUp()), 18).toString());
        tokenSumAddParam.setUuid(order.getOrderNum() + order.getBuyerAddr());
        tokenSumAddParam.setType(type);
        tokenSumAddParam.setChainType(chainType);
        rabbitmqProvider.tokenSumCountAdd(tokenSumAddParam);
        tokenSumAddParam.setTokenAddr(order.getSellerSubjectMatterAddr());
        tokenSumAddParam.setCount(MathUtils.weiToEth(new BigDecimal(order.getSellerMakeUp()), 18).toString());
        tokenSumAddParam.setUuid(order.getOrderNum() + order.getSellerAddr());
        tokenSumAddParam.setType(type);
        tokenSumAddParam.setChainType(chainType);
        rabbitmqProvider.tokenSumCountAdd(tokenSumAddParam);
    }

    public void createMessage(Integer chainType, String orderNum, String userAddr, String messageTypeName, String messageContext) {
        Message message = new Message();
        message.setOrderNum(orderNum);
        message.setUserAddr(userAddr);
        message.setMessageTypeName(messageTypeName);
        message.setMessageContext(messageContext);
        message.setIsRead(0);
        message.setChainType(chainType);
        iMessageService.save(message);
    }

    public String getOrderNum(String orderNum) {
        return orderNum.substring(0, 4) + "****" + orderNum.substring(orderNum.length() - 4);
    }

}
