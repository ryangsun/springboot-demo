package com.example.demo;

import com.example.demo.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
@Slf4j
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		System.out.println("Bumao started.");
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

	@Autowired
	private RedisUtils redisUtils;
	@RequestMapping(value = "/hello/{id}")
	public String helloRedis(@PathVariable(value = "id") String id){
		//查询缓存中是否存在
		boolean hasKey = redisUtils.exists(id);
		String str = "abc";
		if(hasKey){
			//获取缓存
			Object object =  redisUtils.get(id);
			log.info("从缓存获取的数据"+ object);
			str = object.toString();
		}else{
			//从数据库中获取信息
			log.info("从数据库中获取数据");
//			str = testService.test();
			//数据插入缓存（set中的参数含义：key值，user对象，缓存存在时间10（long类型），时间单位）
			redisUtils.set(id,str,10L, TimeUnit.MINUTES);
			log.info("数据插入缓存" + str);
		}
		return str;
	}
}
