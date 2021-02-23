package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1.整合MyBatis-Plus依赖
 * 			1）.导入依赖
 *   		<dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.4.1</version>
 *         </dependency>
 *         2）配置：
 *         		1、配置数据源
 *         			a.导入数据库驱动/导入依赖
 *         			b.在application.yml中配置数据源相关信息
 *         		2.配置MyBatis-plus;
 *					1）.使用mapperscan
 *					2）。告诉Mybaitis=plus,sql映射映射文件位置
 */
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@MapperScan("com.atguigu.gulimall.product.dao")
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallProductApplication.class, args);
	}

}
