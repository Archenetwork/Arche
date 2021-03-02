package com.blockinsight.basefi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.Order;
import com.blockinsight.basefi.entity.UserTokenRecord;
import com.blockinsight.basefi.mapper.UserTokenRecordMapper;
import com.blockinsight.basefi.service.IOrderService;
import com.blockinsight.basefi.service.IUserTokenRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 用户代币记录表 服务实现类
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Service
@Slf4j
public class UserTokenRecordServiceImpl extends ServiceImpl<UserTokenRecordMapper, UserTokenRecord> implements IUserTokenRecordService {

    @Autowired
    private IOrderService orderService;

    @Override
    public R userTokenList(String userAddr, Integer status, Integer pageNumber, Integer pageSize) {
        log.warn("查询个人代币记录列表参数 userAddr:{} status:{}", userAddr, status);
        try {
            HashMap<String, Object> stringStringHashMap = new HashMap<>(5);
            stringStringHashMap.put("pageNumber", pageNumber);
            stringStringHashMap.put("pageSize", pageSize);
            stringStringHashMap.put("userAddr", userAddr);
            stringStringHashMap.put("status", status);
            IPage iPage = new Page();
            long total = baseMapper.userTokenListCount(stringStringHashMap);
            List<UserTokenRecord> userTokenRecords = baseMapper.userTokenList(stringStringHashMap);
            iPage.setTotal(total);
            iPage.setRecords(userTokenRecords);
            return R.ok().put("data", iPage);
        } catch (Exception e) {
            log.error("查询个人代币记录列表异常", e);
            return R.error();
        }
    }

    @Override
    public R checkingOrder(String checkName) {
        log.warn("查询订单参数 checkName:{}", checkName);
        UserTokenRecord userTokenRecords = baseMapper.searchBar(checkName);
        return R.ok().put("data", userTokenRecords);
    }

    @Override
    public R addToUserTokenList(String userNum, String tokenAddr) {
        log.warn("查询添加个人代币记录参数 userNum:{},tokenAddr{}", userNum, tokenAddr);
        try {
            LambdaQueryWrapper<UserTokenRecord> userTokenRecordqueryWrapper = new LambdaQueryWrapper<>();
            Integer integer = baseMapper.selectCount(
                    userTokenRecordqueryWrapper.eq(UserTokenRecord::getUserNum, userNum).eq(UserTokenRecord::getOrderNum, tokenAddr));
            if (integer > 0) {
                return R.error("该代币已存在，请勿重复添加");
            } else {
                LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.select(Order::getOrderNum,
                        Order::getOrderStatus,
                        Order::getBuyerTokenName,
                        Order::getSellerTokenName,
                        Order::getBuyerTokenAddr,
                        Order::getSellerTokenAddr,
                        Order::getCreateTime,
                        Order::getUpdateTime).and(e ->
                        e.eq(Order::getBuyerTokenAddr, tokenAddr).or().eq(Order::getSellerTokenAddr, tokenAddr));
                Order order = orderService.getOne(queryWrapper);
                UserTokenRecord userTokenRecord = new UserTokenRecord();
                userTokenRecord.setUserNum(userNum);
                userTokenRecord.setOrderNum(order.getOrderNum());
                userTokenRecord.setOrderStatus(order.getOrderStatus());
                baseMapper.insert(userTokenRecord);
                return R.ok();
            }
        } catch (Exception e) {
            log.error("添加个人代币记录异常", e);
            return R.error();
        }
    }

    @Override
    public R delToUserTokenList(String userNum, String orderNum) {
        log.warn("删除个人代币记录参数 userNum:{},orderNum{}", userNum, orderNum);
        try {
            HashMap<String, Object> deleteCondition = new HashMap<>();
            deleteCondition.put("user_Num", userNum);
            deleteCondition.put("order_Num", orderNum);
            int i = baseMapper.deleteByMap(deleteCondition);
            if (i == 0) {
                return R.ok().put("data", 0);
            }
            return R.ok().put("data", 1);
        } catch (Exception e) {
            log.error("删除个人代币记录异常", e);
            return R.error();
        }
    }


}



