package com.blockinsight.basefi.controller;


import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.service.IRecommendOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 推荐订单表 前端控制器
 * </p>
 *
 * @author Janin
 * @since 2021-01-23
 */
@Api(tags = "推荐交易对")
@Slf4j
@RestController
@RequestMapping("/recommend-order")
public class RecommendOrderController {

    @Autowired
    private IRecommendOrderService iRecommendOrderService;

    /**
     * @param pageNumber 当前页
     * @param pageSize   每页数量
     * @return 推荐订单
     */
    @ApiOperation(value = "推荐交易对", notes = "推荐交易对", httpMethod = "GET")
    @GetMapping("")
    public R recommendedOrder(@ApiParam(name = "pageNumber", value = "当前页", required = true) @RequestParam("pageNumber") Integer pageNumber,
                       @ApiParam(name = "pageSize", value = "每页条目", required = true) @RequestParam("pageSize") Integer pageSize) {
        try {
            return iRecommendOrderService.recommendedOrder(pageNumber, pageSize);
        } catch (Exception e) {
            log.error("获取推荐交易对数据异常", e);
            return R.error();
        }
    }

}
