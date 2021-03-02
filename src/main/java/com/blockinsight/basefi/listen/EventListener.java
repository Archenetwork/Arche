package com.blockinsight.basefi.listen;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blockinsight.basefi.common.constant.BaseConstants;
import com.blockinsight.basefi.common.rabbitmq.provider.RabbitmqProvider;
import com.blockinsight.basefi.common.util.DateUtils;
import com.blockinsight.basefi.common.util.RedisUtils;
import com.blockinsight.basefi.entity.*;
import com.blockinsight.basefi.service.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class EventListener implements ApplicationRunner {


    @Autowired
    private RabbitmqProvider rabbitmqProvider;
    @Autowired
    private Web3j web3j;
    @Autowired
    private IConfigService iConfigService;
    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IDepositRecordService iDepositRecordService;
    @Autowired
    private IEarnestMoneyRecordService iEarnestMoneyRecordService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ITokenPriceService iTokenPriceService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int waitTime = Integer.parseInt(iConfigService.getConfig("WAIT_TIME"));
        // 获取各个链开始区块
        List<Config> startBlockNumberList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                .in(Config::getIndexName, "Hb_Start_Block_Number", "Ba_Start_Block_Number", "Eth_Start_Block_Number"));
        // 获取各个链合约地址
        List<Config> contractAddrList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                .in(Config::getIndexName, "Hb_Swap_Contract_Address", "Ba_Swap_Contract_Address", "Eth_Swap_Contract_Address"));
        // 获取各个链节点
        List<Config> chainList = iConfigService.list(new LambdaUpdateWrapper<Config>()
                .in(Config::getIndexName, "Hb_Chain", "Ba_Chain", "Eth_Chain"));

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30*1000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(30*1000, TimeUnit.MILLISECONDS);
        builder.readTimeout(30*1000, TimeUnit.MILLISECONDS);
        OkHttpClient httpClient = builder.build();
        Web3j hbWeb3j = null;
        Web3j baWeb3j = null;
        Web3j ethWeb3j = null;
        for (Config config : chainList) {
            if ("Hb_Chain".equals(config.getIndexName())) {
                hbWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
            } else if ("Ba_Chain".equals(config.getIndexName())) {
                baWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
            } else if ("Eth_Chain".equals(config.getIndexName())) {
                ethWeb3j = Web3j.build(new HttpService(config.getIndexValue(), httpClient, false));
            }
        }
        Web3j currentWeb3j = null;

        String hbStartBlockNumber = null;
        String baStartBlockNumber = null;
        String ethStartBlockNumber = null;
        for (Config config : startBlockNumberList) {
            if ("Hb_Start_Block_Number".equals(config.getIndexName())) {
                hbStartBlockNumber = config.getIndexValue();
            } else if ("Ba_Start_Block_Number".equals(config.getIndexName())) {
                baStartBlockNumber = config.getIndexValue();
            } else if ("Eth_Start_Block_Number".equals(config.getIndexName())) {
                ethStartBlockNumber = config.getIndexValue();
            }
        }
        String currentStartBlockNumber = null;
        for (Config config : contractAddrList) {
            String redisKey = "";
            int chainType = 0;
            if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = hbWeb3j;
                currentStartBlockNumber = hbStartBlockNumber;
                redisKey = "hbBaseFiOrderCreate";
                chainType = BaseConstants.ChainType.HB.getCode();
            } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = baWeb3j;
                currentStartBlockNumber = baStartBlockNumber;
                redisKey = "baBaseFiOrderCreate";
                chainType = BaseConstants.ChainType.BA.getCode();
            } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = ethWeb3j;
                currentStartBlockNumber = ethStartBlockNumber;
                redisKey = "ethBaseFiOrderCreate";
                chainType = BaseConstants.ChainType.ETH.getCode();
            }

            Web3j finalCurrentWeb3j = currentWeb3j;
            String finalCurrentStartBlockNumber = currentStartBlockNumber;
            String finalRedisKey = redisKey;
            int finalChinaType = chainType;
            /**
             *  订单创建事件
             */
            Thread orderCreate = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object baseFiOrderCreate = redisUtils.get(finalRedisKey);
                    String start = "";
                    if (baseFiOrderCreate == null) {
                        start = finalCurrentStartBlockNumber;
                    } else {
                        start = baseFiOrderCreate.toString();
                    }
                    log.warn("监听订单创建事件 " + finalRedisKey + ":{}", start);
                    Event event = new Event("E_Create",
                            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Uint256>() {
                            }));
                    final BigInteger[] num = new BigInteger[1];
                    num[0] = new BigInteger(start);
                    EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(num[0]),
                            DefaultBlockParameterName.LATEST, config.getIndexValue());
                    ethFilter.addSingleTopic(EventEncoder.encode(event));
                    finalCurrentWeb3j.ethLogObservable(ethFilter).subscribe(log -> {
                        try {
                            EthBlock ethBlock = finalCurrentWeb3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(log.getBlockNumber()), true).send();
                            Date eventTime = DateUtils.eventTimeStamp(ethBlock.getResult().getTimestamp().toString());
                            EventValues eventValues = Contract.staticExtractEventParameters(event, log);
                            List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
                            Order order = new Order();
                            order.setContractCreateTime(eventTime);
                            order.setOrderNum(nonIndexedValues.get(0).getValue().toString());
                            order.setContractCreatorAddr(nonIndexedValues.get(1).getValue().toString());
                            TokenPrice buyerSubject = iTokenPriceService.getOne(new LambdaUpdateWrapper<TokenPrice>().eq(TokenPrice::getTokenAddr, nonIndexedValues.get(4).getValue().toString()));
                            if (buyerSubject != null) {
                                order.setBuyerSubjectMatter(buyerSubject.getName());
                                order.setBuyerSubjectMatterImg(buyerSubject.getImg());
                            }
                            order.setBuyerSubjectMatterAddr(nonIndexedValues.get(4).getValue().toString());
                            TokenPrice sellerSubject = iTokenPriceService.getOne(new LambdaUpdateWrapper<TokenPrice>().eq(TokenPrice::getTokenAddr, nonIndexedValues.get(3).getValue().toString()));
                            if (sellerSubject != null) {
                                order.setSellerSubjectMatter(sellerSubject.getName());
                                order.setSellerSubjectMatterImg(sellerSubject.getImg());
                            }
                            order.setSellerSubjectMatterAddr(nonIndexedValues.get(3).getValue().toString());
                            order.setMoneyReward(nonIndexedValues.get(5).getValue().toString());
                            order.setContractCreateBlockNumber(log.getBlockNumber().intValue());
                            order.setChainType(finalChinaType);
                            rabbitmqProvider.orderCreate(order);
                            redisUtils.set(finalRedisKey, log.getBlockNumber().intValue() + 1);
                        } catch (Exception e) {
                            EventListener.log.error("订单创建事件异常", e);
                        }
                    });
                }
            });
            orderCreate.start();
        }
        Thread.sleep(waitTime);

        for (Config config : contractAddrList) {
            String redisKey = "";
            int chinaType = 0;
            if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = hbWeb3j;
                currentStartBlockNumber = hbStartBlockNumber;
                redisKey = "hbBaseFiOrderInitialize";
                chinaType = BaseConstants.ChainType.HB.getCode();
            } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = baWeb3j;
                currentStartBlockNumber = baStartBlockNumber;
                redisKey = "baBaseFiOrderInitialize";
                chinaType = BaseConstants.ChainType.BA.getCode();
            } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = ethWeb3j;
                currentStartBlockNumber = ethStartBlockNumber;
                redisKey = "ethBaseFiOrderInitialize";
                chinaType = BaseConstants.ChainType.ETH.getCode();
            }

            Web3j finalCurrentWeb3j = currentWeb3j;
            String finalCurrentStartBlockNumber = currentStartBlockNumber;
            String finalRedisKey = redisKey;
            int finalChinaType = chinaType;
            /**
             *  订单初始化事件
             */
            Thread orderInitialize = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object baseFiOrderCreate = redisUtils.get(finalRedisKey);
                    String start = "";
                    if (baseFiOrderCreate == null) {
                        start = finalCurrentStartBlockNumber;
                    } else {
                        start = baseFiOrderCreate.toString();
                    }
                    log.warn("监听订单初始化事件 " + finalRedisKey + ":{}", start);
                    Event event = new Event("E_Initialize",
                            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Uint256>() {
                            }, new TypeReference<Uint256>() {
                            }, new TypeReference<Uint256>() {
                            }, new TypeReference<Uint256>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Uint256>() {
                            }, new TypeReference<Uint256>() {
                            }));
                    final BigInteger[] num = new BigInteger[1];
                    num[0] = new BigInteger(start);
                    EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(num[0]),
                            DefaultBlockParameterName.LATEST, config.getIndexValue());
                    ethFilter.addSingleTopic(EventEncoder.encode(event));
                    finalCurrentWeb3j.ethLogObservable(ethFilter).subscribe(log -> {
                        try {
                            EthBlock ethBlock = finalCurrentWeb3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(log.getBlockNumber()), true).send();
                            Date eventTime = DateUtils.eventTimeStamp(ethBlock.getResult().getTimestamp().toString());
                            EventValues eventValues = Contract.staticExtractEventParameters(event, log);
                            List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
                            Order order = new Order();
                            order.setOrderNum(nonIndexedValues.get(0).getValue().toString());
                            order.setContractInitializeTime(eventTime);
                            order.setBuyerDeliveryQuantity(nonIndexedValues.get(3).getValue().toString());
                            order.setSellerDeliveryQuantity(nonIndexedValues.get(2).getValue().toString());
                            order.setBuyerEarnestMoney(nonIndexedValues.get(5).getValue().toString());
                            order.setSellerEarnestMoney(nonIndexedValues.get(4).getValue().toString());
                            order.setBuyerAddr(nonIndexedValues.get(7).getValue().toString());
                            order.setSellerAddr(nonIndexedValues.get(6).getValue().toString());
                            order.setEffectiveHeight(Integer.parseInt(nonIndexedValues.get(8).getValue().toString()));
                            order.setDeliveryHeight(Integer.parseInt(nonIndexedValues.get(9).getValue().toString()));
                            order.setContractInitializeBlockNumber(log.getBlockNumber().intValue());
                            order.setChainType(finalChinaType);
                            rabbitmqProvider.orderInitialize(order);
                            redisUtils.set(finalRedisKey, log.getBlockNumber().intValue() + 1);
                        } catch (Exception e) {
                            EventListener.log.error("订单初始化事件异常", e);
                        }
                    });

                }
            });
            orderInitialize.start();
        }

        Thread.sleep(waitTime);

        for (Config config : contractAddrList) {
            String redisKey = "";
            int chinaType = 0;
            if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = hbWeb3j;
                currentStartBlockNumber = hbStartBlockNumber;
                redisKey = "hbBaseFiBuyerPayEarnestMoney";
                chinaType = BaseConstants.ChainType.HB.getCode();
            } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = baWeb3j;
                currentStartBlockNumber = baStartBlockNumber;
                redisKey = "baBaseFiBuyerPayEarnestMoney";
                chinaType = BaseConstants.ChainType.BA.getCode();
            } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = ethWeb3j;
                currentStartBlockNumber = ethStartBlockNumber;
                redisKey = "ethBaseFiBuyerPayEarnestMoney";
                chinaType = BaseConstants.ChainType.ETH.getCode();
            }

            Web3j finalCurrentWeb3j = currentWeb3j;
            String finalCurrentStartBlockNumber = currentStartBlockNumber;
            String finalRedisKey = redisKey;
            int finalChinaType = chinaType;
            /**
             *  买家支付保证金事件
             */
            Thread buyerPayEarnestMoney = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object baseFiOrderCreate = redisUtils.get(finalRedisKey);
                    String start = "";
                    if (baseFiOrderCreate == null) {
                        start = finalCurrentStartBlockNumber;
                    } else {
                        start = baseFiOrderCreate.toString();
                    }
                    log.warn("监听买家支付保证金事件 " + finalRedisKey + ":{}", start);
                    Event event = new Event("E_Claim_For_Tail",
                            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                                                            }, new TypeReference<Address>() {
                                                            },
                                    new TypeReference<Address>() {
                                    }));
                    final BigInteger[] num = new BigInteger[1];
                    num[0] = new BigInteger(start);
                    EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(num[0]),
                            DefaultBlockParameterName.LATEST, config.getIndexValue());
                    ethFilter.addSingleTopic(EventEncoder.encode(event));
                    finalCurrentWeb3j.ethLogObservable(ethFilter).subscribe(log -> {
                        try {
                            EarnestMoneyRecord.EarnestMoneyRecordParam earnestMoneyRecordParam = getEarnestMoneyRecordParam(event, log, finalCurrentWeb3j);
                            EventValues eventValues = Contract.staticExtractEventParameters(event, log);
                            earnestMoneyRecordParam.setReferer(eventValues.getNonIndexedValues().get(2).getValue().toString());
                            earnestMoneyRecordParam.setChainType(finalChinaType);
                            rabbitmqProvider.buyerPayEarnestMoney(earnestMoneyRecordParam);
                            redisUtils.set(finalRedisKey, log.getBlockNumber().intValue() + 1);
                        } catch (Exception e) {
                            EventListener.log.error("买家支付保证金事件异常", e);
                        }
                    });
                }
            });
            buyerPayEarnestMoney.start();
        }

        for (Config config : contractAddrList) {
            String redisKey = "";
            int chinaType = 0;
            if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = hbWeb3j;
                currentStartBlockNumber = hbStartBlockNumber;
                redisKey = "hbBaseFiSellerPayEarnestMoney";
                chinaType = BaseConstants.ChainType.HB.getCode();
            } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = baWeb3j;
                currentStartBlockNumber = baStartBlockNumber;
                redisKey = "baBaseFiSellerPayEarnestMoney";
                chinaType = BaseConstants.ChainType.BA.getCode();
            } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = ethWeb3j;
                currentStartBlockNumber = ethStartBlockNumber;
                redisKey = "ethBaseFiSellerPayEarnestMoney";
                chinaType = BaseConstants.ChainType.ETH.getCode();
            }

            Web3j finalCurrentWeb3j = currentWeb3j;
            String finalCurrentStartBlockNumber = currentStartBlockNumber;
            String finalRedisKey = redisKey;
            int finalChinaType = chinaType;
            /**
             *  卖家支付保证金事件
             */
            Thread sellerPayEarnestMoney = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object baseFiOrderCreate = redisUtils.get(finalRedisKey);
                    String start = "";
                    if (baseFiOrderCreate == null) {
                        start = finalCurrentStartBlockNumber;
                    } else {
                        start = baseFiOrderCreate.toString();
                    }
                    log.warn("监听卖家支付保证金事件 " + finalRedisKey + ":{}", start);
                    Event event = new Event("E_Claim_For_Head",
                            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                                                            }, new TypeReference<Address>() {
                                                            },
                                    new TypeReference<Address>() {
                                    }));
                    final BigInteger[] num = new BigInteger[1];
                    num[0] = new BigInteger(start);
                    EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(num[0]),
                            DefaultBlockParameterName.LATEST, config.getIndexValue());
                    ethFilter.addSingleTopic(EventEncoder.encode(event));
                    finalCurrentWeb3j.ethLogObservable(ethFilter).subscribe(log -> {
                        try {
                            EarnestMoneyRecord.EarnestMoneyRecordParam earnestMoneyRecordParam = getEarnestMoneyRecordParam(event, log, finalCurrentWeb3j);
                            EventValues eventValues = Contract.staticExtractEventParameters(event, log);
                            earnestMoneyRecordParam.setReferer(eventValues.getNonIndexedValues().get(2).getValue().toString());
                            earnestMoneyRecordParam.setChainType(finalChinaType);
                            rabbitmqProvider.sellerPayEarnestMoney(earnestMoneyRecordParam);
                            redisUtils.set(finalRedisKey, log.getBlockNumber().intValue() + 1);
                        } catch (Exception e) {
                            EventListener.log.error("卖家支付保证金事件异常", e);
                        }
                    });
                }
            });
            sellerPayEarnestMoney.start();
        }

        Thread.sleep(waitTime);

        for (Config config : contractAddrList) {
            String redisKey = "";
            int chinaType = 0;
            if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = hbWeb3j;
                currentStartBlockNumber = hbStartBlockNumber;
                redisKey = "hbBaseFiEarnestMoneyComplete";
                chinaType = BaseConstants.ChainType.HB.getCode();
            } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = baWeb3j;
                currentStartBlockNumber = baStartBlockNumber;
                redisKey = "baBaseFiEarnestMoneyComplete";
                chinaType = BaseConstants.ChainType.BA.getCode();
            } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = ethWeb3j;
                currentStartBlockNumber = ethStartBlockNumber;
                redisKey = "ethBaseFiEarnestMoneyComplete";
                chinaType = BaseConstants.ChainType.ETH.getCode();
            }

            Web3j finalCurrentWeb3j = currentWeb3j;
            String finalCurrentStartBlockNumber = currentStartBlockNumber;
            String finalRedisKey = redisKey;
            int finalChinaType = chinaType;
            /**
             *  合约双方保证金全部完成事件
             */
            Thread earnestMoneyComplete = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object baseFiOrderCreate = redisUtils.get(finalRedisKey);
                    String start = "";
                    if (baseFiOrderCreate == null) {
                        start = finalCurrentStartBlockNumber;
                    } else {
                        start = baseFiOrderCreate.toString();
                    }
                    log.warn("监听合约双方保证金全部完成事件 " + finalRedisKey + ":{}", start);
                    Event event = new Event("E_Entanglement",
                            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }));
                    final BigInteger[] num = new BigInteger[1];
                    num[0] = new BigInteger(start);
                    EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(num[0]),
                            DefaultBlockParameterName.LATEST, config.getIndexValue());
                    ethFilter.addSingleTopic(EventEncoder.encode(event));
                    finalCurrentWeb3j.ethLogObservable(ethFilter).subscribe(log -> {
                        try {
                            EventValues eventValues = Contract.staticExtractEventParameters(event, log);
                            List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
                            Order order = new Order();
                            order.setOrderNum(nonIndexedValues.get(0).getValue().toString());
                            order.setBuyerTokenAddr(nonIndexedValues.get(3).getValue().toString());
                            order.setSellerTokenAddr(nonIndexedValues.get(2).getValue().toString());
                            order.setChainType(finalChinaType);
                            rabbitmqProvider.earnestMoneyComplete(order);
                            redisUtils.set(finalRedisKey, log.getBlockNumber().intValue() + 1);
                        } catch (Exception e) {
                            EventListener.log.error("卖家支付保证金事件异常", e);
                        }
                    });
                }
            });
            earnestMoneyComplete.start();
        }

        Thread.sleep(waitTime);

        for (Config config : contractAddrList) {
            String redisKey = "";
            int chinaType = 0;
            if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = hbWeb3j;
                currentStartBlockNumber = hbStartBlockNumber;
                redisKey = "hbBaseFiBuyerPayDeposit";
                chinaType = BaseConstants.ChainType.HB.getCode();
            } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = baWeb3j;
                currentStartBlockNumber = baStartBlockNumber;
                redisKey = "baBaseFiBuyerPayDeposit";
                chinaType = BaseConstants.ChainType.BA.getCode();
            } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = ethWeb3j;
                currentStartBlockNumber = ethStartBlockNumber;
                redisKey = "ethBaseFiBuyerPayDeposit";
                chinaType = BaseConstants.ChainType.ETH.getCode();
            }

            Web3j finalCurrentWeb3j = currentWeb3j;
            String finalCurrentStartBlockNumber = currentStartBlockNumber;
            String finalRedisKey = redisKey;
            int finalChinaType = chinaType;
            /**
             *  买家支付代币事件
             */
            Thread buyerPayDeposit = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object baseFiOrderCreate = redisUtils.get(finalRedisKey);
                    String start = "";
                    if (baseFiOrderCreate == null) {
                        start = finalCurrentStartBlockNumber;
                    } else {
                        start = baseFiOrderCreate.toString();
                    }
                    log.warn("监听买家支付代币事件 " + finalRedisKey + ":{}", start);
                    Event event = new Event("E_Deposit_For_Tail",
                            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Uint256>() {
                            }, new TypeReference<Uint256>() {
                            }));
                    final BigInteger[] num = new BigInteger[1];
                    num[0] = new BigInteger(start);
                    EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(num[0]),
                            DefaultBlockParameterName.LATEST, config.getIndexValue());
                    ethFilter.addSingleTopic(EventEncoder.encode(event));
                    finalCurrentWeb3j.ethLogObservable(ethFilter).subscribe(log -> {
                        try {
                            DepositRecord.DepositRecordParam depositRecordParam = getDepositRecordParam(event, log, finalCurrentWeb3j);
                            depositRecordParam.setChainType(finalChinaType);
                            rabbitmqProvider.buyerPayDeposit(depositRecordParam);
                            redisUtils.set(finalRedisKey, log.getBlockNumber().intValue() + 1);
                        } catch (Exception e) {
                            EventListener.log.error("买家支付代币事件异常", e);
                        }
                    });

                }
            });
            buyerPayDeposit.start();
        }

        Thread.sleep(waitTime);

        for (Config config : contractAddrList) {
            String redisKey = "";
            int chinaType = 0;
            if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = hbWeb3j;
                currentStartBlockNumber = hbStartBlockNumber;
                redisKey = "hbBaseFiSellerPayDeposit";
                chinaType = BaseConstants.ChainType.HB.getCode();
            } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = baWeb3j;
                currentStartBlockNumber = baStartBlockNumber;
                redisKey = "baBaseFiSellerPayDeposit";
                chinaType = BaseConstants.ChainType.BA.getCode();
            } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = ethWeb3j;
                currentStartBlockNumber = ethStartBlockNumber;
                redisKey = "ethBaseFiSellerPayDeposit";
                chinaType = BaseConstants.ChainType.ETH.getCode();
            }

            Web3j finalCurrentWeb3j = currentWeb3j;
            String finalCurrentStartBlockNumber = currentStartBlockNumber;
            String finalRedisKey = redisKey;
            int finalChinaType = chinaType;
            /**
             *  卖家支付代币事件
             */
            Thread sellerPayDeposit = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object baseFiOrderCreate = redisUtils.get(finalRedisKey);
                    String start = "";
                    if (baseFiOrderCreate == null) {
                        start = finalCurrentStartBlockNumber;
                    } else {
                        start = baseFiOrderCreate.toString();
                    }
                    log.warn("监听卖家支付代币事件 " + finalRedisKey + ":{}", start);
                    Event event = new Event("E_Deposit_For_Head",
                            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Uint256>() {
                            }, new TypeReference<Uint256>() {
                            }));
                    final BigInteger[] num = new BigInteger[1];
                    num[0] = new BigInteger(start);
                    EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(num[0]),
                            DefaultBlockParameterName.LATEST, config.getIndexValue());
                    ethFilter.addSingleTopic(EventEncoder.encode(event));
                    finalCurrentWeb3j.ethLogObservable(ethFilter).subscribe(log -> {
                        try {
                            DepositRecord.DepositRecordParam depositRecordParam = getDepositRecordParam(event, log, finalCurrentWeb3j);
                            depositRecordParam.setChainType(finalChinaType);
                            rabbitmqProvider.sellerPayDeposit(depositRecordParam);
                            redisUtils.set(finalRedisKey, log.getBlockNumber().intValue() + 1);
                        } catch (Exception e) {
                            EventListener.log.error("卖家支付代币事件异常", e);
                        }
                    });
                }
            });
            sellerPayDeposit.start();
        }

        Thread.sleep(waitTime);
        for (Config config : contractAddrList) {
            String redisKey = "";
            int chinaType = 0;
            if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = hbWeb3j;
                currentStartBlockNumber = hbStartBlockNumber;
                redisKey = "hbBaseFiBuyerWithdraw";
                chinaType = BaseConstants.ChainType.HB.getCode();
            } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = baWeb3j;
                currentStartBlockNumber = baStartBlockNumber;
                redisKey = "baBaseFiBuyerWithdraw";
                chinaType = BaseConstants.ChainType.BA.getCode();
            } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = ethWeb3j;
                currentStartBlockNumber = ethStartBlockNumber;
                redisKey = "ethBaseFiBuyerWithdraw";
                chinaType = BaseConstants.ChainType.ETH.getCode();
            }

            Web3j finalCurrentWeb3j = currentWeb3j;
            String finalCurrentStartBlockNumber = currentStartBlockNumber;
            String finalRedisKey = redisKey;
            int finalChinaType = chinaType;
            /**
             *  买家领取应得代币事件
             */
            Thread buyerWithdraw = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object baseFiOrderCreate = redisUtils.get(finalRedisKey);
                    String start = "";
                    if (baseFiOrderCreate == null) {
                        start = finalCurrentStartBlockNumber;
                    } else {
                        start = baseFiOrderCreate.toString();
                    }
                    log.warn("监听买家领取应得代币事件 " + finalRedisKey + ":{}", start);
                    Event event = new Event("E_Withdraw_Tail",
                            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Uint256>() {
                            }));
                    final BigInteger[] num = new BigInteger[1];
                    num[0] = new BigInteger(start);
                    EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(num[0]),
                            DefaultBlockParameterName.LATEST, config.getIndexValue());
                    ethFilter.addSingleTopic(EventEncoder.encode(event));
                    finalCurrentWeb3j.ethLogObservable(ethFilter).subscribe(log -> {
                        try {
                            EventValues eventValues = Contract.staticExtractEventParameters(event, log);
                            List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
                            Order order = new Order();
                            order.setOrderNum(nonIndexedValues.get(0).getValue().toString());
                            order.setBuyerWithdrawStatus(Integer.parseInt(nonIndexedValues.get(2).getValue().toString()));
                            order.setChainType(finalChinaType);
                            rabbitmqProvider.buyerWithdraw(order);
                            redisUtils.set(finalRedisKey, log.getBlockNumber().intValue() + 1);
                        } catch (Exception e) {
                            EventListener.log.error("买家领取应得代币事件异常", e);
                        }
                    });
                }
            });
            buyerWithdraw.start();
        }

        Thread.sleep(waitTime);
        for (Config config : contractAddrList) {
            String redisKey = "";
            int chinaType = 0;
            if ("Hb_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = hbWeb3j;
                currentStartBlockNumber = hbStartBlockNumber;
                redisKey = "hbBaseFiSellerWithdraw";
                chinaType = BaseConstants.ChainType.HB.getCode();
            } else if ("Ba_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = baWeb3j;
                currentStartBlockNumber = baStartBlockNumber;
                redisKey = "baBaseFiSellerWithdraw";
                chinaType = BaseConstants.ChainType.BA.getCode();
            } else if ("Eth_Swap_Contract_Address".equals(config.getIndexName())) {
                currentWeb3j = ethWeb3j;
                currentStartBlockNumber = ethStartBlockNumber;
                redisKey = "ethBaseFiSellerWithdraw";
                chinaType = BaseConstants.ChainType.ETH.getCode();
            }

            Web3j finalCurrentWeb3j = currentWeb3j;
            String finalCurrentStartBlockNumber = currentStartBlockNumber;
            String finalRedisKey = redisKey;
            int finalChinaType = chinaType;
            /**
             *  卖家领取应得代币事件
             */
            Thread sellerWithdraw = new Thread(new Runnable() {
                @Override
                public void run() {
                    Object baseFiOrderCreate = redisUtils.get(finalRedisKey);
                    String start = "";
                    if (baseFiOrderCreate == null) {
                        start = finalCurrentStartBlockNumber;
                    } else {
                        start = baseFiOrderCreate.toString();
                    }
                    log.warn("监听卖家领取应得代币事件 " + finalRedisKey + ":{}", start);
                    Event event = new Event("E_Withdraw_Head",
                            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                            }, new TypeReference<Address>() {
                            }, new TypeReference<Uint256>() {
                            }));
                    final BigInteger[] num = new BigInteger[1];
                    num[0] = new BigInteger(start);
                    EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(num[0]),
                            DefaultBlockParameterName.LATEST, config.getIndexValue());
                    ethFilter.addSingleTopic(EventEncoder.encode(event));
                    finalCurrentWeb3j.ethLogObservable(ethFilter).subscribe(log -> {
                        try {
                            EventValues eventValues = Contract.staticExtractEventParameters(event, log);
                            List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
                            Order order = new Order();
                            order.setOrderNum(nonIndexedValues.get(0).getValue().toString());
                            order.setSellerWithdrawStatus(Integer.parseInt(nonIndexedValues.get(2).getValue().toString()));
                            order.setChainType(finalChinaType);
                            rabbitmqProvider.sellerWithdraw(order);
                            redisUtils.set(finalRedisKey, log.getBlockNumber().intValue() + 1);
                        } catch (Exception e) {
                            EventListener.log.error("卖家领取应得代币事件异常", e);
                        }
                    });
                }
            });
            sellerWithdraw.start();
        }
        log.warn("监听结束");
    }

    private DepositRecord.DepositRecordParam getDepositRecordParam(Event event, Log log, Web3j web) throws java.io.IOException {
        EthBlock ethBlock = web.ethGetBlockByNumber(DefaultBlockParameter.valueOf(log.getBlockNumber()), true).send();
        Date eventTime = DateUtils.eventTimeStamp(ethBlock.getResult().getTimestamp().toString());
        EventValues eventValues = Contract.staticExtractEventParameters(event, log);
        List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
        DepositRecord.DepositRecordParam depositRecordParam = new DepositRecord.DepositRecordParam();
        depositRecordParam.setOrderNum(nonIndexedValues.get(0).getValue().toString());
        depositRecordParam.setUserAddr(nonIndexedValues.get(1).getValue().toString());
        depositRecordParam.setAmount(nonIndexedValues.get(2).getValue().toString());
        depositRecordParam.setDepositedAmount(nonIndexedValues.get(3).getValue().toString());
        depositRecordParam.setEventTime(eventTime);
        depositRecordParam.setBlockNumber(log.getBlockNumber().intValue());
        depositRecordParam.setTransactionHash(log.getTransactionHash());
        return depositRecordParam;
    }

    private EarnestMoneyRecord.EarnestMoneyRecordParam getEarnestMoneyRecordParam(Event event, Log log, Web3j web) throws java.io.IOException {
        EthBlock ethBlock = web.ethGetBlockByNumber(DefaultBlockParameter.valueOf(log.getBlockNumber()), true).send();
        Date eventTime = DateUtils.eventTimeStamp(ethBlock.getResult().getTimestamp().toString());
        EventValues eventValues = Contract.staticExtractEventParameters(event, log);
        List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
        EarnestMoneyRecord.EarnestMoneyRecordParam earnestMoneyRecordParam = new EarnestMoneyRecord.EarnestMoneyRecordParam();
        earnestMoneyRecordParam.setOrderNum(nonIndexedValues.get(0).getValue().toString());
        earnestMoneyRecordParam.setUserAddr(nonIndexedValues.get(1).getValue().toString());
        earnestMoneyRecordParam.setEventTime(eventTime);
        earnestMoneyRecordParam.setBlockNumber(log.getBlockNumber().intValue());
        earnestMoneyRecordParam.setTransactionHash(log.getTransactionHash());
        return earnestMoneyRecordParam;
    }
}
