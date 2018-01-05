package com.mmerhav.weiboidextractor.selenium.exec;

import com.mmerhav.weiboidextractor.selenium.core.model.Card;
import com.mmerhav.weiboidextractor.selenium.core.page.FanListPage;
import com.mmerhav.weiboidextractor.selenium.core.page.FanPage;
import com.mmerhav.weiboidextractor.selenium.core.repository.Repository;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNumeric;


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

    @Autowired
    private ApplicationContext context;

    private int attempts = 0;

    private Set<Card> alreadyAddedCards;

    private Set<String> alreadyAddedNickNames;

    public void run() {

        alreadyAddedCards = repository.getCards();
        alreadyAddedNickNames = alreadyAddedCards.stream().map(Card::getName).collect(Collectors.toSet());

        boolean shouldClickFansTab = true;
        while (alreadyAddedCards.size() < numIdsToAdd) {
            try {
                if (fanListPage.isOnPage(driver)) {
                    if (shouldClickFansTab) {
                        fanListPage.getFansTab(driver).click();
                        shouldClickFansTab = false;
                    }
                    WebElement element = scanForNextCard(alreadyAddedNickNames);

                    if (element != null) {
                        String nickName = fanListPage.getNickName(element);
                        element.click();

                        if (fanPage.isOnPage(driver)) {
                            String id = fanPage.getWeiboId(driver);
                            if (isNumeric(id)) {
                                writeCard(id, nickName);
                                log.info("Processed {} ids out of {}", alreadyAddedCards.size(), numIdsToAdd);
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
            } catch (Exception e) {
                e.printStackTrace();
                driver.close();
                driver = context.getBean(WebDriver.class);
            }
        }

    }

    private void writeCard(String id, String nickName) {
        Card addedCard = new Card(id, nickName);
        try {
            repository.writeCardCovered(addedCard);
        } catch (DuplicateKeyException e) {
            repository.updateCardName(addedCard);
        }
        alreadyAddedNickNames.add(nickName);
        alreadyAddedCards.add(addedCard);
    }

    private WebElement scanForNextCard(Set<String> alreadyAddedNickNames) {
        WebElement element;
        attempts = 0;

        do {
            log.info("scan for card attempt {} of {}", attempts + 1, maxUserScanAttempts);
            fanListPage.scrollListDown(driver);
            element = findNextCard(alreadyAddedNickNames, fanListPage.getCardsList(driver));

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
