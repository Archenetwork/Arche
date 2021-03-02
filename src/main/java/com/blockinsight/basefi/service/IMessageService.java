package com.blockinsight.basefi.service;

import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 消息记录表 服务类
 * </p>
 *
 * @author Janin
 * @since 2021-01-05
 */

public interface IMessageService extends IService<Message> {
    /**
     * @param userNum    用户公钥
     * @param pageNumber 当前页
     * @param pageSize   每页数量
     * @return 返回消息列表
     */
    R messageS(Integer chainType, String userNum, String messageTypeName, Integer pageNumber, Integer pageSize);

    R messageReadCount(Integer chainType, String userNum);
}
