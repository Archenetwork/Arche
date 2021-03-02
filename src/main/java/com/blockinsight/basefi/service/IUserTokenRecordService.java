package com.blockinsight.basefi.service;

import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.UserTokenRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户代币记录表 服务类
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
public interface IUserTokenRecordService extends IService<UserTokenRecord> {
    /**
     * @param userNum   用户公钥
     * @param status     状态(1待支付保证金 2已支付保证金 3等待交割 4已交割)
     * @param pageNumber 当前页
     * @param pageSize   每页数量
     * @return 个人代币记录列表
     */
    R userTokenList(String userNum, Integer status, Integer pageNumber, Integer pageSize);

    /**
     * @param checkName 查询条件(代币名称、代币地址、订单编号)
     * @return 查询订单
     */
    R checkingOrder(String checkName);

    /**
     * @param userNum 用户公钥
     * @param tokenAddr 代币地址
     * @return 添加个人代币记录
     */
    R addToUserTokenList(String userNum, String tokenAddr);

    /**
     * @param userNum 用户公钥
     * @param orderNum 订单便编号
     * @return 删除个人代币记录
     */
    R delToUserTokenList(String userNum, String orderNum);

}
