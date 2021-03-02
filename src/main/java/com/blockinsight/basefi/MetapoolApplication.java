package com.blockinsight.basefi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@MapperScan(basePackages = {"com.blockinsight.basefi.mapper"})
@SpringBootApplication(scanBasePackages = "com.blockinsight.basefi.*")
public class MetapoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetapoolApplication.class, args);
	}

}
