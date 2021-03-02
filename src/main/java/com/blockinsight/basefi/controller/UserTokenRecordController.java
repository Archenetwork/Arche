package com.blockinsight.basefi.controller;


import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.service.IUserTokenRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户代币记录表 前端控制器
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Api(tags = "用户代币记录表")
@Slf4j
@RestController
@RequestMapping("/user-token-record")
public class UserTokenRecordController {
    @Autowired
    private IUserTokenRecordService iUserTokenRecordService;

    /**
     * @param checkName 查询条件(代币名称/代币地址/订单编号)
     * @return 查询订单
     */
    @ApiOperation(value = "查询订单", notes = "查询订单", httpMethod = "GET")
    @GetMapping("/checking-order")
    public R checkingOrder(@ApiParam(name = "checkName", value = "查询条件(代币名称/代币地址/订单编号)", required = true) @RequestParam("checkName") String checkName) {
        try {
            return iUserTokenRecordService.checkingOrder(checkName);
        } catch (Exception e) {
            log.error("");
            return R.error();
        }
    }

    /**
     * @param pageNumber 当前页
     * @param pageSize   每页数量
     * @param userNum    用户公钥
     * @param status     状态(1待支付保证金 2已支付保证金 3等待交割 4已交割)
     * @return 个人代币记录列表
     */
    @ApiOperation(value = "个人代币记录列表", notes = "个人代币记录列表", httpMethod = "GET")
    @GetMapping("/user-token")
    public R userTokenList(@ApiParam(name = "userNum", value = "用户公钥", required = true) @RequestParam("userNum") String userNum,
                           @ApiParam(name = "status", value = "状态(1待支付保证金 2已支付保证金 3等待交割 4已交割)", required = true) @RequestParam("status") Integer status,
                           @ApiParam(name = "pageNumber", value = "当前页", required = true) @RequestParam("pageNumber") Integer pageNumber,
                           @ApiParam(name = "pageSize", value = "每页数量", required = true) @RequestParam("pageSize") Integer pageSize) {
        return iUserTokenRecordService.userTokenList(userNum, status, pageNumber, pageSize);
    }

    /**
     * @param userNum  用户公钥
     * @param tokenAddr 代币地址
     * @return 添加个人代币记录
     */
    @ApiOperation(value = "添加个人代币记录", notes = "添加个人代币记录", httpMethod = "POST")
    @PostMapping("/user-token")
    public R addToUserTokenList(@ApiParam(name = "userNum", value = "用户公钥", required = true) @RequestParam("userNum") String userNum,
                                @ApiParam(name = "tokenAddr", value = "订单编号", required = true) @RequestParam("tokenAddr") String tokenAddr) {
        return iUserTokenRecordService.addToUserTokenList(userNum, tokenAddr);
    }

    /**
     * @param userNum  用户公钥
     * @param orderNum 订单编号
     * @return 删除个人代币记录
     */
    @ApiOperation(value = "删除个人代币记录", notes = "删除个人代币记录", httpMethod = "DELETE")
    @DeleteMapping("/user-token")
    public R delToUserTokenList(@ApiParam(name = "userNum", value = "用户公钥", required = true) @RequestParam("userNum") String userNum,
                                @ApiParam(name = "orderNum", value = "订单编号", required = true) @RequestParam("orderNum") String orderNum) {
        return iUserTokenRecordService.delToUserTokenList(userNum, orderNum);
    }

}
