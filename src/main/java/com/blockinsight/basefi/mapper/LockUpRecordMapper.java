package com.blockinsight.basefi.mapper;

import com.blockinsight.basefi.entity.LockUpRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Janin
 * @since 2021-02-02
 */
public interface LockUpRecordMapper extends BaseMapper<LockUpRecord> {

    /**
     * 获取累计锁仓金额和当前锁仓金额
     * @return
     */
    Map<String, Object> getSumPriceAndCurrentPrice(@Param("chainType") Integer chainType);

    /**
     * 获取前三当前锁仓金额代币
     * @return
     */
    List<Map<String, Object>> getTopThreeCurrentPriceToken(@Param("chainType") Integer chainType);

}
