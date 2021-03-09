//package com.blockinsight.basefi.common.config;
//
//import okhttp3.OkHttpClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Scope;
//import org.web3j.protocol.Web3j;
//import org.web3j.protocol.http.HttpService;
//
//import java.util.concurrent.TimeUnit;
//
////@Configuration
//public class EthConfig {
//
//    @Value("${web3j.client-address}")
//    private String url;
//
////    @Bean
////    @Scope("prototype")
//    public Web3j web3j() {
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.connectTimeout(30*1000, TimeUnit.MILLISECONDS);
//        builder.writeTimeout(30*1000, TimeUnit.MILLISECONDS);
//        builder.readTimeout(30*1000, TimeUnit.MILLISECONDS);
//        OkHttpClient httpClient = builder.build();
//        return Web3j.build(new HttpService(url,httpClient,false));
//    }
//}
