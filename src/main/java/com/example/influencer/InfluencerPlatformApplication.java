package com.example.influencer;

import com.example.influencer.demo.DemoUser;
import com.example.influencer.demo.DemoUserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InfluencerPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(InfluencerPlatformApplication.class, args);
	}

	@Bean
	public org.springframework.boot.CommandLineRunner runner(DemoUserRepository repo){
		return args -> repo.save(DemoUser.builder().email("check@db.test").build());
	}

}
