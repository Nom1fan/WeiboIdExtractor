package com.mmerhav.weiboidextractor.selenium.exec;

import com.google.common.collect.Sets;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mmerhav.weiboidextractor.selenium.core.model.Card;
import com.mmerhav.weiboidextractor.selenium.core.page.FanListPage;
import com.mmerhav.weiboidextractor.selenium.core.page.FanPage;
import com.mmerhav.weiboidextractor.selenium.core.repository.CardsDBRepository;
import com.mmerhav.weiboidextractor.selenium.core.repository.Repository;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
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

        Set<Card> alreadyAddedCards = repository.getCards();
        Set<String> alreadyAddedNickNames = alreadyAddedCards.stream().map(Card::getName).collect(Collectors.toSet());

        try {
            boolean shouldClickFansTab = true;
            while (alreadyAddedCards.size() < numIdsToAdd) {
                try {

                    if (fanListPage.isOnPage()) {
                        if (shouldClickFansTab) {
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
                                    writeCard(id, nickName, alreadyAddedNickNames);
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
                } catch (WebDriverException e) {
                    driver.get(url);
                }
            }
        } finally {
            driver.close();
        }
    }

    private void writeCard(String id, String nickName, Set<String> alreadyAddedNickNames) {
        Card addedCard = new Card(id, nickName);
        try {
            repository.writeCardCovered(addedCard);
        } catch (DuplicateKeyException e) {
            repository.updateCardName(addedCard);
        }
        alreadyAddedNickNames.add(nickName);
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

    public static void main(String[] args) throws PropertyVetoException {

        CardsDBRepository cardsDBRepository = new CardsDBRepository();
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/weibodb");
        dataSource.setUser("root");
        dataSource.setPassword("egg9986");
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        cardsDBRepository.setJdbcTemplate(new JdbcTemplate(dataSource));

        try {
            cardsDBRepository.writeCardsCovered(Sets.newHashSet(new Card("1000199580", null)));
        } catch (DuplicateKeyException e) {
            System.out.println("I'm here");
        }
    }
}
