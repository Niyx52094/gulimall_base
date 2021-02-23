package com.atguigu.gulimall.product;

//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClient;
//import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallProductApplicationTests {
//	@Resource
//	//OSSClient ossClient;

	@Autowired
	BrandService brandService;

	@Autowired
	CategoryService categoryService;

	@Test
	public void testFindPath(){
		Long[] fullPath = categoryService.findFullPath(225L);
		log.info("完整路径：{}" , Arrays.asList(fullPath));
	}
//	@Test
//	public void testUpload() throws FileNotFoundException {
////		// Endpoint以杭州为例，其它Region请按实际情况填写。
////		String endpoint = "oss-ap-southeast-1.aliyuncs.com";
////		// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
////		String accessKeyId = "LTAI4FyeQjU9V49uTXGoNFGk";
////		String accessKeySecret = "rMwpUzffKsnrBIRmgcu5DaIWfkuAjJ";
////
////		// 创建OSSClient实例。
////		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//			// 上传文件流。
//		InputStream inputStream = new FileInputStream("D:\\CS\\JAVA\\ideaProject\\谷粒商城\\gulimall-learning-master-339star\\docs\\images\\1587637858665.png");
//		ossClient.putObject("gulimall-nyx", "1587637858665.png", inputStream);
//
//		// 关闭OSSClient。
//		ossClient.shutdown();
//		System.out.println("上传完成。。。");
//	}


	@Test
	void contextLoads() {

//		BrandEntity brandEntity = new BrandEntity();
//		brandEntity.setBrandId(1L);
//		brandEntity.setDescript("华为");
//
////		brandEntity.setName("huawei");
////		brandService.save(brandEntity);
////		System.out.println("保存成功");
//		brandService.updateById(brandEntity);

		List<BrandEntity> list= brandService.list(new QueryWrapper<BrandEntity>()
				.eq("brand_id", 1L));
		list.forEach((item)->{
			System.out.println(item);
		});
	}

}
