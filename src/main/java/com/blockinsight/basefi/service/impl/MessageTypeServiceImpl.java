package com.blockinsight.basefi.service.impl;

import com.blockinsight.basefi.entity.MessageType;
import com.blockinsight.basefi.mapper.MessageTypeMapper;
import com.blockinsight.basefi.service.IMessageTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 消息类型 服务实现类
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */
@Service
public class MessageTypeServiceImpl extends ServiceImpl<MessageTypeMapper, MessageType> implements IMessageTypeService {

}
