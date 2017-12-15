package com.mmerhav.weiboidextractor.selenium.exec;

import ch.qos.logback.core.util.FileUtil;
import com.google.gson.Gson;
import com.mmerhav.weiboidextractor.selenium.core.model.Card;
import com.mmerhav.weiboidextractor.selenium.core.page.FanListPage;
import com.mmerhav.weiboidextractor.selenium.core.page.FanPage;
import com.mmerhav.weiboidextractor.selenium.core.repository.Repository;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
public class WeiboIdExtractorRunner {

    @Value("${num.ids.to.add}")
    private int numIdsToAdd;

    @Value("${max.user.scan.attempts}")
    private int maxUserScanAttempts;

    @Value("${url.to.scrape}")
    private String url;

    @Autowired
    private FanListPage fanListPage;

    @Autowired
    private WebDriver driver;

    @Autowired
    private FanPage fanPage;

    @Autowired
    private Repository repository;

    private int attempts = 0;

    public void run() throws IOException {

        Set<String> fanIds = new HashSet<>();
        Set<Card> alreadyAddedCards = repository.loadAlreadyAddedCards();
        Set<String> alreadyAddedNickNames = alreadyAddedCards.stream().map(Card::getName).collect(Collectors.toSet());


        try {
            boolean shouldClickFansTab = true;
            while (fanIds.size() < numIdsToAdd) {
                try {

                    if (fanListPage.isOnPage()) {
                        if(shouldClickFansTab) {
                            fanListPage.getFansTab().click();
                            shouldClickFansTab = false;
                        }
                        WebElement element = scanForNextCard(alreadyAddedNickNames);

                        if (element != null) {
                            String nickName = fanListPage.getNickName(element);
                            element.click();

                            if (fanPage.isOnPage()) {
                                String id = fanPage.getWeiboId();
                                if (id.length() == 10) {
                                    writeId(id, fanIds);
                                    writeCard(id, nickName, alreadyAddedNickNames);
                                    log.info("Processed {} ids out of {}", fanIds.size(), numIdsToAdd);
                                }
                                driver.navigate().back();
                            } else {
                                driver.get(url);
                                shouldClickFansTab = true;
                            }
                        } else {
                            if (attempts >= maxUserScanAttempts) {
                                driver.navigate().refresh();
                                shouldClickFansTab = true;
                            }
                        }
                    } else {
                        driver.get(url);
                        shouldClickFansTab = true;
                    }
                } catch (WebDriverException e) {
                    driver.get(url);
                }
            }
        } finally {
            driver.close();
        }
    }

    private void writeCard(String id, String nickName, Set<String> alreadyAddedNickNames) throws IOException {
        Card addedCard = new Card(id, nickName);
        alreadyAddedNickNames.add(nickName);
        repository.writeCardCovered(addedCard);
    }

    private void writeId(String id, Set<String> fanIds) throws IOException {
        repository.writeResult(id);
        fanIds.add(id);
    }

    private WebElement scanForNextCard(Set<String> alreadyAddedNickNames) {
        WebElement element;
        attempts = 0;

        do {
            log.info("scan for card attempt {} of {}", attempts + 1, maxUserScanAttempts);
            fanListPage.scrollListDown();
            element = findNextCard(alreadyAddedNickNames, fanListPage.getCardsList());

            attempts++;
        } while (element == null && attempts < maxUserScanAttempts);
        return element;
    }

    private WebElement findNextCard(Set<String> alreadyAddedNickNames, List<WebElement> elements) {
        log.info("Finding next card...");
        for (int i = 2; i < elements.size(); i++) {
            WebElement element = elements.get(i);
            String nickName = fanListPage.getNickName(element);
            if (nickName != null && !alreadyAddedNickNames.contains(nickName)) {
                log.info("Next card found:{}", element);
                return element;
            }
        }
        log.warn("Next card not found");
        return null;
    }
}
