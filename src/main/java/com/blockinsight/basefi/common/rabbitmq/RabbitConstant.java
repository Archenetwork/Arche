package com.blockinsight.basefi.common.rabbitmq;

public class RabbitConstant {

    /**
     * 订单创建
     */
    public static final String ORDER_CREATE_PUT = "order_create_put";
    public static final String ORDER_CREATE_QUEUE_PUT = "order_create_queue_put";
    public static final String ORDER_CREATE_ROUTING_KEY_PUT = "order_create_routing_key_put";

    /**
     * 订单初始化
     */
    public static final String ORDER_INITIALIZE_PUT = "order_initialize_put";
    public static final String ORDER_INITIALIZE_QUEUE_PUT = "order_initialize_queue_put";
    public static final String ORDER_INITIALIZE_ROUTING_KEY_PUT = "order_initialize_routing_key_put";

    /**
     * 买家支付保证金
     */
    public static final String BUYER_PAY_EARNEST_MONEY_PUT = "buyer_pay_earnest_money_put";
    public static final String BUYER_PAY_EARNEST_MONEY_QUEUE_PUT = "buyer_pay_earnest_money_queue_put";
    public static final String BUYER_PAY_EARNEST_MONEY_ROUTING_KEY_PUT = "buyer_pay_earnest_money_routing_key_put";

    /**
     * 卖家支付保证金
     */
    public static final String SELLER_PAY_EARNEST_MONEY_PUT = "seller_pay_earnest_money_put";
    public static final String SELLER_PAY_EARNEST_MONEY_QUEUE_PUT = "seller_pay_earnest_money_queue_put";
    public static final String SELLER_PAY_EARNEST_MONEY_ROUTING_KEY_PUT = "seller_pay_earnest_money_routing_key_put";

    /**
     * 合约双方保证金全部完成
     */
    public static final String EARNEST_MONEY_COMPLETE_PUT = "earnest_money_complete_put";
    public static final String EARNEST_MONEY_COMPLETE_QUEUE_PUT = "earnest_money_complete_queue_put";
    public static final String EARNEST_MONEY_COMPLETE_ROUTING_KEY_PUT = "earnest_money_complete_routing_key_put";

    /**
     * 买家支付代币
     */
    public static final String BUYER_PAY_DEPOSIT_PUT = "buyer_pay_deposit_put";
    public static final String BUYER_PAY_DEPOSIT_QUEUE_PUT = "buyer_pay_deposit_queue_put";
    public static final String BUYER_PAY_DEPOSIT_ROUTING_KEY_PUT = "buyer_pay_deposit_routing_key_put";

    /**
     * 卖家支付代币
     */
    public static final String SELLER_PAY_DEPOSIT_PUT = "seller_pay_deposit_put";
    public static final String SELLER_PAY_DEPOSIT_QUEUE_PUT = "seller_pay_deposit_queue_put";
    public static final String SELLER_PAY_DEPOSIT_ROUTING_KEY_PUT = "seller_pay_deposit_routing_key_put";

    /**
     * 买家领取应得代币
     */
    public static final String BUYER_WITHDRAW_PUT = "buyer_withdraw_put";
    public static final String BUYER_WITHDRAW_QUEUE_PUT = "buyer_withdraw_queue_put";
    public static final String BUYER_WITHDRAW_ROUTING_KEY_PUT = "buyer_withdraw_routing_key_put";

    /**
     * 卖家领取应得代币
     */
    public static final String SELLER_WITHDRAW_PUT = "seller_withdraw_put";
    public static final String SELLER_WITHDRAW_QUEUE_PUT = "seller_withdraw_queue_put";
    public static final String SELLER_WITHDRAW_ROUTING_KEY_PUT = "seller_withdraw_routing_key_put";

    /**
     * 推荐订单表累加
     */
    public static final String RECOMMEND_ORDER_ADD_PUT = "recommend_order_add_put";
    public static final String RECOMMEND_ORDER_ADD_QUEUE_PUT = "recommend_order_add_queue_put";
    public static final String RECOMMEND_ORDER_ADD_ROUTING_KEY_PUT = "recommend_order_add_routing_key_put";

    /**
     * 标的物总量累加
     */
    public static final String TOKEN_SUM_COUNT_ADD_PUT = "token_sum_count_add_put";
    public static final String TOKEN_SUM_COUNT_ADD_QUEUE_PUT = "token_sum_count_add_queue_put";
    public static final String TOKEN_SUM_COUNT_ADD_ROUTING_KEY_PUT = "token_sum_count_add_routing_key_put";
}
