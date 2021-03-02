package com.blockinsight.basefi.service;

import com.blockinsight.basefi.common.resp.R;
import com.blockinsight.basefi.entity.LockUpRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Janin
 * @since 2021-02-02
 */
public interface ILockUpRecordService extends IService<LockUpRecord> {

    R homePage(Integer chainType) throws Exception;
}
