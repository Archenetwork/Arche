package com.blockinsight.basefi.common.constant;

public class RedisConstant {
    public static final String COLON = ":";
    public static final String PREFIX = "metapool" + COLON;

    public static final String ETH_FIRST = PREFIX + "eth" + COLON + "EthBlockFirst";
    public static final String ETH = PREFIX + "eth" + COLON + "EthBlock";

    public static final String FINAL_COUNTDOWN = PREFIX + "other" + "final_countdown";


    public static void main(String[] args) {
        System.out.println(FINAL_COUNTDOWN);
    }
}
