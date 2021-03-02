package com.blockinsight.basefi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blockinsight.basefi.common.constant.BaseConstants;
import com.blockinsight.basefi.common.parentclass.BaseEntity;
import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.Message;
import com.blockinsight.basefi.mapper.MessageMapper;
import com.blockinsight.basefi.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 消息记录表 服务实现类
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {
    @Override
    public R messageS(Integer chainType, String userNum, String messageTypeName, Integer pageNumbers, Integer pageSizes) {
        log.warn("查询用户消息列表参数 chainType:{} userNum:{} messageTypeName:{} pageNumbers:{} pageSizes:{}",
                chainType, userNum, messageTypeName, pageNumbers, pageSizes);
        if ("全部消息".equals(messageTypeName)) {
            messageTypeName = "";
        }
        IPage<Message> iPage = new Page<>(pageNumbers, pageSizes);
        LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (chainType != BaseConstants.ChainType.ALL.getCode()) {
            messageLambdaQueryWrapper.eq(Message::getChainType, chainType);
        }
        messageLambdaQueryWrapper.eq(Message::getUserAddr, userNum)
                .eq(StringUtils.isNotBlank(messageTypeName), Message::getMessageTypeName, messageTypeName)
                .orderByAsc(Message::getIsRead).orderByDesc(BaseEntity::getCreateTime);
        IPage<Message> page = this.page(iPage, messageLambdaQueryWrapper);
        if (!page.getRecords().isEmpty()) {
            for (Message record : page.getRecords()) {
                if (record.getIsRead() == 0) {
                    record.setIsRead(1);
                    this.updateById(record);
                    record.setIsRead(0);
                }
            }
        }
        return R.ok().put("data", page);
    }

    @Override
    public R messageReadCount(Integer chainType, String userNum) {
        @Data
        class ResultDto{
            /**
             *          ALL(1, "全部消息"),
             *         CREATE_ORDER(2, "订单消息"),
             *         EARNEST_MONEY(3, "保证金消息"),
             *         DELIVERY(4, "交割消息"),
             *         TRANSACTION(5, "违约消息"),
             *         RECEIVER(6, "收款消息");
             */
            Integer createOrderCount;
            Integer earnestMoneyCount;
            Integer deliveryCount;
            Integer transactionCount;
            Integer receiverCount;
            Integer allCount;
        }
        log.warn("查询消息红点数量参数 userNum:{}", userNum);
        LambdaUpdateWrapper<Message> createOrder = new LambdaUpdateWrapper<Message>().eq(Message::getUserAddr, userNum).eq(Message::getIsRead, 0).eq(Message::getMessageTypeName, BaseConstants.MessageType.CREATE_ORDER.getMes());
        LambdaUpdateWrapper<Message> earnestMoney = new LambdaUpdateWrapper<Message>().eq(Message::getUserAddr, userNum).eq(Message::getIsRead, 0).eq(Message::getMessageTypeName, BaseConstants.MessageType.EARNEST_MONEY.getMes());
        LambdaUpdateWrapper<Message> delivery = new LambdaUpdateWrapper<Message>().eq(Message::getUserAddr, userNum).eq(Message::getIsRead, 0).eq(Message::getMessageTypeName, BaseConstants.MessageType.DELIVERY.getMes());
        LambdaUpdateWrapper<Message> transaction = new LambdaUpdateWrapper<Message>().eq(Message::getUserAddr, userNum).eq(Message::getIsRead, 0).eq(Message::getMessageTypeName, BaseConstants.MessageType.TRANSACTION.getMes());
        LambdaUpdateWrapper<Message> receiver = new LambdaUpdateWrapper<Message>().eq(Message::getUserAddr, userNum).eq(Message::getIsRead, 0).eq(Message::getMessageTypeName, BaseConstants.MessageType.RECEIVER.getMes());
        if (chainType != BaseConstants.ChainType.ALL.getCode()) {
            createOrder.eq(Message::getChainType, chainType);
            earnestMoney.eq(Message::getChainType, chainType);
            delivery.eq(Message::getChainType, chainType);
            transaction.eq(Message::getChainType, chainType);
            receiver.eq(Message::getChainType, chainType);
        }
        int createOrderCount = this.count(createOrder);
        int earnestMoneyCount = this.count(earnestMoney);
        int deliveryCount = this.count(delivery);
        int transactionCount = this.count(transaction);
        int receiverCount = this.count(receiver);
        ResultDto resultDto = new ResultDto();
        resultDto.setCreateOrderCount(createOrderCount);
        resultDto.setDeliveryCount(deliveryCount);
        resultDto.setEarnestMoneyCount(earnestMoneyCount);
        resultDto.setReceiverCount(receiverCount);
        resultDto.setTransactionCount(transactionCount);
        resultDto.setAllCount(createOrderCount + earnestMoneyCount + deliveryCount + transactionCount + receiverCount);
        return R.ok().put("data", resultDto);
    }
}
