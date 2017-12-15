package com.mmerhav.weiboidextractor.selenium.core.page;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FanPage extends BasePage {

    @Override
    public boolean isOnPage() {
        boolean isOnPage = isBtnVisible() || isEmptyFailedLabelVisible();
        log.info("{} isOnPage() is {}", FanPage.class.getSimpleName(), isOnPage);
        return isOnPage;
    }

    public String getWeiboId() {
        return driver.getCurrentUrl().replaceAll("^\\D*(\\d+).*", "$1");
    }

    private boolean isEmptyFailedLabelVisible() {
        try {
            new WebDriverWait(driver, waitForElemTimeout).
                    until(ExpectedConditions.elementToBeClickable(By.className("empty_failed")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean isBtnVisible() {
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
