package com.blockinsight.basefi.common.util;

import java.math.BigDecimal;

public class UserLevelUtils {

    private static BigDecimal level1SumPrice = new BigDecimal("0.5");
    private static BigDecimal level2SumPrice = new BigDecimal("1.5");
    private static BigDecimal level3SumPrice = new BigDecimal("3.5");
    private static BigDecimal level4SumPrice = new BigDecimal("7.5");
    private static BigDecimal level5SumPrice = new BigDecimal("15.5");
    private static BigDecimal level6SumPrice = new BigDecimal("31.5");

    private static BigDecimal level1Price = new BigDecimal("0.5");
    private static BigDecimal level2Price = new BigDecimal("1");
    private static BigDecimal level3Price = new BigDecimal("2");
    private static BigDecimal level4Price = new BigDecimal("4");
    private static BigDecimal level5Price = new BigDecimal("8");
    private static BigDecimal level6Price = new BigDecimal("16");

    /**
     * 获取购买广告牌的等级
     * @param price
     * @return
     */
    public static int getUpgradeLevel(BigDecimal price) {
        if (level1Price.compareTo(price) == 0) {
            return 1;
        } else if (level2Price.compareTo(price) == 0) {
            return 2;
        } else if (level3Price.compareTo(price) == 0) {
            return 3;
        } else if (level4Price.compareTo(price) == 0) {
            return 4;
        } else if (level5Price.compareTo(price) == 0) {
            return 5;
        } else if (level6Price.compareTo(price) == 0) {
            return 6;
        } else {
            return 0;
        }
    }

    public static BigDecimal getLevelPrice(int level) {
        BigDecimal levelPrice = new BigDecimal("0");
        switch (level) {
            case 1:
                levelPrice = level1SumPrice;
            break;
            case 2:
                levelPrice = level2SumPrice;
            break;
            case 3:
                levelPrice = level3SumPrice;
            break;
            case 4:
                levelPrice = level4SumPrice;
            break;
            case 5:
                levelPrice = level5SumPrice;
            break;
            case 6:
                levelPrice = level6SumPrice;
            break;
            default:

        }
        return levelPrice;
    }

}
