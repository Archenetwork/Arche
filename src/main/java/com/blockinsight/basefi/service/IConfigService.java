package com.blockinsight.basefi.service;

import com.blockinsight.basefi.entity.Config;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 配置表 服务类
 * </p>
 *
 * @author Janin
 * @since 2021-01-09
 */
public interface IConfigService extends IService<Config> {

    String getConfig(String indexName);
}
