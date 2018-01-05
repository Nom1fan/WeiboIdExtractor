package com.mmerhav.weiboidextractor.selenium.core.page;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FanListPage extends BasePage {

    @Value("${url.to.scrape}")
    private String url;

    public WebElement getFansTab(WebDriver driver) {
        String fansTabXPath = "//*[@id=\"app\"]/div[1]/div[1]/div[1]/div[2]/div[1]/div/div/div/ul/li[3]/span";
        new WebDriverWait(driver, waitForElemTimeout).
                until(ExpectedConditions.numberOfElementsToBe(By.xpath(fansTabXPath), 1));
        return driver.findElement(By.xpath(fansTabXPath));
    }

    public List<WebElement> getCardsList(WebDriver driver) {
        new WebDriverWait(driver, waitForElemTimeout).
                until(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("card-wrap"), 10));
        return driver.findElements(By.className("card-wrap"));
    }

    public void scrollListDown(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,250)", "");
    }

    public String getNickName(WebElement element) {
        List<WebElement> elements = element.findElements(By.className("m-text-cut"));
        if (elements.size() > 0) {
            return elements.get(0).getText();
        }
        return null;
    }

    @Override
    public boolean isOnPage(WebDriver driver) {
        boolean isOnPage = driver.getCurrentUrl().equals(url);
        log.info("{} isOnPage() is {}", FanListPage.class.getSimpleName(), isOnPage);
        return isOnPage;
    }
}
