package com.blockinsight.basefi.common.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
public class NonceUtil {

    public static final String ALGORITHM_NAME = "md5";
    public static final String SALT = "WwFEhq4hgK4yBtWmtKYMvyea95";

    /**
     * token解密
     *
     * @param pk
     * @param timestamp
     * @param token
     * @return
     */
    public static boolean check(String pk, String timestamp, String token) {
        boolean result = false;

        if (!timestampDifference(timestamp)) {
            return false;
        }
        if (encrypt(pk, timestamp).equals(token)) {
            result = true;
        }
        return result;
    }

    public static boolean timestampDifference(String timestamp) {
        boolean yes = false;
        if (((System.currentTimeMillis() - Long.parseLong(timestamp)) / 1000) <= 600) {
            yes = true;
        }

        return yes;
    }

    public static String encrypt(String pk, String timestamp) {
        String beforeEncrypt = pk + SALT + timestamp;
        byte[] digest = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance(ALGORITHM_NAME);
            digest = md5.digest(beforeEncrypt.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //16是表示转换为16进制数
        assert digest != null;
        return new BigInteger(1, digest).toString(16);
    }

    public static void main(String[] args) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        String after_encrypt = encrypt("0x6aab0ae6742565a5a34a66ed718e5c8d68c458fe", timestamp);
        log.info(timestamp);
        log.info(after_encrypt);
        log.info("check : " + check("0x6aab0ae6742565a5a34a66ed718e5c8d68c458fe", timestamp, after_encrypt));
    }

}
