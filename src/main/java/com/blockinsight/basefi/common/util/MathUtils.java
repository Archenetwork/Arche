package com.blockinsight.basefi.common.util;

import java.math.BigDecimal;

public class MathUtils {
    public static final String ALGORITHM_NAME = "md5";
    public static final String SALT = "WwFEhq4hgK4yBtWmtKYMvyea95";

    /**
     * wei转换以太坊
     *
     * @param bigDecimal
     * @return
     */
    public static BigDecimal weiToEth(BigDecimal bigDecimal) {
        BigDecimal divide = new BigDecimal("1000000000000000000");
        return bigDecimal.divide(divide).setScale(2, BigDecimal.ROUND_DOWN);
    }

    /**
     * wei转换以太坊
     *
     * @param bigDecimal
     * @return
     */
    public static BigDecimal weiToEth(BigDecimal bigDecimal, int scale) {
        BigDecimal divide = new BigDecimal("1000000000000000000");
        return bigDecimal.divide(divide).setScale(scale, BigDecimal.ROUND_DOWN);
    }

}
