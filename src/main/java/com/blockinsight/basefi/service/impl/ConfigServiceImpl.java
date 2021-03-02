package com.blockinsight.basefi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blockinsight.basefi.entity.Config;
import com.blockinsight.basefi.mapper.ConfigMapper;
import com.blockinsight.basefi.service.IConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 配置表 服务实现类
 * </p>
 *
 * @author Janin
 * @since 2021-01-09
 */
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements IConfigService {

    @Override
    public String getConfig(String indexName) {
        return baseMapper.selectOne(new LambdaUpdateWrapper<Config>().eq(Config::getIndexName, indexName)).getIndexValue();
    }
}
