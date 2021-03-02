package com.blockinsight.basefi.common.rabbitmq.customer;

import com.blockinsight.basefi.common.rabbitmq.RabbitConstant;
import com.blockinsight.basefi.common.rabbitmq.business.OrderBusiness;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderReceiver {

    @Autowired
    private OrderBusiness orderBusiness;

    @RabbitListener(queues = RabbitConstant.ORDER_CREATE_QUEUE_PUT)
    public void listenerOrderCreate(String msg){
        log.warn("监听到订单创建事件 msg:{}", msg);
        orderBusiness.listenerOrderCreate(msg);
    }

    @RabbitListener(queues = RabbitConstant.ORDER_INITIALIZE_QUEUE_PUT)
    public void listenerOrderInitialize(String msg){
        log.warn("监听到订单初始化事件 msg:{}", msg);
        orderBusiness.listenerOrderInitialize(msg);
    }

    @RabbitListener(queues = RabbitConstant.BUYER_PAY_EARNEST_MONEY_QUEUE_PUT)
    public void listenerBuyerPayEarnestMoney(String msg){
        log.warn("监听到买家支付保证金事件 msg:{}", msg);
        orderBusiness.listenerBuyerPayEarnestMoney(msg);
    }

    @RabbitListener(queues = RabbitConstant.SELLER_PAY_EARNEST_MONEY_QUEUE_PUT)
    public void listenerSellerPayEarnestMoney(String msg){
        log.warn("监听到卖家支付保证金事件 msg:{}", msg);
        orderBusiness.listenerSellerPayEarnestMoney(msg);
    }

    @RabbitListener(queues = RabbitConstant.EARNEST_MONEY_COMPLETE_QUEUE_PUT)
    public void listenerEarnestMoneyComplete(String msg){
        log.warn("监听到合约双方保证金全部完成事件 msg:{}", msg);
        orderBusiness.listenerEarnestMoneyComplete(msg);
    }

    @RabbitListener(queues = RabbitConstant.BUYER_PAY_DEPOSIT_QUEUE_PUT)
    public void listenerBuyerPayDeposit(String msg){
        log.warn("监听到买家支付代币事件 msg:{}", msg);
        orderBusiness.listenerBuyerPayDeposit(msg);
    }

    @RabbitListener(queues = RabbitConstant.SELLER_PAY_DEPOSIT_QUEUE_PUT)
    public void listenerSellerPayDeposit(String msg){
        log.warn("监听到卖家支付代币事件 msg:{}", msg);
        orderBusiness.listenerSellerPayDeposit(msg);
    }

    @RabbitListener(queues = RabbitConstant.BUYER_WITHDRAW_QUEUE_PUT)
    public void listenerBuyerWithdraw(String msg){
        log.warn("监听到买家领取应得代币事件 msg:{}", msg);
        orderBusiness.listenerBuyerWithdraw(msg);
    }

    @RabbitListener(queues = RabbitConstant.SELLER_WITHDRAW_QUEUE_PUT)
    public void listenerSellerWithdraw(String msg){
        log.warn("监听到卖家领取应得代币事件 msg:{}", msg);
        orderBusiness.listenerSellerWithdraw(msg);
    }

    @RabbitListener(queues = RabbitConstant.RECOMMEND_ORDER_ADD_QUEUE_PUT)
    public void listenerRecommendOrderAdd(String msg){
        log.warn("监听到推荐订单表累加 msg:{}", msg);
        orderBusiness.listenerRecommendOrderAdd(msg);
    }

    @RabbitListener(queues = RabbitConstant.TOKEN_SUM_COUNT_ADD_QUEUE_PUT)
    public void listenerTokenSumCountAdd(String msg){
        log.warn("监听到标的物总量累加 msg:{}", msg);
        orderBusiness.listenerTokenSumCountAdd(msg);
    }
}

