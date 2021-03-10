package com.blockinsight.basefi.controller;


import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.service.ILockUpRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Janin
 * @since 2021-02-02
 */

@Api(tags = "锁仓")
@Slf4j
@RestController
@RequestMapping("/lock-up-record")
public class LockUpRecordController {

    @Autowired
    private ILockUpRecordService iLockUpRecordService;

    @ApiOperation(value = "首页数据展示", notes = "首页数据展示", httpMethod = "GET")
    @GetMapping("")
    public R homePage(@RequestParam("chainType") Integer chainType) {
        try {
            return iLockUpRecordService.homePage(chainType);
        } catch (Exception e) {
            log.error("首页数据展示异常", e);
            return R.error();
        }
    }

}
