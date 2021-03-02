package com.blockinsight.basefi.controller;


import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.service.IMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 消息记录表 前端控制器
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Slf4j
@Api(tags = "消息记录")
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private IMessageService iMessageService;

    /**
     * @param userNum    用户地址
     * @param pageNumber 分页数量
     * @param pageSize   分页数量
     * @return 用户消息列表
     */
    @ApiOperation(value = "查询用户消息列表", notes = "查询用户消息列表", httpMethod = "GET")
    @GetMapping("/message-lists")
    public R messageLists(@ApiParam(name = "chainType", value = "链类型") @RequestParam("chainType") Integer chainType,
                          @ApiParam(name = "userNum", value = "用户地址") @RequestParam("userNum") String userNum,
                          @ApiParam(name = "messageTypeName", value = "类型名称") @RequestParam("messageTypeName") String messageTypeName,
                          @ApiParam(name = "pageNumber", value = "分页数量") @RequestParam("pageNumber") Integer pageNumber,
                          @ApiParam(name = "pageSize", value = "分页数量") @RequestParam("pageSize") Integer pageSize) {
        try {
            return iMessageService.messageS(chainType, userNum, messageTypeName, pageNumber, pageSize);
        } catch (Exception e) {
            log.error("查询用户消息列表异常", e);
            return R.error();
        }
    }

    /**
     * @param userNum    用户地址
     * @return 消息红点数量
     */
    @ApiOperation(value = "消息红点数量", notes = "消息红点数量", httpMethod = "GET")
    @GetMapping("/message-read-count")
    public R messageReadCount(@ApiParam(name = "chainType", value = "链类型") @RequestParam("chainType") Integer chainType,
                              @ApiParam(name = "userNum", value = "用户地址") @RequestParam("userNum") String userNum) {
        try {
            return iMessageService.messageReadCount(chainType, userNum);
        } catch (Exception e) {
            log.error("查询消息红点数量异常", e);
            return R.error();
        }
    }

}
