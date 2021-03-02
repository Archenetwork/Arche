package com.blockinsight.basefi.common.constant;

public interface BaseConstants {

    long blockNumberTime = 30;
    long hbBlockNumberTime = 30;
    long baBlockNumberTime = 30;
    long ethBlockNumberTime = 133;

    int add = 1;
    int del = 2;
    int del2 = 3;

    /**
     * 链类型
     */
    enum ChainType
    {
        ALL(0, "全部"),
        HB(1, "火币"),
        BA(2, "币安"),
        ETH(3, "以太坊");

        private int code;
        private String mes;

        ChainType(int code, String mes)
        {
            this.code = code;
            this.mes = mes;
        }

        public int getCode()
        {
            return code;
        }

        public String getMes()
        {
            return mes;
        }
    }

    /**
     * 订单状态
     */
    enum OrderStatus
    {
        WAIT_INITIALIZE(0, "等待初始化"),
        INITIALIZE(1, "初始化"),
        WAIT_BUYER(2, "等待买家"),
        WAIT_SELLER(3, "等待卖家"),
        BUYER_SELLER(4, "买卖家都确定"),
        BUYER_RECEIVE(5, "买方领取"),
        SELLER_RECEIVE(6, "卖方领取"),
        OVER(7, "交割完成");

        private int code;
        private String mes;

        OrderStatus(int code, String mes)
        {
            this.code = code;
            this.mes = mes;
        }

        public int getCode()
        {
            return code;
        }

        public String getMes()
        {
            return mes;
        }
    }

    /**
     * 用户类型
     */
    enum UserType
    {
        BUYER(1, "买方"),
        SELLER(2, "卖方");

        private int code;
        private String mes;

        UserType(int code, String mes)
        {
            this.code = code;
            this.mes = mes;
        }

        public int getCode()
        {
            return code;
        }

        public String getMes()
        {
            return mes;
        }
    }

    /**
     * 保证金状态
     */
    enum EarnestMoneyStatus
    {
        NOT_PAY(0, "双方未支付"),
        BUYER_PAY(1, "买方支付"),
        SELLER_PAY(2, "卖方支付"),
        ALL_PAY(3, "双方支付");

        private int code;
        private String mes;

        EarnestMoneyStatus(int code, String mes)
        {
            this.code = code;
            this.mes = mes;
        }

        public int getCode()
        {
            return code;
        }

        public String getMes()
        {
            return mes;
        }
    }

    /**
     * 补足代币状态
     */
    enum DepositStatus
    {
        NOT_MAKE_UP(0, "双方未补"),
        BUYER_MAKE_UP(1, "买方补足"),
        SELLER_MAKE_UP(2, "卖方补足"),
        ALL_MAKE_UP(3, "双方补足");

        private int code;
        private String mes;

        DepositStatus(int code, String mes)
        {
            this.code = code;
            this.mes = mes;
        }

        public int getCode()
        {
            return code;
        }

        public String getMes()
        {
            return mes;
        }
    }

    /**
     * 消息类型
     */
    enum MessageType
    {
        ALL(1, "全部消息"),
        CREATE_ORDER(2, "订单消息"),
        EARNEST_MONEY(3, "保证金消息"),
        DELIVERY(4, "交割消息"),
        TRANSACTION(5, "违约消息"),
        RECEIVER(6, "收款消息");

        private int code;
        private String mes;

        MessageType(int code, String mes)
        {
            this.code = code;
            this.mes = mes;
        }

        public int getCode()
        {
            return code;
        }

        public String getMes()
        {
            return mes;
        }
    }
}
