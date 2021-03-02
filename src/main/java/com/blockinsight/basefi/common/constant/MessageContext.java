package com.blockinsight.basefi.common.constant;

import com.blockinsight.basefi.common.util.MathUtils;

import java.math.BigDecimal;

public class MessageContext {

    public static String getPrice(String price) {
        return MathUtils.weiToEth(new BigDecimal(price)).toString();
    }

    public static String createOrderMessage(String orderNum) {
        return "订单创建成功，订单编号为" + orderNum;
    }

    public static String buyerEarnestMoney(String orderNum, String price) {
        return "你已支付订单（" + orderNum+ "）" + getPrice(price) + "保证金";
    }

    public static String creatorBuyerEarnestMoney(String userAddr, String orderNum, String price) {
       return "买方（" + getUserAddr(userAddr) + "）已支付订单（" + orderNum + "）" + getPrice(price) + "保证金";
    }

    public static String creatorSellerEarnestMoney(String userAddr, String orderNum, String price) {
        return "卖方（" + getUserAddr(userAddr) + "）已支付订单（" + orderNum + "）" + getPrice(price) + "保证金";
    }

    public static String allPayEarnestMoney(String orderNum) {
        return "买/卖双方已支付完成订单（" + orderNum + "）保证金，等待补足剩余数量的代币";
    }

    public static String waitSellerPayEarnestMoney(String orderNum) {
        return "等待卖家支付订单（" + orderNum + "）保证金";
    }

    public static String waitBuyerPayEarnestMoney(String orderNum) {
        return "等待买家支付订单（" + orderNum + "）保证金";
    }

    public static String buyerEarnestMoneyTime(String orderNum, String time) {
        return "买家加入订单后，提示买家：“你已加入订单（" + orderNum + "），请在  时间（" + time + "）内支付保证金";
    }

    public static String sellerEarnestMoneyTime(String orderNum, String time) {
        return "卖家加入订单后，提示卖家：“你已加入订单（" + orderNum + "），请在  时间（" + time + "）内支付保证金";
    }

    public static String makeUpToken(String orderNum) {
        return "你的订单（" + orderNum + "）剩余代币数量已补足，等待交割";
    }

    public static String allMakeUpToken(String orderNum) {
        return "买/卖双方已补足订单（" + orderNum + "）的剩余代币，等待交割";
    }

    public static String buyerMakeUpTokenToCreator(String userAddr, String orderNum) {
        return "买家（" + getUserAddr(userAddr) + "）已补足订单（" + orderNum + "）的剩余代币,等待交割";
    }

    public static String waitSellerMakeUpToken(String orderNum, String price) {
        return "等待卖家补足订单（" + orderNum + "）的剩余" + getPrice(price) + "（数量）代币";
    }

    public static String waitBuyerMakeUpToken(String orderNum, String price) {
        return "等待买家补足订单（" + orderNum + "）的剩余" + getPrice(price) + "（数量）代币";
    }

    public static String makeUpToken(String orderNum, String currentPrice, String remainPrice) {
        return "你已补足订单（" + orderNum + "）" + getPrice(currentPrice) + "（数量）代币，还剩" + getPrice(remainPrice) + "（数量）代币待补足";
    }

    public static String noPayEarnestMoneyOrderInvalidation(String orderNum, String time) {
        return "你加入的订单（" + orderNum + "）未在  时间（" + time + "）内支付保证金，订单已失效";
    }

    public static String noMakeUpTokenInvalidation(String orderNum, String time) {
        return "你加入的订单（" + orderNum + "）未在  时间（" + time + "）内补足代币，已违约";
    }


    public static String getUserAddr(String userAddr) {
        return userAddr.substring(0, 4) + "****" + userAddr.substring(userAddr.length() - 4);
    }
}
