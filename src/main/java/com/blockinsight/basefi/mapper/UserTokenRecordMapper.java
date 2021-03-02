package com.blockinsight.basefi.mapper;

import com.blockinsight.basefi.entity.UserTokenRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 用户代币记录表 Mapper 接口
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
public interface UserTokenRecordMapper extends BaseMapper<UserTokenRecord> {

    /**
     * @param hashMap
     * @return 个人代币记录的查询
     */
    List<UserTokenRecord> userTokenList(HashMap<String, Object> hashMap);

    /**
     * @param hashMap
     * @return 个人代币记录总数的查询
     */
    long userTokenListCount(HashMap<String, Object> hashMap);

    /**
     * @param checkName
     * @return 搜索订单
     */

    UserTokenRecord searchBar(String checkName);



}
