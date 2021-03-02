package com.blockinsight.basefi.common.util;

import com.blockinsight.basefi.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisLockUtils {

    private static Redisson redisson;
    static
    {
        redisson = SpringContextUtils.getBean(Redisson.class);
    }

    // 加锁
    public static boolean lock(String lockName, long minutes){
        // 声明key对象
        String key = Constants.redisLock + lockName;
        // 获取锁对象
        RLock mylock = redisson.getLock(key);
        boolean locked = mylock.isLocked();
        if (!locked) {
            // 加锁，并且设置锁过期时间，防止死锁的产生
            mylock.lock(minutes, TimeUnit.MINUTES);
            log.warn("======lock======{} =====lockName===== {}", Thread.currentThread().getName(), lockName);
            // 加锁成功
            return true;
        }
        return false;
    }
    // 锁的释放
    public static void unLock(String lockName){
        // 必须是和加锁时的同一个key
        String key = Constants.redisLock + lockName;
        // 获取所对象
        RLock mylock = redisson.getLock(key);
        boolean locked = mylock.isLocked();
        if (locked) {
            // 释放锁（解锁）
            mylock.unlock();
        }
        log.warn("======unlock======{} =====lockName===== {}", Thread.currentThread().getName(), lockName);
    }
}
