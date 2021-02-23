package com.atguigu.gulimall.ware;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableDiscoveryClient
@MapperScan("com.atguigu.gulimall.ware.dao")
@SpringBootTest
class GulimallWareApplicationTests {

	@Test
	void contextLoads() {
	}

}
