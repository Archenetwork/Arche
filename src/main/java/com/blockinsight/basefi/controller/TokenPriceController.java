package com.blockinsight.basefi.controller;


import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.service.ITokenPriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 代币价格 前端控制器
 * </p>
 *
 * @author Janin
 * @since 2021-02-03
 */
@Api(tags = "基础代币")
@Slf4j
@RestController
@RequestMapping("/token-price")
public class TokenPriceController {

    @Autowired
    private ITokenPriceService iTokenPriceService;

    @ApiOperation(value = "新增基础代币", notes = "新增基础代币", httpMethod = "POST")
    @PostMapping("")
    public R saveTokenPrice(@ApiParam(name = "name", value = "代币名称", required = true)
                                @RequestParam("name") String name,
                            @ApiParam(name = "tokenAddr", value = "代币地址", required = true)
                            @RequestParam("tokenAddr") String tokenAddr,
                            @ApiParam(name = "img", value = "图片", required = true)
                                @RequestParam("img") String img) {
        try {
            return iTokenPriceService.saveTokenPrice(name, tokenAddr, img);
        } catch (Exception e) {
            log.error("新增基础代币异常", e);
            return R.error();
        }
    }

    @ApiOperation(value = "查询基础代币", notes = "查询基础代币", httpMethod = "GET")
    @GetMapping("")
    public R getTokenPrice(@ApiParam(name = "addr", value = "地址", required = true)
                            @RequestParam("addr") String addr) {
        try {
            return iTokenPriceService.getTokenPrice(addr);
        } catch (Exception e) {
            log.error("查询基础代币异常", e);
            return R.error();
        }
    }
}
