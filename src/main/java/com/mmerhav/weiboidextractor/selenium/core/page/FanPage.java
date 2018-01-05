package com.mmerhav.weiboidextractor.selenium.core.page;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FanPage extends BasePage {

    @Override
    public boolean isOnPage(WebDriver driver) {
        boolean isOnPage = isBtnVisible(driver) || isEmptyFailedLabelVisible(driver);
        log.info("{} isOnPage() is {}", FanPage.class.getSimpleName(), isOnPage);
        return isOnPage;
    }

    public String getWeiboId(WebDriver driver) {
        return driver.getCurrentUrl().replaceAll("^\\D*(\\d+).*", "$1");
    }

    private boolean isEmptyFailedLabelVisible(WebDriver driver) {
        try {
            new WebDriverWait(driver, waitForElemTimeout).
                    until(ExpectedConditions.elementToBeClickable(By.className("empty_failed")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean isBtnVisible(WebDriver driver) {
        try {
            log.info("Checking if fan page is loaded");
            new WebDriverWait(driver, waitForElemTimeout).
                    until(ExpectedConditions.elementToBeClickable(By.className("btn")));
            return true;
        } catch (TimeoutException ignored) {
        }
        return false;
    }
}
