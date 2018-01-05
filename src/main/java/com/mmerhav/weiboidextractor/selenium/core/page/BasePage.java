package com.mmerhav.weiboidextractor.selenium.core.page;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public abstract class BasePage implements Page {

    @Value("${wait.for.elem.timeout}")
    protected int waitForElemTimeout;

    public abstract boolean isOnPage(WebDriver driver);
}
