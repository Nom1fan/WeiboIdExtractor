package com.mmerhav.weiboidextractor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mmerhav.weiboidextractor.exec.runner.WeiboIdExtractorRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootApplication
public class WeiboIdExtractorApplication {

	@Bean
	public Gson gson() {
		return new GsonBuilder().setPrettyPrinting().create();
	}

	public static void main(String[] args) throws IOException {
		ApplicationContext context = SpringApplication.run(WeiboIdExtractorApplication.class, args);
		WeiboIdExtractorRunner runner = context.getBean(WeiboIdExtractorRunner.class);
		runner.run();
	}
}
