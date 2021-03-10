package com.blockinsight.basefi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blockinsight.basefi.common.constant.BaseConstants;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.common.util.BeanCopyUtils;
import com.blockinsight.basefi.entity.Config;
import com.blockinsight.basefi.entity.Order;
import com.blockinsight.basefi.entity.RevenuePool;
import com.blockinsight.basefi.entity.YesterDayOrderNum;
import com.blockinsight.basefi.entity.dto.OrderDto;
import com.blockinsight.basefi.mapper.OrderMapper;
import com.blockinsight.basefi.service.IConfigService;
import com.blockinsight.basefi.service.IOrderService;
import com.blockinsight.basefi.service.IYesterDayOrderNumService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private IConfigService iConfigService;
    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IYesterDayOrderNumService iYesterDayOrderNumService;

    @Override
    public R recommendedOrder(Integer pageNumber, Integer pageSize) {
        log.warn("推荐订单 pageNumber:{} pageSize:{}", pageNumber, pageSize);
        /*try {
            // 获取当前节点最新区块号
            EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
            String endBlockNumber = ethBlockNumber.getBlockNumber().toString();
            IPage iPage = new Page(pageNumber, pageSize);
            IPage<Order> page = this.page(iPage, new LambdaQueryWrapper<Order>()
                    .and(i -> i.eq(Order::getBuyerAddr, "0x0000000000000000000000000000000000000000")
                            .or()
                            .eq(Order::getSellerAddr, "0x0000000000000000000000000000000000000000"))
                    .gt(Order::getOrderStatus, "0")
                    .le(Order::getEffectiveHeight, endBlockNumber)
                    .orderByDesc(Order::getRecommendedValue));
            List<OrderDto> orderDtolist = new ArrayList<>();
            for (Order record : page.getRecords()) {
                OrderDto messageDto = new OrderDto();
                BeanCopyUtils.copyNotNullProperties(record, messageDto);
                orderDtolist.add(messageDto);
            }
            IPage<OrderDto> orderDtoiPage = new Page<>();
            BeanCopyUtils.copyNotNullProperties(page, orderDtoiPage);
            orderDtoiPage.setRecords(orderDtolist);
            return R.ok().put("data", orderDtoiPage);
        } catch (Exception e) {
            log.error("推荐订单异常", e);
            return R.error();
        }*/
        return R.error();
    }

    @Override
    public R buyOrSell(Order.OrderInpParam orderInpParam) {
        log.warn("查询买/卖订单列表参数 orderInpParam:{}", orderInpParam.toString());
        try {
            Web3j hbWeb3j = null;
            Web3j baWeb3j = null;
            Web3j ethWeb3j = null;
            List<Config> chainList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                    .in(Config::getIndexName, "Hb_Chain", "Ba_Chain", "Eth_Chain"));
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30*1000, TimeUnit.MILLISECONDS);
            builder.writeTimeout(30*1000, TimeUnit.MILLISECONDS);
            builder.readTimeout(30*1000, TimeUnit.MILLISECONDS);
            OkHttpClient httpClient = builder.build();
            for (Config config : chainList) {
                if ("Hb_Chain".equals(config.getIndexName())) {
                    hbWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                } else if ("Ba_Chain".equals(config.getIndexName())) {
                    baWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                } else if ("Eth_Chain".equals(config.getIndexName())) {
                    ethWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                }
            }
            // 获取当前节点最新区块号
            EthBlockNumber hbBlockNumber = hbWeb3j.ethBlockNumber().send();
            EthBlockNumber baBlockNumber = baWeb3j.ethBlockNumber().send();
            EthBlockNumber ethBlockNumber = ethWeb3j.ethBlockNumber().send();
            String hbEndBlockNumber = hbBlockNumber.getBlockNumber().toString();
            String baEndBlockNumber = baBlockNumber.getBlockNumber().toString();
            String ethEndBlockNumber = ethBlockNumber.getBlockNumber().toString();
            String sql = "";
            if (orderInpParam.getChainType() == BaseConstants.ChainType.HB.getCode()) {
                sql = " chain_type = 1 and effective_height + contract_initialize_block_number > " + hbEndBlockNumber;
            } else if (orderInpParam.getChainType() == BaseConstants.ChainType.BA.getCode()) {
                sql = " chain_type = 2 and effective_height + contract_initialize_block_number > " + baEndBlockNumber;
            } else if (orderInpParam.getChainType() == BaseConstants.ChainType.ETH.getCode()) {
                sql = " chain_type = 3 and effective_height + contract_initialize_block_number > " + ethEndBlockNumber;
            } else {
                String splic = " effective_height + contract_initialize_block_number > ";
                sql = " case chain_type when 1 then " + splic + hbEndBlockNumber +
                        " when 2 then " + splic + baEndBlockNumber +
                        " when 3 then " + splic + ethEndBlockNumber + " end ";
            }
            IPage<Order> iPage = new Page<>(orderInpParam.getPageNumber(), orderInpParam.getPageSize());
            LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderLambdaQueryWrapper.and(e -> e.eq(Order::getSellerAddr, "0x0000000000000000000000000000000000000000").
                    or().eq(Order::getBuyerAddr, "0x0000000000000000000000000000000000000000"));
            orderLambdaQueryWrapper.eq(StringUtils.isNotBlank(orderInpParam.getSellerSubject()), Order::getSellerSubjectMatterAddr, orderInpParam.getSellerSubject());
            orderLambdaQueryWrapper.eq(StringUtils.isNotBlank(orderInpParam.getBuyerSubject()), Order::getBuyerSubjectMatterAddr, orderInpParam.getBuyerSubject());
            orderLambdaQueryWrapper.eq(StringUtils.isNotBlank(orderInpParam.getOrderNumber()), Order::getOrderNum, orderInpParam.getOrderNumber());
            orderLambdaQueryWrapper.apply(sql);
            orderLambdaQueryWrapper.orderByDesc(Order::getContractCreateTime);
            IPage<Order> page = this.page(iPage, orderLambdaQueryWrapper);
            return R.ok().put("data", page);
        } catch (Exception e) {
            log.error("查询买/卖订单列表异常", e);
            return R.error();
        }
    }

    @Override
    public R orderDetails(String orderNumber) {
        log.warn("查询订单详情参数 orderNumber:{}", orderNumber);
        try {
            LambdaQueryWrapper<Order> orderQueryWrapper = new LambdaQueryWrapper<>();
            orderQueryWrapper.eq(Order::getOrderNum, orderNumber);
            Order order = baseMapper.selectOne(orderQueryWrapper);
            return R.ok().put("data", order);
        } catch (Exception e) {
            log.error("查询订单详情异常", e);
            return R.error();
        }
    }

    @Override
    public R userOrderList(Integer chainType, String userNum, String searchFor, Integer createStatus, Integer statusOne, Integer pageNumber, Integer pageSize) {
        log.warn("查询用戶订单列表参数 chainType:{} userNum:{},searchFor:{},createStatus:{},statusOne:{}", chainType, userNum, searchFor, createStatus, statusOne);
        try {
            IPage<Order> iPage = new Page<>(pageNumber, pageSize);
            LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<Order>();
            if (createStatus == 1) {
                orderLambdaQueryWrapper.eq(Order::getContractCreatorAddr, userNum);
            }
            Web3j hbWeb3j = null;
            Web3j baWeb3j = null;
            Web3j ethWeb3j = null;
            List<Config> chainList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                    .in(Config::getIndexName, "Hb_Chain", "Ba_Chain", "Eth_Chain"));
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30*1000, TimeUnit.MILLISECONDS);
            builder.writeTimeout(30*1000, TimeUnit.MILLISECONDS);
            builder.readTimeout(30*1000, TimeUnit.MILLISECONDS);
            OkHttpClient httpClient = builder.build();
            for (Config config : chainList) {
                if ("Hb_Chain".equals(config.getIndexName())) {
                    hbWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                } else if ("Ba_Chain".equals(config.getIndexName())) {
                    baWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                } else if ("Eth_Chain".equals(config.getIndexName())) {
                    ethWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                }
            }
            // 获取当前节点最新区块号
            EthBlockNumber hbBlockNumber = hbWeb3j.ethBlockNumber().send();
            EthBlockNumber baBlockNumber = baWeb3j.ethBlockNumber().send();
            EthBlockNumber ethBlockNumber = ethWeb3j.ethBlockNumber().send();

            String hbEndBlockNumber = hbBlockNumber.getBlockNumber().toString();
            String baEndBlockNumber = baBlockNumber.getBlockNumber().toString();
            String ethEndBlockNumber = ethBlockNumber.getBlockNumber().toString();
            String currentEndBlockNumber = "";
            if (chainType == BaseConstants.ChainType.HB.getCode()) {
                currentEndBlockNumber = hbEndBlockNumber;
            } else if (chainType == BaseConstants.ChainType.BA.getCode()) {
                currentEndBlockNumber = baEndBlockNumber;
            } else if (chainType == BaseConstants.ChainType.ETH.getCode()) {
                currentEndBlockNumber = ethEndBlockNumber;
            }
            log.warn("当前区块高度 currentEndBlockNumber:{}", currentEndBlockNumber);
            switch (statusOne) {
                case 2:
                    log.warn("查询待支付保证金 statusOne:{}", statusOne);
                    String splic = " contract_initialize_block_number + effective_height > ";
                    String sql = "";
                    if (chainType != BaseConstants.ChainType.ALL.getCode()) {
                        sql = splic + currentEndBlockNumber;
                    } else {
                        sql = "case chain_type when 1 then " + splic + hbEndBlockNumber +
                                " when 2 then " + splic + baEndBlockNumber + " when 3 then " + splic + ethEndBlockNumber
                                + " end ";
                    }
                   /*
                   （判断生效时间）
                    待支付保证金
                    等待卖家
                    等待买家*/
                    orderLambdaQueryWrapper.and(i -> i
                            .eq(Order::getContractCreatorAddr, userNum)
                            .or()
                            .eq(Order::getBuyerAddr, userNum)
                            .or()
                            .eq(Order::getSellerAddr, userNum))
                            .apply(sql)
                            .in(Order::getOrderStatus, BaseConstants.OrderStatus.BUYER_SELLER.getCode(),
                                    BaseConstants.OrderStatus.WAIT_SELLER.getCode(),
                                    BaseConstants.OrderStatus.WAIT_BUYER.getCode(),
                                    BaseConstants.OrderStatus.INITIALIZE.getCode())
                            .in(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.BUYER_PAY.getCode(),
                                    BaseConstants.EarnestMoneyStatus.SELLER_PAY.getCode(),
                                    BaseConstants.EarnestMoneyStatus.NOT_PAY.getCode());
                    break;
                case 4:
                    log.warn("查询已支付保证金 statusOne:{}", statusOne);
                    /*
                    （判断生效时间）
                    已支付保证金*/
                    splic = " contract_initialize_block_number + effective_height > ";
                    if (chainType != BaseConstants.ChainType.ALL.getCode()) {
                        sql = splic + currentEndBlockNumber;
                    } else {
                        sql = "case chain_type when 1 then " + splic + hbEndBlockNumber +
                                " when 2 then " + splic + baEndBlockNumber + " when 3 then " + splic + ethEndBlockNumber
                                + " end ";
                    }
                    orderLambdaQueryWrapper.and(i -> i
                            .eq(Order::getContractCreatorAddr, userNum)
                            .or()
                            .eq(Order::getBuyerAddr, userNum)
                            .or()
                            .eq(Order::getSellerAddr, userNum))
                            .apply(sql)
                            .eq(Order::getOrderStatus, BaseConstants.OrderStatus.BUYER_SELLER.getCode())
                            .eq(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode());
                    break;
                case 6:
                    log.warn("查询等待交割 statusOne:{}", statusOne);
                    /*
                    （判断交割时间）
                     6为等待交割*/
                    splic = " contract_initialize_block_number + effective_height <= ";
                    String splic2 = " and contract_initialize_block_number + delivery_height > ";
                    if (chainType != BaseConstants.ChainType.ALL.getCode()) {
                        sql = splic + currentEndBlockNumber + splic2 + currentEndBlockNumber;
                    } else {
                        sql = "case chain_type when 1 then " + splic + hbEndBlockNumber + splic2 + hbEndBlockNumber +
                                " when 2 then " + splic + baEndBlockNumber + splic2 + baEndBlockNumber +
                                " when 3 then " + splic + ethEndBlockNumber + splic2 + ethEndBlockNumber
                                + " end ";
                    }
                    orderLambdaQueryWrapper.and(i -> i
                            .eq(Order::getContractCreatorAddr, userNum)
                            .or()
                            .eq(Order::getBuyerAddr, userNum)
                            .or()
                            .eq(Order::getSellerAddr, userNum))
                            .apply(sql)
                            .eq(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode());
                    break;
                case 7:
                    log.warn("查询已交割 statusOne:{}", statusOne);
                    /*
                    （判断交割时间）
                     7为已交割*/
                    splic = " contract_initialize_block_number + delivery_height <= ";
                    if (chainType != BaseConstants.ChainType.ALL.getCode()) {
                        sql = splic + currentEndBlockNumber;
                    } else {
                        sql = "case chain_type when 1 then " + splic + hbEndBlockNumber +
                                " when 2 then " + splic + baEndBlockNumber + " when 3 then " + splic + ethEndBlockNumber
                                + " end ";
                    }
                    orderLambdaQueryWrapper.and(i -> i
                            .eq(Order::getContractCreatorAddr, userNum)
                            .or()
                            .eq(Order::getBuyerAddr, userNum).or()
                            .eq(Order::getSellerAddr, userNum))
                            .apply(sql)
                            .eq(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode());
                    break;
                case 8:
                    log.warn("查询订单失效 statusOne:{}", statusOne);
                   /*
                   （判断生效时间）
                    和
                    ((买家已付（买家支付保证金数量） < 买方保证金（交割数量 * 交割价格 * 保证金比例）
                    或
                    卖家已付（卖家支付保证金数量）<卖方保证金（交割数量 * 保证金比例))
                    订单失效*/
                    splic = " contract_initialize_block_number + effective_height <= ";
                    if (chainType != BaseConstants.ChainType.ALL.getCode()) {
                        sql = splic + currentEndBlockNumber;
                    } else {
                        sql = "case chain_type when 1 then " + splic + hbEndBlockNumber +
                                " when 2 then " + splic + baEndBlockNumber + " when 3 then " + splic + ethEndBlockNumber
                                + " end ";
                    }
                    orderLambdaQueryWrapper.and(i -> i
                            .eq(Order::getContractCreatorAddr, userNum)
                            .or()
                            .eq(Order::getBuyerAddr, userNum)
                            .or()
                            .eq(Order::getSellerAddr, userNum))
                            .apply(sql)
                            .ne(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode());
                    break;
                case 9:
                    log.warn("查询我创建的订单 statusOne:{}", statusOne);
                    if (chainType != BaseConstants.ChainType.ALL.getCode()) {
                        orderLambdaQueryWrapper.eq(Order::getChainType, chainType);
                    }
                    /*
                    (状态不为0，创建人）
                    我创建的*/
                    orderLambdaQueryWrapper.eq(Order::getContractCreatorAddr, userNum);
                    break;
                default:
                    log.warn("查询全部订单 statusOne:{}", statusOne);
                    if (chainType != BaseConstants.ChainType.ALL.getCode()) {
                        orderLambdaQueryWrapper.eq(Order::getChainType, chainType);
                    }
                    orderLambdaQueryWrapper.and(i -> i
                            .eq(Order::getContractCreatorAddr, userNum)
                            .or()
                            .eq(Order::getBuyerAddr, userNum)
                            .or()
                            .eq(Order::getSellerAddr, userNum));
                    break;
            }
            if (StringUtils.isNotEmpty(searchFor)) {
                log.warn("查询指定内容 searchFor:{}", searchFor);
                orderLambdaQueryWrapper.eq(Order::getOrderNum, searchFor)
                        .or()
                        .eq(Order::getSellerSubjectMatterAddr, searchFor)
                        .or()
                        .eq(Order::getBuyerSubjectMatterAddr, searchFor);
            }
            orderLambdaQueryWrapper.orderByDesc(Order::getContractCreateTime);
            IPage<Order> page = this.page(iPage, orderLambdaQueryWrapper);
            return R.ok().put("data", page);
        } catch (Exception e) {
            log.error("查询用户订单列表异常", e);
            return R.error();
        }
    }

    @Override
    public int updateOrderById(List<Order> list) {
        return baseMapper.updateOrderById(list);
    }

    @Override
    public R orderCount(Integer chainType) {
        try {
            Map<String, Object> result = new HashMap<>();
            Web3j hbWeb3j = null;
            Web3j baWeb3j = null;
            Web3j ethWeb3j = null;
            List<Config> chainList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                    .in(Config::getIndexName, "Hb_Chain", "Ba_Chain", "Eth_Chain"));
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30*1000, TimeUnit.MILLISECONDS);
            builder.writeTimeout(30*1000, TimeUnit.MILLISECONDS);
            builder.readTimeout(30*1000, TimeUnit.MILLISECONDS);
            OkHttpClient httpClient = builder.build();
            for (Config config : chainList) {
                if ("Hb_Chain".equals(config.getIndexName())) {
                    hbWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                } else if ("Ba_Chain".equals(config.getIndexName())) {
                    baWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                } else if ("Eth_Chain".equals(config.getIndexName())) {
                    ethWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
                }
            }
            // 获取当前节点最新区块号
            EthBlockNumber hbBlockNumber = hbWeb3j.ethBlockNumber().send();
            EthBlockNumber baBlockNumber = baWeb3j.ethBlockNumber().send();
            EthBlockNumber ethBlockNumber = ethWeb3j.ethBlockNumber().send();
            String hbEndBlockNumber = hbBlockNumber.getBlockNumber().toString();
            String baEndBlockNumber = baBlockNumber.getBlockNumber().toString();
            String ethEndBlockNumber = ethBlockNumber.getBlockNumber().toString();
            if (chainType == BaseConstants.ChainType.ALL.getCode()) {
                int count = iOrderService.count();
                Map<String, Object> orderNumber = new HashMap<>();
                orderNumber.put("totalNum", count);
                String splic = " contract_initialize_block_number + effective_height > ";
                String sql = " case chain_type when 1 then " + splic + hbEndBlockNumber +
                        " when 2 then " + splic + baEndBlockNumber +
                        " when 3 then" + splic + ethEndBlockNumber + " end ";
                int waitEffectNum = iOrderService.count(new LambdaUpdateWrapper<Order>()
                        .apply(sql));
                orderNumber.put("waitEffectNum", waitEffectNum);
                splic = " contract_initialize_block_number + effective_height < ";
                String splic2 = " and contract_initialize_block_number + delivery_height > ";
                sql = " case chain_type when 1 then " + splic + hbEndBlockNumber + splic2 + hbEndBlockNumber +
                        " when 2 then " + splic + baEndBlockNumber + splic2 + baEndBlockNumber +
                        " when 3 then" + splic + ethEndBlockNumber + splic2 + ethEndBlockNumber + " end ";
                int waitDeliveryNum = iOrderService.count(new LambdaUpdateWrapper<Order>()
                        .eq(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode())
                        .apply(sql));
                orderNumber.put("waitDeliveryNum", waitDeliveryNum);
                result.put("orderNum", orderNumber);
                List<YesterDayOrderNum> ylist = iYesterDayOrderNumService.list(new LambdaUpdateWrapper<YesterDayOrderNum>()
                        .orderByDesc(BaseEntity::getCreateTime));
                YesterDayOrderNum yesterDayOrderNum;
                if (ylist.isEmpty()) {
                    yesterDayOrderNum = new YesterDayOrderNum();
                    yesterDayOrderNum.setTotalNum(0);
                    yesterDayOrderNum.setWaitDeliveryNum(0);
                    yesterDayOrderNum.setWaitEffectNum(0);
                    yesterDayOrderNum.setChainType(BaseConstants.ChainType.HB.getCode());
                    iYesterDayOrderNumService.save(yesterDayOrderNum);
                    yesterDayOrderNum.setChainType(BaseConstants.ChainType.BA.getCode());
                    iYesterDayOrderNumService.save(yesterDayOrderNum);
                    yesterDayOrderNum.setChainType(BaseConstants.ChainType.ETH.getCode());
                    iYesterDayOrderNumService.save(yesterDayOrderNum);
                } else {
                    YesterDayOrderNum yesterDay = new YesterDayOrderNum();
                    yesterDay.setWaitDeliveryNum(0);
                    yesterDay.setWaitEffectNum(0);
                    yesterDay.setTotalNum(0);
                    for (int i = 0; i < ylist.size(); i++) {
                        if (i == 3)
                            break;
                        YesterDayOrderNum dayOrderNum = ylist.get(i);
                        yesterDay.setTotalNum(yesterDay.getTotalNum() + dayOrderNum.getTotalNum());
                        yesterDay.setWaitEffectNum(yesterDay.getWaitEffectNum() + dayOrderNum.getWaitEffectNum());
                        yesterDay.setWaitDeliveryNum(yesterDay.getWaitDeliveryNum() + dayOrderNum.getWaitDeliveryNum());
                    }
                    yesterDayOrderNum = yesterDay;
                }
                result.put("yesterDayOrderNum", yesterDayOrderNum);
            } else {
                int count = iOrderService.count(new LambdaUpdateWrapper<Order>().eq(Order::getChainType, chainType));
                String currentBlockNumber = "";
                if (chainType == BaseConstants.ChainType.HB.getCode()) {
                    currentBlockNumber = hbEndBlockNumber;
                } else if (chainType == BaseConstants.ChainType.BA.getCode()) {
                    currentBlockNumber = baEndBlockNumber;
                } else if (chainType == BaseConstants.ChainType.ETH.getCode()) {
                    currentBlockNumber = ethEndBlockNumber;
                }
                Map<String, Object> orderNumber = new HashMap<>();
                orderNumber.put("totalNum", count);
                String sql = " contract_initialize_block_number + effective_height > " + currentBlockNumber;
                int waitEffectNum = iOrderService.count(new LambdaUpdateWrapper<Order>()
                        .apply(sql));
                orderNumber.put("waitEffectNum", waitEffectNum);
                sql = " contract_initialize_block_number + effective_height < " + currentBlockNumber +
                        " and contract_initialize_block_number + delivery_height > " + currentBlockNumber;
                int waitDeliveryNum = iOrderService.count(new LambdaUpdateWrapper<Order>()
                        .eq(Order::getEarnestMoneyStatus, BaseConstants.EarnestMoneyStatus.ALL_PAY.getCode())
                        .apply(sql));
                orderNumber.put("waitDeliveryNum", waitDeliveryNum);
                result.put("orderNum", orderNumber);
                List<YesterDayOrderNum> ylist = iYesterDayOrderNumService.list(new LambdaUpdateWrapper<YesterDayOrderNum>()
                        .eq(YesterDayOrderNum::getChainType, chainType).orderByDesc(BaseEntity::getCreateTime));
                YesterDayOrderNum yesterDayOrderNum;
                if (ylist.isEmpty()) {
                    yesterDayOrderNum = new YesterDayOrderNum();
                    yesterDayOrderNum.setTotalNum(0);
                    yesterDayOrderNum.setWaitDeliveryNum(0);
                    yesterDayOrderNum.setWaitEffectNum(0);
                    yesterDayOrderNum.setChainType(chainType);
                    iYesterDayOrderNumService.save(yesterDayOrderNum);
                } else {
                    yesterDayOrderNum = ylist.get(0);
                }
                result.put("yesterDayOrderNum", yesterDayOrderNum);
            }
            return R.ok().put("data", result);
        } catch (Exception e) {
            log.error("首页订单数量异常", e);
            return R.error();
        }
    }
}
