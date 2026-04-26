package com.lzh.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.lzh.handler.LongArrayTypeHandler;
import com.lzh.vo.TimeBitmap;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基础设施层配置：统一管理持久化逻辑
 */
@Configuration
@MapperScan("com.lzh.mapper")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 1. 开启乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 2. 开启分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 【修正点】使用 MyBatis-Plus 专门为 SpringBoot 3 提供的 Customizer
     * 导入包路径应为: com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer
     */
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> {
            // 告诉 MyBatis：以后看到 TimeBitmap 类型，直接用这个 Handler 处理
            configuration.getTypeHandlerRegistry().register(TimeBitmap.class, LongArrayTypeHandler.class);
        };
    }
}