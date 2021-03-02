package com.blockinsight.basefi.common.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    // 订单创建
    @Bean
    public DirectExchange defaultExchangeOrderCreatePut(){
        return new DirectExchange(RabbitConstant.ORDER_CREATE_PUT);
    }
    @Bean
    public Queue queuePutOrderCreatePut(){
        return new Queue(RabbitConstant.ORDER_CREATE_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutOrderCreatePut(){
        return BindingBuilder.bind(queuePutOrderCreatePut()).to(defaultExchangeOrderCreatePut()).with(RabbitConstant.ORDER_CREATE_ROUTING_KEY_PUT);
    }

    // 订单初始化
    @Bean
    public DirectExchange defaultExchangeOrderInitializePut(){
        return new DirectExchange(RabbitConstant.ORDER_INITIALIZE_PUT);
    }
    @Bean
    public Queue queuePutOrderInitializePut(){
        return new Queue(RabbitConstant.ORDER_INITIALIZE_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutOrderInitializePut(){
        return BindingBuilder.bind(queuePutOrderInitializePut()).to(defaultExchangeOrderInitializePut()).with(RabbitConstant.ORDER_INITIALIZE_ROUTING_KEY_PUT);
    }

    // 买家支付保证金
    @Bean
    public DirectExchange defaultExchangeBuyerPayEarnestMoneyPut(){
        return new DirectExchange(RabbitConstant.BUYER_PAY_EARNEST_MONEY_PUT);
    }
    @Bean
    public Queue queuePutBuyerPayEarnestMoneyPut(){
        return new Queue(RabbitConstant.BUYER_PAY_EARNEST_MONEY_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutBuyerPayEarnestMoneyPut(){
        return BindingBuilder.bind(queuePutBuyerPayEarnestMoneyPut()).to(defaultExchangeBuyerPayEarnestMoneyPut()).with(RabbitConstant.BUYER_PAY_EARNEST_MONEY_ROUTING_KEY_PUT);
    }

    // 卖家支付保证金
    @Bean
    public DirectExchange defaultExchangeSellerPayEarnestMoneyPut(){
        return new DirectExchange(RabbitConstant.SELLER_PAY_EARNEST_MONEY_PUT);
    }
    @Bean
    public Queue queuePutSellerPayEarnestMoneyPut(){
        return new Queue(RabbitConstant.SELLER_PAY_EARNEST_MONEY_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutSellerPayEarnestMoneyPut(){
        return BindingBuilder.bind(queuePutSellerPayEarnestMoneyPut()).to(defaultExchangeSellerPayEarnestMoneyPut()).with(RabbitConstant.SELLER_PAY_EARNEST_MONEY_ROUTING_KEY_PUT);
    }

    // 合约双方保证金全部完成
    @Bean
    public DirectExchange defaultExchangeEarnestMoneyCompletePut(){
        return new DirectExchange(RabbitConstant.EARNEST_MONEY_COMPLETE_PUT);
    }
    @Bean
    public Queue queuePutEarnestMoneyCompletePut(){
        return new Queue(RabbitConstant.EARNEST_MONEY_COMPLETE_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutEarnestMoneyCompletePut(){
        return BindingBuilder.bind(queuePutEarnestMoneyCompletePut()).to(defaultExchangeEarnestMoneyCompletePut()).with(RabbitConstant.EARNEST_MONEY_COMPLETE_ROUTING_KEY_PUT);
    }

    // 买家支付代币
    @Bean
    public DirectExchange defaultExchangeBuyerPayDepositPut(){
        return new DirectExchange(RabbitConstant.BUYER_PAY_DEPOSIT_PUT);
    }
    @Bean
    public Queue queuePutBuyerPayDepositPut(){
        return new Queue(RabbitConstant.BUYER_PAY_DEPOSIT_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutBuyerPayDepositPut(){
        return BindingBuilder.bind(queuePutBuyerPayDepositPut()).to(defaultExchangeBuyerPayDepositPut()).with(RabbitConstant.BUYER_PAY_DEPOSIT_ROUTING_KEY_PUT);
    }

    // 卖家支付代币
    @Bean
    public DirectExchange defaultExchangeSellerPayDepositPut(){
        return new DirectExchange(RabbitConstant.SELLER_PAY_DEPOSIT_PUT);
    }
    @Bean
    public Queue queuePutSellerPayDepositPut(){
        return new Queue(RabbitConstant.SELLER_PAY_DEPOSIT_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutSellerPayDepositPut(){
        return BindingBuilder.bind(queuePutSellerPayDepositPut()).to(defaultExchangeSellerPayDepositPut()).with(RabbitConstant.SELLER_PAY_DEPOSIT_ROUTING_KEY_PUT);
    }

    // 买家领取应得代币
    @Bean
    public DirectExchange defaultExchangeBuyerWithdrawPut(){
        return new DirectExchange(RabbitConstant.BUYER_WITHDRAW_PUT);
    }
    @Bean
    public Queue queuePutBuyerWithdrawPut(){
        return new Queue(RabbitConstant.BUYER_WITHDRAW_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutBuyerWithdrawPut(){
        return BindingBuilder.bind(queuePutBuyerWithdrawPut()).to(defaultExchangeBuyerWithdrawPut()).with(RabbitConstant.BUYER_WITHDRAW_ROUTING_KEY_PUT);
    }

    // 卖家领取应得代币
    @Bean
    public DirectExchange defaultExchangeSellerWithdrawPut(){
        return new DirectExchange(RabbitConstant.SELLER_WITHDRAW_PUT);
    }
    @Bean
    public Queue queuePutSellerWithdrawPut(){
        return new Queue(RabbitConstant.SELLER_WITHDRAW_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutSellerWithdrawPut(){
        return BindingBuilder.bind(queuePutSellerWithdrawPut()).to(defaultExchangeSellerWithdrawPut()).with(RabbitConstant.SELLER_WITHDRAW_ROUTING_KEY_PUT);
    }

    // 推荐订单表累加
    @Bean
    public DirectExchange defaultExchangeRecommendOrderAddPut(){
        return new DirectExchange(RabbitConstant.RECOMMEND_ORDER_ADD_PUT);
    }
    @Bean
    public Queue queuePutRecommendOrderAddPut(){
        return new Queue(RabbitConstant.RECOMMEND_ORDER_ADD_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutRecommendOrderAddPut(){
        return BindingBuilder.bind(queuePutRecommendOrderAddPut()).to(defaultExchangeRecommendOrderAddPut()).with(RabbitConstant.RECOMMEND_ORDER_ADD_ROUTING_KEY_PUT);
    }

    // 标的物总量累加
    @Bean
    public DirectExchange defaultExchangeTokenSumCountAddPut(){
        return new DirectExchange(RabbitConstant.TOKEN_SUM_COUNT_ADD_PUT);
    }
    @Bean
    public Queue queuePutTokenSumCountAddPut(){
        return new Queue(RabbitConstant.TOKEN_SUM_COUNT_ADD_QUEUE_PUT,true);
    }
    @Bean
    public Binding bindingPutTokenSumCountAddPut(){
        return BindingBuilder.bind(queuePutTokenSumCountAddPut()).to(defaultExchangeTokenSumCountAddPut()).with(RabbitConstant.TOKEN_SUM_COUNT_ADD_ROUTING_KEY_PUT);
    }

}