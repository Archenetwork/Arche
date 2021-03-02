package com.blockinsight.basefi.common.config.mybatisplus;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan("com.blockinsight.basefi.mapper.*") //扫描DAO接口
public class MybatisPlusConfig {
    /**
     * 分页插件，自动识别数据库类型
     * @return PaginationInterceptor
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * 公共字段 create_time update_time 自动创建与更新
     * @return
     */
    @Bean
    public SqlInterceptor sqlInterceptor(){
        return new SqlInterceptor();
    }
}