package com.blockinsight.basefi.common.rabbitmq.provider;

import com.alibaba.fastjson.JSON;
import com.blockinsight.basefi.common.rabbitmq.RabbitConstant;
import com.blockinsight.basefi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RabbitmqProvider implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 订单创建
     * @param order
     */
    public void orderCreate(Order order){
        String msg = JSON.toJSONString(order);
        log.warn("发送订单创建事件消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData(order.getContractCreatorAddr() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.ORDER_CREATE_PUT, RabbitConstant.ORDER_CREATE_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 订单初始化
     * @param order
     */
    public void orderInitialize(Order order){
        String msg = JSON.toJSONString(order);
        log.warn("发送订单初始化消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData(order.getContractInitializeBlockNumber() + order.getContractCreatorAddr() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.ORDER_INITIALIZE_PUT, RabbitConstant.ORDER_INITIALIZE_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 买家支付保证金
     * @param earnestMoneyRecordParam
     */
    public void buyerPayEarnestMoney(EarnestMoneyRecord.EarnestMoneyRecordParam earnestMoneyRecordParam){
        String msg = JSON.toJSONString(earnestMoneyRecordParam);
        log.warn("发送买家支付保证金事件消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData(earnestMoneyRecordParam.getUserAddr() + earnestMoneyRecordParam.getOrderNum() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.BUYER_PAY_EARNEST_MONEY_PUT, RabbitConstant.BUYER_PAY_EARNEST_MONEY_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 卖家支付保证金
     * @param earnestMoneyRecordParam
     */
    public void sellerPayEarnestMoney(EarnestMoneyRecord.EarnestMoneyRecordParam earnestMoneyRecordParam){
        String msg = JSON.toJSONString(earnestMoneyRecordParam);
        log.warn("发送卖家支付保证金事件消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData(earnestMoneyRecordParam.getUserAddr() + earnestMoneyRecordParam.getOrderNum() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.SELLER_PAY_EARNEST_MONEY_PUT, RabbitConstant.SELLER_PAY_EARNEST_MONEY_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 合约双方保证金全部完成
     * @param order
     */
    public void earnestMoneyComplete(Order order) {
        String msg = JSON.toJSONString(order);
        log.warn("发送合约双方保证金全部完成件消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData("earnestMoneyComplete" + order.getOrderNum() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.EARNEST_MONEY_COMPLETE_PUT, RabbitConstant.EARNEST_MONEY_COMPLETE_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 买家支付代币
     * @param depositRecordParam
     */
    public void buyerPayDeposit(DepositRecord.DepositRecordParam depositRecordParam){
        String msg = JSON.toJSONString(depositRecordParam);
        log.warn("发送买家支付代币事件消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData(depositRecordParam.getUserAddr() + depositRecordParam.getDepositedAmount() + depositRecordParam.getOrderNum() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.BUYER_PAY_DEPOSIT_PUT, RabbitConstant.BUYER_PAY_DEPOSIT_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 卖家支付代币
     * @param depositRecordParam
     */
    public void sellerPayDeposit(DepositRecord.DepositRecordParam depositRecordParam){
        String msg = JSON.toJSONString(depositRecordParam);
        log.warn("发送卖家支付代币事件消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData(depositRecordParam.getUserAddr() + depositRecordParam.getDepositedAmount() + depositRecordParam.getOrderNum() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.SELLER_PAY_DEPOSIT_PUT, RabbitConstant.SELLER_PAY_DEPOSIT_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 买家领取应得代币事件
     * @param order
     */
    public void buyerWithdraw(Order order) {
        String msg = JSON.toJSONString(order);
        log.warn("发送买家领取应得代币事件消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData("buyerWithdraw" + order.getOrderNum() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.BUYER_WITHDRAW_PUT, RabbitConstant.BUYER_WITHDRAW_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 卖家领取应得代币事件
     * @param order
     */
    public void sellerWithdraw(Order order) {
        String msg = JSON.toJSONString(order);
        log.warn("发送卖家领取应得代币事件消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData("sellerWithdraw" + order.getOrderNum()  + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.SELLER_WITHDRAW_PUT, RabbitConstant.SELLER_WITHDRAW_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 推荐订单表累加
     * @param recommendOrder
     */
    public void recommendOrderAdd(RecommendOrder recommendOrder) {
        String msg = JSON.toJSONString(recommendOrder);
        log.warn("发送推荐订单表累加消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData("recommendOrderAdd" + recommendOrder.getOrderNum() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.RECOMMEND_ORDER_ADD_PUT, RabbitConstant.RECOMMEND_ORDER_ADD_ROUTING_KEY_PUT, msg, correlationId);
    }

    /**
     * 标的物总量累加
     * @param tokenSumAddParam
     */
    public void tokenSumCountAdd(LockUpRecord.TokenSumAddParam tokenSumAddParam) {
        String msg = JSON.toJSONString(tokenSumAddParam);
        log.warn("发送标的物总量累加消息 msg:{}", msg);
        CorrelationData correlationId = new CorrelationData("tokenSumCountAdd" + tokenSumAddParam.getUuid() + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitConstant.TOKEN_SUM_COUNT_ADD_PUT, RabbitConstant.TOKEN_SUM_COUNT_ADD_ROUTING_KEY_PUT, msg, correlationId);
    }



    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("Successful consumption");
        } else {
            log.info("Consumption failure : " + cause);
        }
    }



}
