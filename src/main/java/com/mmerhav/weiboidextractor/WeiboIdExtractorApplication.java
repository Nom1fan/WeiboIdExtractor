package com.mmerhav.weiboidextractor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mmerhav.weiboidextractor.selenium.exec.WeiboIdExtractorRunner;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

@SpringBootApplication
@ComponentScan(basePackages = "com.mmerhav")
@Slf4j
public class WeiboIdExtractorApplication {

    @Bean
    public Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    @Value("${url.to.scrape}")
    private String url;

    @Bean
    public WebDriver webDriver() {
        WebDriver driver = new ChromeDriver();
        driver.get(url);
        return driver;
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext context = SpringApplication.run(WeiboIdExtractorApplication.class, args);
        WeiboIdExtractorRunner runner = context.getBean(WeiboIdExtractorRunner.class);
//        WeiboIdCrawlJaxExtractorRunner runner = context.getBean(WeiboIdCrawlJaxExtractorRunner.class);
        runner.run();
    }
}
