package com.blockinsight.basefi.controller;


import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.Order;
import com.blockinsight.basefi.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Api(tags = "订单")
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService iOrderService;

    /**
     * @param pageNumber 当前页
     * @param pageSize   每页数量
     * @return 推荐订单
     */
    @ApiOperation(value = "推荐订单", notes = "推荐订单", httpMethod = "GET")
    @GetMapping("/recommend")
    public R recommend(
            @ApiParam(name = "pageNumber", value = "当前页", required = true) @RequestParam("pageNumber") Integer pageNumber,
                       @ApiParam(name = "pageSize", value = "每页条目", required = true) @RequestParam("pageSize") Integer pageSize) {
        return iOrderService.recommendedOrder(pageNumber, pageSize);
    }

    /**
     * @param orderInpParam 内部类包含以下内容
     *                      pageNumber   当前页
     *                      pageSize     每页数量01
     *                      buyOrSell    买buyOrSell=1卖buyOrSell=2
     *                      orderNumber  搜索内容
     *                      buyerSubject 卖家标的物
     *                      sellerSubjec 买家标的物
     *                      startingTime 开始时间
     *                      endTime      结束时间
     * @return 买/卖订单列表
     */
    @ApiOperation(value = "买/卖订单列表", notes = "买/卖订单列表", httpMethod = "POST")
    @PostMapping("/buy-or-sell")
    public R buyOrSell(@ApiParam(name = "orderInpParam", value = "买/卖订单列表", required = true) @RequestBody Order.OrderInpParam orderInpParam) {
        return iOrderService.buyOrSell(orderInpParam);
    }

    /**
     * @param orderNumber 订单编号
     * @return 订单详情
     */
    @ApiOperation(value = "订单详情", notes = "订单详情", httpMethod = "GET")
    @GetMapping("/order-details")
    public R orderDetails(@ApiParam(name = "orderNumber", value = "订单编号", required = true) @RequestParam("orderNumber") String orderNumber) {
        return iOrderService.orderDetails(orderNumber);
    }

    /**
     * @param userNum     用户公钥
     * @param searchFor   搜索
     * @param status      状态1
     * @param pageNumber  当前页
     * @param pageSize    每页数量
     * @return 用户订单列表
     */
    @ApiOperation(value = "用户订单列表", notes = "用户订单列表", httpMethod = "GET")
    @GetMapping("/user-order-list")
    public R userOrderList(@ApiParam(name = "chainType", value = "链类型", required = true) @RequestParam("chainType") Integer chainType,
                           @ApiParam(name = "userNum", value = "用户公钥", required = true) @RequestParam("userNum") String userNum,
                           @ApiParam(name = "searchFor", value = "搜索内容") @RequestParam(value = "searchFor", required = false) String searchFor,
                           @ApiParam(name = "status", value = "状态", required = true) @RequestParam("status") Integer status,
                           @ApiParam(name = "createStatus", value = "是否仅展示我创建的 0 否 1 是", required = true) @RequestParam("createStatus") Integer createStatus,
                           @ApiParam(name = "pageNumber", value = "当前页", required = true) @RequestParam("pageNumber") Integer pageNumber,
                           @ApiParam(name = "pageSize", value = "每页数量", required = true) @RequestParam("pageSize") Integer pageSize) {
        return iOrderService.userOrderList(chainType, userNum, searchFor, createStatus, status, pageNumber, pageSize);
    }

    /**
     * @param chainType 链类型
     * @return 首页订单数量
     */
    @ApiOperation(value = "首页订单数量", notes = "首页订单数量", httpMethod = "GET")
    @GetMapping("/order-count")
    public R orderCount(@ApiParam(name = "chainType", value = "链类型", required = true) @RequestParam("chainType") Integer chainType) {
        return iOrderService.orderCount(chainType);
    }


}
