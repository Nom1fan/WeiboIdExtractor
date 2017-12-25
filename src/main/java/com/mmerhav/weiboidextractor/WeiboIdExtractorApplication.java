package com.mmerhav.weiboidextractor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mchange.v2.c3p0.ComboPooledDataSource;
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
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
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

    @Bean
    public JdbcTemplate jdbcTemplate() throws PropertyVetoException {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/weibodb");
        dataSource.setUser("root");
        dataSource.setPassword("egg9986");
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        return dataSource;
    }

    public static void main(String[] args) throws IOException {
        ApplicationContext context = SpringApplication.run(WeiboIdExtractorApplication.class, args);
        WeiboIdExtractorRunner runner = context.getBean(WeiboIdExtractorRunner.class);
//        WeiboIdCrawlJaxExtractorRunner runner = context.getBean(WeiboIdCrawlJaxExtractorRunner.class);
        runner.run();
    }
}
