package com.blockinsight.basefi.common.rabbitmq.business;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blockinsight.basefi.common.constant.BaseConstants;
import com.blockinsight.basefi.common.constant.MessageContext;
import com.blockinsight.basefi.common.rabbitmq.provider.RabbitmqProvider;
import com.blockinsight.basefi.common.util.*;
import com.blockinsight.basefi.entity.*;
import com.blockinsight.basefi.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Slf4j
@Component
public class OrderBusiness {

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private RabbitmqProvider rabbitmqProvider;
    @Autowired
    private IEarnestMoneyRecordService iEarnestMoneyRecordService;
    @Autowired
    private IDepositRecordService iDepositRecordService;
    @Autowired
    private IMessageService iMessageService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private IRecommendOrderService iRecommendOrderService;
    @Autowired
    private ILockUpRecordService iLockUpRecordService;
    @Autowired
    private ITokenPriceService iTokenPriceService;

    @Async
    public void listenerOrderCreate(String msg) {
        Order order = JSONObject.parseObject(msg, Order.class);
        Order oneOrder = iOrderService.getOne(new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderNum, order.getOrderNum())
                .eq(Order::getChainType, order.getChainType()));
        if (oneOrder == null) {
            order.setOrderStatus(BaseConstants.OrderStatus.WAIT_INITIALIZE.getCode());
            order.setBuyerPaid("0");
            order.setSellerPaid("0");
            order.setBuyerMakeUp("0");
            order.setSellerMakeUp("0");
            order.setContractStatus(0);
            order.setDepositStatus(0);
            order.setRecommendOrderStatus(0);
            TokenPrice buyerTokenPrice = iTokenPriceService.getOne(new LambdaUpdateWrapper<TokenPrice>().eq(TokenPrice::getTokenAddr, order.getBuyerSubjectMatterAddr()));
            if (buyerTokenPrice != null) {
                order.setBuyerSubjectMatter(buyerTokenPrice.getName());
                order.setBuyerSubjectMatterImg(buyerTokenPrice.getImg());
            }
            TokenPrice sellerTokenPrice = iTokenPriceService.getOne(new LambdaUpdateWrapper<TokenPrice>().eq(TokenPrice::getTokenAddr, order.getSellerSubjectMatterAddr()));
            if (sellerTokenPrice != null) {
                order.setSellerSubjectMatter(sellerTokenPrice.getName());
                order.setSellerSubjectMatterImg(sellerTokenPrice.getImg());
            }
            iOrderService.save(order);
            // 订单标记
            log.warn("设置订单创建标记 orderNum:{}", order.getOrderNum());
            redisUtils.set("orderCreate" + order.getOrderNum(), true);
        }
    }

    @Async
    public void listenerOrderInitialize(String msg) {
        Order order = JSONObject.parseObject(msg, Order.class);
        boolean is = true;
        while (is) {
            Object o = redisUtils.get("orderCreate" + order.getOrderNum());
            if (o != null) {
                log.warn("订单创建 -> 订单初始化 orderNum:{}", order.getOrderNum());
                is = false;
                redisUtils.del("orderCreate" + order.getOrderNum());
            } else {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("订单初始化事件异常", e);
                }
                log.warn("订单初始化快于订单创建 等待2s orderNum:{} chainType:{}", order.getOrderNum(), order.getChainType());
            }
        }
        try {
            Order oneOrder = iOrderService.getOne(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getOrderNum, order.getOrderNum())
                    .eq(Order::getChainType, order.getChainType()));
            // 订单创建区块
            Integer effectiveHeight = order.getEffectiveHeight();
            long effectiveHeightL = effectiveHeight.longValue();
            long math = 0;
            if (order.getChainType() == BaseConstants.ChainType.HB.getCode()) {
                math = BaseConstants.hbBlockNumberTime;
            } else if (order.getChainType() == BaseConstants.ChainType.BA.getCode()) {
                math = BaseConstants.baBlockNumberTime;
            } else if (order.getChainType() == BaseConstants.ChainType.ETH.getCode()) {
                math = BaseConstants.ethBlockNumberTime;
            }
            long effective = effectiveHeightL * math * 100;
            Integer deliveryHeight = order.getDeliveryHeight();
            long deliveryHeightL = deliveryHeight.longValue();
            long delivery = deliveryHeightL * math * 100;
            Date contractInitializeTime = order.getContractInitializeTime();
            long time = contractInitializeTime.getTime();
            effective = time + effective;
            delivery = time + delivery;
            order.setOrderTakeEffectTime(new Date(effective));
            order.setOrderDeliveryTime(new Date(delivery));
            String orderNum = oneOrder.getOrderNum();
            orderNum = getOrderNum(orderNum);
            if (order.getBuyerAddr().equals("0x0000000000000000000000000000000000000000") && order.getSellerAddr().equals("0x0000000000000000000000000000000000000000")) {
                order.setOrderStatus(BaseConstants.OrderStatus.INITIALIZE.getCode());
            } else if (order.getBuyerAddr().equals("0x0000000000000000000000000000000000000000") && !order.getSellerAddr().equals("0x0000000000000000000000000000000000000000")){
                order.setOrderStatus(BaseConstants.OrderStatus.WAIT_BUYER.getCode());
                createMessage(order.getChainType(), oneOrder.getOrderNum(), oneOrder.getSellerAddr(), BaseConstants.MessageType.CREATE_ORDER.getMes(), MessageContext.createOrderMessage(orderNum));
                createMessage(order.getChainType(), oneOrder.getOrderNum(), oneOrder.getSellerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.sellerEarnestMoneyTime(orderNum, DateUtils.formatDetailDate(oneOrder.getOrderTakeEffectTime())));
            } else if (!order.getBuyerAddr().equals("0x0000000000000000000000000000000000000000") && order.getSellerAddr().equals("0x0000000000000000000000000000000000000000")){
                order.setOrderStatus(BaseConstants.OrderStatus.WAIT_SELLER.getCode());
                createMessage(order.getChainType(), oneOrder.getOrderNum(), oneOrder.getBuyerAddr(), BaseConstants.MessageType.CREATE_ORDER.getMes(), MessageContext.createOrderMessage(orderNum));
                createMessage(order.getChainType(), oneOrder.getOrderNum(), oneOrder.getBuyerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.buyerEarnestMoneyTime(orderNum, DateUtils.formatDetailDate(oneOrder.getOrderTakeEffectTime())));
            } else if (!order.getBuyerAddr().equals("0x0000000000000000000000000000000000000000") && !order.getSellerAddr().equals("0x0000000000000000000000000000000000000000")){
                order.setOrderStatus(BaseConstants.OrderStatus.BUYER_SELLER.getCode());
                createMessage(order.getChainType(), oneOrder.getOrderNum(), oneOrder.getSellerAddr(), BaseConstants.MessageType.CREATE_ORDER.getMes(), MessageContext.createOrderMessage(orderNum));
                createMessage(order.getChainType(), oneOrder.getOrderNum(), oneOrder.getBuyerAddr(), BaseConstants.MessageType.CREATE_ORDER.getMes(), MessageContext.createOrderMessage(orderNum));
                createMessage(order.getChainType(), oneOrder.getOrderNum(), oneOrder.getSellerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.sellerEarnestMoneyTime(orderNum, DateUtils.formatDetailDate(oneOrder.getOrderTakeEffectTime())));
                createMessage(order.getChainType(), oneOrder.getOrderNum(), oneOrder.getBuyerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.buyerEarnestMoneyTime(orderNum, DateUtils.formatDetailDate(oneOrder.getOrderTakeEffectTime())));
            }
            order.setEarnestMoneyStatus(BaseConstants.EarnestMoneyStatus.NOT_PAY.getCode());
            order.setOrderId(oneOrder.getOrderId());
            iOrderService.updateById(order);

            // 增加推荐订单
            RecommendOrder recommendOrder = new RecommendOrder();
            recommendOrder.setOrderNum(orderNum);
            recommendOrder.setBuyerTokenAddr(oneOrder.getBuyerSubjectMatterAddr());
            recommendOrder.setSellerTokenAddr(oneOrder.getSellerSubjectMatterAddr());
            recommendOrder.setType(1);
            recommendOrder.setChainType(order.getChainType());
            rabbitmqProvider.recommendOrderAdd(recommendOrder);


            // 保存给创建人订单创建消息
            createMessage(order.getChainType(), oneOrder.getOrderNum(), oneOrder.getContractCreatorAddr(), BaseConstants.MessageType.CREATE_ORDER.getMes(), MessageContext.createOrderMessage(orderNum));
            // 订单初始化指定初始化等待5s
            log.warn("订单初始化设置标记 orderNum:{}", oneOrder.getOrderNum());
            redisUtils.set("orderInitialize" + order.getOrderNum(), true);
        } catch (Exception e) {
            log.error("订单初始化事件异常 orderNum:{}", order.getOrderNum(), e);
        }
    }

    @Async
    public void listenerBuyerPayEarnestMoney(String msg) {
        EarnestMoneyRecord.EarnestMoneyRecordParam earnestMoneyRecordParam = JSONObject.parseObject(msg, EarnestMoneyRecord.EarnestMoneyRecordParam.class);
        boolean is = true;
        while (is) {
            Object o = redisUtils.get("orderInitialize" + earnestMoneyRecordParam.getOrderNum());
            if (o != null) {
                log.warn("订单初始化成功 -> 买家支付保证金 orderNum:{}", earnestMoneyRecordParam.getOrderNum());
                is = false;
            } else {
                try {
                    log.warn("买家支付保证金等待2s orderNum:{}", earnestMoneyRecordParam.getOrderNum());
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("买家支付保证金事件异常", e);
                }
            }
        }
        String lockName = "payEarnestMoney" + earnestMoneyRecordParam.getOrderNum();
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.warn("买家支付保证金事件未获取锁 orderNum:{}", earnestMoneyRecordParam.getOrderNum());
                Thread.sleep(5000);
                listenerBuyerPayEarnestMoney(msg);
            }
            Order order = iOrderService.getOne(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getOrderNum, earnestMoneyRecordParam.getOrderNum())
                    .eq(Order::getChainType, earnestMoneyRecordParam.getChainType()));
            order.setBuyerAddr(earnestMoneyRecordParam.getUserAddr());
            order.setBuyerReferer(earnestMoneyRecordParam.getReferer());
            order.setBuyerMakeUp(order.getBuyerEarnestMoney());
            if (order.getEarnestMoneyStatus() == BaseConstants.EarnestMoneyStatus.SELLER_PAY.getCode()) {
                order.setEarnestMoneyStatus(BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode());
            } else {
                order.setEarnestMoneyStatus(BaseConstants.EarnestMoneyStatus.BUYER_PAY.getCode());
            }
            if (order.getSellerAddr().equals("0x0000000000000000000000000000000000000000")) {
                order.setOrderStatus(BaseConstants.OrderStatus.WAIT_SELLER.getCode());
            } else {
                order.setOrderStatus(BaseConstants.OrderStatus.BUYER_SELLER.getCode());
            }
            order.setBuyerPaid(order.getBuyerEarnestMoney());
            iOrderService.updateById(order);

            LockUpRecord.TokenSumAddParam tokenSumAddParam = new LockUpRecord.TokenSumAddParam();
            tokenSumAddParam.setTokenAddr(order.getBuyerSubjectMatterAddr());
            tokenSumAddParam.setCount(MathUtils.weiToEth(new BigDecimal(order.getBuyerEarnestMoney()), 18).toString());
            tokenSumAddParam.setUuid(order.getOrderNum() + order.getBuyerAddr());
            tokenSumAddParam.setType(BaseConstants.add);
            tokenSumAddParam.setChainType(order.getChainType());
            rabbitmqProvider.tokenSumCountAdd(tokenSumAddParam);

            EarnestMoneyRecord one = iEarnestMoneyRecordService.getOne(new LambdaUpdateWrapper<EarnestMoneyRecord>()
                    .eq(EarnestMoneyRecord::getTransactionHash, earnestMoneyRecordParam.getTransactionHash())
                    .eq(EarnestMoneyRecord::getUserAddr, earnestMoneyRecordParam.getUserAddr())
                    .eq(EarnestMoneyRecord::getChainType, earnestMoneyRecordParam.getChainType()));
            if (one == null) {
                EarnestMoneyRecord earnestMoneyRecord = new EarnestMoneyRecord();
                BeanCopyUtils.copyNotNullProperties(earnestMoneyRecordParam, earnestMoneyRecord);
                earnestMoneyRecord.setType(BaseConstants.UserType.BUYER.getCode());
                earnestMoneyRecord.setPrice(order.getBuyerEarnestMoney());
                earnestMoneyRecord.setChainType(order.getChainType());
                iEarnestMoneyRecordService.save(earnestMoneyRecord);
            }
            String orderNum = getOrderNum(order.getOrderNum());
            // 买方在该订单中支付完成保证金后，提示买方
            createMessage(order.getChainType(), order.getOrderNum(), earnestMoneyRecordParam.getUserAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.buyerEarnestMoney(orderNum, order.getBuyerEarnestMoney()));
            // 买方在该订单中支付完成保证金后，提示创建人
            createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.creatorBuyerEarnestMoney(earnestMoneyRecordParam.getUserAddr(), orderNum, order.getBuyerEarnestMoney()));
            // 双方补足保证金
            if (order.getEarnestMoneyStatus() == BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode()) {
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.allPayEarnestMoney(orderNum));
                createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.allPayEarnestMoney(orderNum));
                createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.allPayEarnestMoney(orderNum));
            } else {
                createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.waitSellerPayEarnestMoney(orderNum));
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.waitSellerPayEarnestMoney(orderNum));
            }
            log.warn("设置买家支付保证金标记 orderNum:{}", order.getOrderNum());
            redisUtils.set("buyerPayEarnestMoney" + order.getOrderNum(), true);
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            log.error("买家支付保证金事件异常 orderNum:{}", earnestMoneyRecordParam.getOrderNum(), e);
            RedisLockUtils.unLock(lockName);
        }
    }

    @Async
    public void listenerSellerPayEarnestMoney(String msg) {
        EarnestMoneyRecord.EarnestMoneyRecordParam earnestMoneyRecordParam = JSONObject.parseObject(msg, EarnestMoneyRecord.EarnestMoneyRecordParam.class);
        boolean is = true;
        while (is) {
            Object o = redisUtils.get("orderInitialize" + earnestMoneyRecordParam.getOrderNum());
            if (o != null) {
                log.warn("订单初始化成功 -> 卖家支付保证金 orderNum:{}", earnestMoneyRecordParam.getOrderNum());
                is = false;
            } else {
                try {
                    log.warn("卖家支付保证金等待2s orderNum:{}", earnestMoneyRecordParam.getOrderNum());
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("卖家支付保证金事件异常", e);
                }
            }
        }
        String lockName = "payEarnestMoney" + earnestMoneyRecordParam.getOrderNum();
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.warn("卖家支付保证金事件未获取锁 orderNum:{}", earnestMoneyRecordParam.getOrderNum());
                Thread.sleep(5000);
                listenerSellerPayEarnestMoney(msg);
            }
            Order order = iOrderService.getOne(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getOrderNum, earnestMoneyRecordParam.getOrderNum())
                    .eq(Order::getChainType, earnestMoneyRecordParam.getChainType()));
            order.setSellerAddr(earnestMoneyRecordParam.getUserAddr());
            order.setSellerReferer(earnestMoneyRecordParam.getReferer());
            order.setSellerMakeUp(order.getSellerEarnestMoney());
            if (order.getEarnestMoneyStatus() == BaseConstants.EarnestMoneyStatus.BUYER_PAY.getCode()) {
                order.setEarnestMoneyStatus(BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode());
            } else {
                order.setEarnestMoneyStatus(BaseConstants.EarnestMoneyStatus.SELLER_PAY.getCode());
            }
            if (order.getBuyerAddr().equals("0x0000000000000000000000000000000000000000")) {
                order.setOrderStatus(BaseConstants.OrderStatus.WAIT_BUYER.getCode());
            } else {
                order.setOrderStatus(BaseConstants.OrderStatus.BUYER_SELLER.getCode());
            }
            order.setSellerPaid(order.getSellerEarnestMoney());
            iOrderService.updateById(order);

            LockUpRecord.TokenSumAddParam tokenSumAddParam = new LockUpRecord.TokenSumAddParam();
            tokenSumAddParam.setTokenAddr(order.getSellerSubjectMatterAddr());
            tokenSumAddParam.setCount(MathUtils.weiToEth(new BigDecimal(order.getSellerEarnestMoney()), 18).toString());
            tokenSumAddParam.setUuid(order.getOrderNum() + order.getSellerAddr());
            tokenSumAddParam.setType(BaseConstants.add);
            tokenSumAddParam.setChainType(order.getChainType());
            rabbitmqProvider.tokenSumCountAdd(tokenSumAddParam);

            EarnestMoneyRecord one = iEarnestMoneyRecordService.getOne(new LambdaUpdateWrapper<EarnestMoneyRecord>()
                    .eq(EarnestMoneyRecord::getTransactionHash, earnestMoneyRecordParam.getTransactionHash())
                    .eq(EarnestMoneyRecord::getUserAddr, earnestMoneyRecordParam.getUserAddr())
                    .eq(EarnestMoneyRecord::getChainType, earnestMoneyRecordParam.getChainType()));
            if (one == null) {
                EarnestMoneyRecord earnestMoneyRecord = new EarnestMoneyRecord();
                BeanCopyUtils.copyNotNullProperties(earnestMoneyRecordParam, earnestMoneyRecord);
                earnestMoneyRecord.setType(BaseConstants.UserType.SELLER.getCode());
                earnestMoneyRecord.setPrice(order.getSellerEarnestMoney());
                earnestMoneyRecord.setChainType(order.getChainType());
                iEarnestMoneyRecordService.save(earnestMoneyRecord);
            }
            String orderNum = getOrderNum(order.getOrderNum());
            // 卖方在该订单中支付完成保证金后，提示买方
            createMessage(order.getChainType(), order.getOrderNum(), earnestMoneyRecordParam.getUserAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.buyerEarnestMoney(orderNum, order.getSellerEarnestMoney()));
            // 卖方在该订单中支付完成保证金后，提示创建人
            createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.creatorSellerEarnestMoney(earnestMoneyRecordParam.getUserAddr(), orderNum, order.getSellerEarnestMoney()));
            // 双方补足保证金
            if (order.getEarnestMoneyStatus() == BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode()) {
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.allPayEarnestMoney(orderNum));
                createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.allPayEarnestMoney(orderNum));
                createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.allPayEarnestMoney(orderNum));
            } else {
                createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.waitBuyerPayEarnestMoney(orderNum));
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.EARNEST_MONEY.getMes(), MessageContext.waitBuyerPayEarnestMoney(orderNum));
            }
            redisUtils.set("sellerPayEarnestMoney" + order.getOrderNum(), true);
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            log.error("卖家支付保证金事件异常 orderNum:{}", earnestMoneyRecordParam.getOrderNum(), e);
            RedisLockUtils.unLock(lockName);
        }
    }

    @Async
    public void listenerEarnestMoneyComplete(String msg) {
        Order order = JSONObject.parseObject(msg, Order.class);
        boolean is = true;
        while (is) {
            Object buyerPayEarnestMoney = redisUtils.get("buyerPayEarnestMoney" + order.getOrderNum());
            Object sellerPayEarnestMoney = redisUtils.get("sellerPayEarnestMoney" + order.getOrderNum());
            if (buyerPayEarnestMoney != null && sellerPayEarnestMoney != null) {
                log.warn("双方都已支付保证金 -> 合约双方保证金全部完成 orderNum:{}", order.getOrderNum());
                is = false;
                // 合约双方保证金全部完成 清除订单初始化标记
                redisUtils.del("orderInitialize" + order.getOrderNum());
            } else {
                try {
                    log.warn("合约双方保证金全部完成等待2s orderNum:{}", order.getOrderNum());
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("合约双方保证金全部完成事件异常", e);
                }
            }
        }
        try {
            Order oneOrder = iOrderService.getOne(new LambdaUpdateWrapper<Order>().eq(Order::getOrderNum, order.getOrderNum()));
            order.setOrderId(oneOrder.getOrderId());
            order.setEarnestMoneyStatus(BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode());
            iOrderService.updateById(order);
        } catch (Exception e) {
            log.error("合约双方保证金全部完成事件异常 orderNum:{}", order.getOrderNum(), e);
        }
    }

    @Async
    public void listenerBuyerPayDeposit(String msg) {
        DepositRecord.DepositRecordParam depositRecordParam = JSONObject.parseObject(msg, DepositRecord.DepositRecordParam.class);
        boolean is = true;
        while (is) {
            Object buyerPayEarnestMoney = redisUtils.get("buyerPayEarnestMoney" + depositRecordParam.getOrderNum());
            if (buyerPayEarnestMoney != null) {
                log.warn("买家支付保证金 -> 买家支付代币 orderNum:{}", depositRecordParam.getOrderNum());
                is = false;
            } else {
                try {
                    log.warn("买家支付代币等待2s orderNum:{}", depositRecordParam.getOrderNum());
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("买家支付代币事件异常", e);
                }
            }
        }
        String lockName = "payDeposit" + depositRecordParam.getOrderNum();
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.warn("买家支付代币事件未获取锁 orderNum:{}", depositRecordParam.getOrderNum());
                Thread.sleep(5000);
                listenerBuyerPayDeposit(msg);
            }
            Order order = iOrderService.getOne(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getOrderNum, depositRecordParam.getOrderNum())
                    .eq(Order::getChainType, depositRecordParam.getChainType()));
            order.setBuyerMakeUp(depositRecordParam.getDepositedAmount());
            String orderNum = getOrderNum(order.getOrderNum());
            if (depositRecordParam.getDepositedAmount().equals(order.getBuyerDeliveryQuantity())) {
                order.setDepositStatus(BaseConstants.DepositStatus.BUYER_MAKE_UP.getCode());
                createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.makeUpToken(orderNum));
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.buyerMakeUpTokenToCreator(order.getBuyerAddr() ,orderNum));
                // 卖方补足代币
                if (order.getSellerMakeUp().equals(order.getSellerDeliveryQuantity())) {
                    createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.allMakeUpToken(orderNum));
                    createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.allMakeUpToken(orderNum));
                    createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.allMakeUpToken(orderNum));
                }
            } else {
                BigDecimal price = new BigDecimal(order.getBuyerDeliveryQuantity()).subtract(new BigDecimal(order.getBuyerMakeUp()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.waitBuyerMakeUpToken(orderNum, price.toString()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.waitBuyerMakeUpToken(orderNum, price.toString()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.makeUpToken(orderNum, order.getBuyerMakeUp(), price.toString()));
            }
            // 卖方未补足代币
            if (!order.getSellerMakeUp().equals(order.getSellerDeliveryQuantity())) {
                BigDecimal price = new BigDecimal(order.getSellerDeliveryQuantity()).subtract(new BigDecimal(order.getSellerMakeUp()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.waitSellerMakeUpToken(orderNum, price.toString()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.waitSellerMakeUpToken(orderNum, price.toString()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.makeUpToken(order.getOrderNum(), order.getSellerMakeUp(), price.toString()));
            }
            if (order.getDepositStatus() == BaseConstants.DepositStatus.SELLER_MAKE_UP.getCode() && depositRecordParam.getDepositedAmount().equals(order.getBuyerDeliveryQuantity())) {
                order.setDepositStatus(BaseConstants.DepositStatus.ALL_MAKE_UP.getCode());
            }
            iOrderService.updateById(order);

            LockUpRecord.TokenSumAddParam tokenSumAddParam = new LockUpRecord.TokenSumAddParam();
            tokenSumAddParam.setTokenAddr(order.getBuyerSubjectMatterAddr());
            tokenSumAddParam.setCount(MathUtils.weiToEth(new BigDecimal(depositRecordParam.getAmount()), 18).toString());
            tokenSumAddParam.setUuid(order.getOrderNum() + order.getBuyerAddr());
            tokenSumAddParam.setType(BaseConstants.add);
            tokenSumAddParam.setChainType(order.getChainType());
            rabbitmqProvider.tokenSumCountAdd(tokenSumAddParam);

            DepositRecord one = iDepositRecordService.getOne(new LambdaUpdateWrapper<DepositRecord>()
                    .eq(DepositRecord::getTransactionHash, depositRecordParam.getTransactionHash())
                    .eq(DepositRecord::getUserAddr, depositRecordParam.getUserAddr())
                    .eq(DepositRecord::getChainType, depositRecordParam.getChainType()));
            if (one == null) {
                DepositRecord depositRecord = new DepositRecord();
                BeanCopyUtils.copyNotNullProperties(depositRecordParam, depositRecord);
                depositRecord.setType(BaseConstants.UserType.BUYER.getCode());
                depositRecord.setChainType(order.getChainType());
                iDepositRecordService.save(depositRecord);
            }
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            log.error("买家支付代币事件异常 orderNum:{}", depositRecordParam.getOrderNum(), e);
            RedisLockUtils.unLock(lockName);
        }
    }

    @Async
    public void listenerSellerPayDeposit(String msg) {
        DepositRecord.DepositRecordParam depositRecordParam = JSONObject.parseObject(msg, DepositRecord.DepositRecordParam.class);
        boolean is = true;
        while (is) {
            Object sellerPayEarnestMoney = redisUtils.get("sellerPayEarnestMoney" + depositRecordParam.getOrderNum());
            if (sellerPayEarnestMoney != null) {
                log.warn("卖家支付保证金 -> 卖家支付代币 orderNum:{}", depositRecordParam.getOrderNum());
                is = false;
            } else {
                try {
                    log.warn("卖家支付代币事件等待2s orderNum:{}", depositRecordParam.getOrderNum());
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("卖家支付代币事件异常", e);
                }
            }
        }
        String lockName = "payDeposit" + depositRecordParam.getOrderNum();
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.warn("卖家支付代币事件未获取锁 orderNum:{}", depositRecordParam.getOrderNum());
                Thread.sleep(5000);
                listenerSellerPayDeposit(msg);
            }
            Order order = iOrderService.getOne(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getOrderNum, depositRecordParam.getOrderNum()).
                    eq(Order::getChainType, depositRecordParam.getChainType()));
            order.setSellerMakeUp(depositRecordParam.getDepositedAmount());
            String orderNum = getOrderNum(order.getOrderNum());
            if (depositRecordParam.getDepositedAmount().equals(order.getSellerDeliveryQuantity())) {
                order.setDepositStatus(BaseConstants.DepositStatus.SELLER_MAKE_UP.getCode());
                createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.makeUpToken(orderNum));
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.buyerMakeUpTokenToCreator(order.getSellerAddr() ,orderNum));
                // 买方补足代币
                if (order.getBuyerMakeUp().equals(order.getBuyerDeliveryQuantity())) {
                    createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.allMakeUpToken(orderNum));
                    createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.allMakeUpToken(orderNum));
                    createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.allMakeUpToken(orderNum));
                }
            } else {
                BigDecimal price = new BigDecimal(order.getSellerDeliveryQuantity()).subtract(new BigDecimal(order.getSellerMakeUp()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.waitSellerMakeUpToken(orderNum, price.toString()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.waitSellerMakeUpToken(orderNum, price.toString()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.makeUpToken(orderNum, order.getSellerMakeUp(), price.toString()));
            }
            // 买方未补足代币
            if (!order.getBuyerMakeUp().equals(order.getBuyerDeliveryQuantity())) {
                BigDecimal price = new BigDecimal(order.getBuyerDeliveryQuantity()).subtract(new BigDecimal(order.getBuyerMakeUp()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getContractCreatorAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.waitBuyerMakeUpToken(orderNum, price.toString()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getSellerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.waitBuyerMakeUpToken(orderNum, price.toString()));
                createMessage(order.getChainType(), order.getOrderNum(), order.getBuyerAddr(), BaseConstants.MessageType.DELIVERY.getMes(), MessageContext.makeUpToken(orderNum, order.getBuyerMakeUp(), price.toString()));
            }
            if (order.getDepositStatus() == BaseConstants.DepositStatus.BUYER_MAKE_UP.getCode() && depositRecordParam.getDepositedAmount().equals(order.getSellerDeliveryQuantity())) {
                order.setDepositStatus(BaseConstants.DepositStatus.ALL_MAKE_UP.getCode());
            }
            iOrderService.updateById(order);

            LockUpRecord.TokenSumAddParam tokenSumAddParam = new LockUpRecord.TokenSumAddParam();
            tokenSumAddParam.setTokenAddr(order.getSellerSubjectMatterAddr());
            tokenSumAddParam.setCount(MathUtils.weiToEth(new BigDecimal(depositRecordParam.getAmount()), 18).toString());
            tokenSumAddParam.setUuid(order.getOrderNum() + order.getSellerAddr());
            tokenSumAddParam.setType(BaseConstants.add);
            tokenSumAddParam.setChainType(order.getChainType());
            rabbitmqProvider.tokenSumCountAdd(tokenSumAddParam);

            DepositRecord one = iDepositRecordService.getOne(new LambdaUpdateWrapper<DepositRecord>()
                    .eq(DepositRecord::getTransactionHash, depositRecordParam.getTransactionHash())
                    .eq(DepositRecord::getUserAddr, depositRecordParam.getUserAddr())
                    .eq(DepositRecord::getChainType, depositRecordParam.getChainType()));
            if (one == null) {
                DepositRecord depositRecord = new DepositRecord();
                BeanCopyUtils.copyNotNullProperties(depositRecordParam, depositRecord);
                depositRecord.setType(BaseConstants.UserType.SELLER.getCode());
                depositRecord.setChainType(order.getChainType());
                iDepositRecordService.save(depositRecord);
            }
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            log.error("卖家支付代币事件异常 orderNum:{}", depositRecordParam.getOrderNum(), e);
            RedisLockUtils.unLock(lockName);
        }
    }

    @Async
    public void listenerBuyerWithdraw(String msg) {
        Order order = JSONObject.parseObject(msg, Order.class);
        boolean is = true;
        while (is) {
            Object buyerPayEarnestMoney = redisUtils.get("buyerPayEarnestMoney" + order.getOrderNum());
            if (buyerPayEarnestMoney != null) {
                log.warn("买家支付保证金 -> 买家领取代币 orderNum:{}", order.getOrderNum());
                is = false;
            } else {
                try {
                    log.warn("买家领取代币等待俩秒 orderNum:{}", order.getOrderNum());
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("买家领取代币事件异常", e);
                }
            }
        }
        String lockName = "withdraw" + order.getOrderNum();
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.warn("买家领取代币事件未获取锁 orderNum:{}", order.getOrderNum());
                Thread.sleep(5000);
                listenerBuyerWithdraw(msg);
            }
            Order oneOrder = iOrderService.getOne(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getOrderNum, order.getOrderNum())
                    .eq(Order::getChainType, order.getChainType()));
            log.warn("买家领取代币 orderStatus:{}", oneOrder.getOrderStatus());
            order.setOrderId(oneOrder.getOrderId());
            if (oneOrder.getOrderStatus() == BaseConstants.OrderStatus.SELLER_RECEIVE.getCode()) {
                order.setOrderStatus(BaseConstants.OrderStatus.OVER.getCode());
            } else {
                order.setOrderStatus(BaseConstants.OrderStatus.BUYER_RECEIVE.getCode());
            }
            iOrderService.updateById(order);
            redisUtils.del("buyerPayEarnestMoney" + order.getOrderNum());
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            RedisLockUtils.unLock(lockName);
            log.error("买家领取应得代币事件异常 orderNum:{}", order.getOrderNum(), e);
        }
    }

    @Async
    public void listenerSellerWithdraw(String msg) {
        Order order = JSONObject.parseObject(msg, Order.class);
        boolean is = true;
        while (is) {
            Object sellerPayEarnestMoney = redisUtils.get("sellerPayEarnestMoney" + order.getOrderNum());
            if (sellerPayEarnestMoney != null) {
                log.warn("卖家支付保证金 -> 卖家领取代币 orderNum:{}", order.getOrderNum());
                is = false;
            } else {
                try {
                    log.warn("卖家领取代币等待2s orderNum:{}", order.getOrderNum());
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("卖家领取代币事件异常", e);
                }
            }
        }
        String lockName = "withdraw" + order.getOrderNum();
        try {
            // 没有获取锁
            if (!RedisLockUtils.lock(lockName, 5)) {
                log.warn("卖家领取代币事件未获取锁 orderNum:{}", order.getOrderNum());
                Thread.sleep(5000);
                RedisLockUtils.unLock(lockName);
                listenerSellerWithdraw(msg);
            }
            Order oneOrder = iOrderService.getOne(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getOrderNum, order.getOrderNum())
                    .eq(Order::getChainType, order.getChainType()));
            log.warn("卖家领取代币 orderStatus:{}", oneOrder.getOrderStatus());
            order.setOrderId(oneOrder.getOrderId());
            if (oneOrder.getOrderStatus() == BaseConstants.OrderStatus.BUYER_RECEIVE.getCode()) {
                order.setOrderStatus(BaseConstants.OrderStatus.OVER.getCode());
            } else {
                order.setOrderStatus(BaseConstants.OrderStatus.SELLER_RECEIVE.getCode());
            }
            iOrderService.updateById(order);
            redisUtils.del("sellerPayEarnestMoney" + order.getOrderNum());
            RedisLockUtils.unLock(lockName);
        } catch (Exception e) {
            RedisLockUtils.unLock(lockName);
            log.error("卖家领取应得代币事件异常 orderNum:{}", order.getOrderNum(), e);
        }
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

    public synchronized void listenerRecommendOrderAdd(String msg) {
        try {
            RecommendOrder recommendOrder = JSONObject.parseObject(msg, RecommendOrder.class);
            RecommendOrder oneRecommendOrder = iRecommendOrderService.getOne(new LambdaUpdateWrapper<RecommendOrder>()
                    .nested(e -> e.eq(RecommendOrder::getBuyerTokenAddr, recommendOrder.getBuyerTokenAddr())
                            .eq(RecommendOrder::getSellerTokenAddr, recommendOrder.getSellerTokenAddr()))
                    .or()
                    .nested(e -> e.eq(RecommendOrder::getBuyerTokenAddr, recommendOrder.getSellerTokenAddr())
                            .eq(RecommendOrder::getSellerTokenAddr, recommendOrder.getBuyerTokenAddr())));
            // 1 创建订单，增加待生效，总数量 2 增加待交割 减少待生效 3 减少待交割
            if (recommendOrder.getType() == 1) {
                if (oneRecommendOrder != null) {
                    oneRecommendOrder.setTotalNum(oneRecommendOrder.getTotalNum() + 1);
                    oneRecommendOrder.setWaitEffectNum(oneRecommendOrder.getWaitEffectNum() + 1);
                    iRecommendOrderService.updateById(oneRecommendOrder);
                } else {
                    recommendOrder.setWaitDeliveryNum(0);
                    recommendOrder.setWaitEffectNum(1);
                    recommendOrder.setTotalNum(1);
                    iRecommendOrderService.save(recommendOrder);
                }
            } else if (recommendOrder.getType() == 2){
                if (oneRecommendOrder != null) {
                    oneRecommendOrder.setWaitDeliveryNum(oneRecommendOrder.getWaitDeliveryNum() + 1);
                    oneRecommendOrder.setWaitEffectNum(oneRecommendOrder.getWaitEffectNum() - 1);
                    iRecommendOrderService.updateById(oneRecommendOrder);
                }
            } else if (recommendOrder.getType() == 3){
                if (oneRecommendOrder != null) {
                    oneRecommendOrder.setWaitDeliveryNum(oneRecommendOrder.getWaitDeliveryNum() - 1);
                    iRecommendOrderService.updateById(oneRecommendOrder);
                }
            } else if (recommendOrder.getType() == 4){
                if (oneRecommendOrder != null) {
                    oneRecommendOrder.setWaitEffectNum(oneRecommendOrder.getWaitEffectNum() - 1);
                    iRecommendOrderService.updateById(oneRecommendOrder);
                }
            }

        } catch (Exception e) {
            log.error("推荐订单表累加异常", e);
        }

    }

    public synchronized void listenerTokenSumCountAdd(String msg) {
        try {
            LockUpRecord.TokenSumAddParam tokenSumAddParam = JSONObject.parseObject(msg, LockUpRecord.TokenSumAddParam.class);
            LockUpRecord lockUpRecord = iLockUpRecordService.getOne(new LambdaUpdateWrapper<LockUpRecord>()
                    .eq(LockUpRecord::getTokenAddr, tokenSumAddParam.getTokenAddr())
                    .eq(LockUpRecord::getChainType, tokenSumAddParam.getChainType()));
            TokenPrice tokenPrice = iTokenPriceService.getOne(new LambdaUpdateWrapper<TokenPrice>().eq(TokenPrice::getTokenAddr, tokenSumAddParam.getTokenAddr()));
            String price = "0";
            if (tokenPrice != null) {
                price = tokenPrice.getPrice();
            }
            // 1 累加 2 累减
            if (tokenSumAddParam.getType() == BaseConstants.add) {
                if (lockUpRecord != null) {
                    lockUpRecord.setSumCount(new BigDecimal(lockUpRecord.getSumCount()).add(new BigDecimal(tokenSumAddParam.getCount())).toString());
                    lockUpRecord.setCurrentCount(new BigDecimal(lockUpRecord.getCurrentCount()).add(new BigDecimal(tokenSumAddParam.getCount())).toString());
                    lockUpRecord.setPrice(price);
                    iLockUpRecordService.updateById(lockUpRecord);
                } else {
                    lockUpRecord = new LockUpRecord();
                    lockUpRecord.setSumCount(tokenSumAddParam.getCount());
                    lockUpRecord.setTokenAddr(tokenSumAddParam.getTokenAddr());
                    lockUpRecord.setCurrentCount(tokenSumAddParam.getCount());
                    lockUpRecord.setDeliveryCount("0");
                    lockUpRecord.setPrice(price);
                    lockUpRecord.setChainType(tokenSumAddParam.getChainType());
                    iLockUpRecordService.save(lockUpRecord);
                }
            } else if(tokenSumAddParam.getType() == BaseConstants.del) {
                if (lockUpRecord != null) {
                    lockUpRecord.setCurrentCount(new BigDecimal(lockUpRecord.getCurrentCount()).subtract(new BigDecimal(tokenSumAddParam.getCount())).toString());
                    lockUpRecord.setDeliveryCount(new BigDecimal(lockUpRecord.getDeliveryCount()).add(new BigDecimal(tokenSumAddParam.getCount())).toString());
                    lockUpRecord.setPrice(price);
                    iLockUpRecordService.updateById(lockUpRecord);
                }
            } else {
                if (lockUpRecord != null) {
                    lockUpRecord.setCurrentCount(new BigDecimal(lockUpRecord.getCurrentCount()).subtract(new BigDecimal(tokenSumAddParam.getCount())).toString());
                    lockUpRecord.setPrice(price);
                    iLockUpRecordService.updateById(lockUpRecord);
                }
            }
        } catch (Exception e) {
            log.error("标的物总量累加/累减异常", e);
        }
    }
}
