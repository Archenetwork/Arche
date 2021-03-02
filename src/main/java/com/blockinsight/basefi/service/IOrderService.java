package com.blockinsight.basefi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.Order;

import java.util.List;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
public interface IOrderService extends IService<Order> {

    /**
     * @param pageNumbers 当前页
     * @param pageSizes   每页数量
     * @return 推荐订单
     */
    R recommendedOrder(Integer pageNumbers, Integer pageSizes);

    /**
     * @param orderInpParam 内部类包含以下内容
     *                      pageNumber   当前页
     *                      pageSize     每页数量
     *                      buyOrSell    买buyOrSell=1卖buyOrSell=2
     *                      orderNumber  搜索内容
     *                      buyerSubject 卖家标的物
     *                      sellerSubjec 买家标的物
     *                      startingTime 开始时间
     *                      endTime      结束时间
     * @return 买/卖订单列表
     */
    R buyOrSell(Order.OrderInpParam orderInpParam);

    /**
     * @param orderNumber 订单编号
     * @return 订单详情
     */
    R orderDetails(String orderNumber);

    /**
     * @param userNum     用户公钥
     * @param searchFor   搜索
     * @param status      状态1
     * @param pageNumber  当前页
     * @param pageSize    每页数量
     * @return 用户订单列表
     */
    R userOrderList(Integer chainType, String userNum, String searchFor, Integer createStatus, Integer status, Integer pageNumber, Integer pageSize);

    int updateOrderById(List<Order> list);

}
