package com.mmerhav.weiboidextractor.selenium.exec;

import ch.qos.logback.core.util.FileUtil;
import com.google.gson.Gson;
import com.mmerhav.weiboidextractor.selenium.core.model.Card;
import com.mmerhav.weiboidextractor.selenium.core.page.FanListPage;
import com.mmerhav.weiboidextractor.selenium.core.page.FanPage;
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

    @Value("${weibo.cards.path}")
    private String cardsFilePath;

    @Value("${weibo.ids.path}")
    private String idsFilePath;

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

    private int attempts = 0;

    private File cardsFile;

    private File idsFile;

    @PostConstruct
    public void init() {
        log.info("Initiating...");
        cardsFile = new File(cardsFilePath);
        idsFile = new File(idsFilePath);
        FileUtil.createMissingParentDirectories(cardsFile);
        FileUtil.createMissingParentDirectories(idsFile);
    }

    public void run() throws IOException {

        Set<String> fanIds = new HashSet<>();
        Set<Card> alreadyAddedCards = loadAlreadyAddedCards();
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
                            }
                        } else {
                            if (attempts >= maxUserScanAttempts) {
                                driver.navigate().refresh();
                                shouldClickFansTab = true;
                            }
                        }
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
        writeCardCovered(addedCard);
    }

    private void writeId(String id, Set<String> fanIds) throws IOException {
        writeResult(id);
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

    private Set<Card> loadAlreadyAddedCards() throws IOException {
        Set<Card> cards = new HashSet<>();
        Gson gson = new Gson();
        List<String> cardJsons = Files.readAllLines(Paths.get(cardsFilePath));
        for (String cardJson : cardJsons) {
            Card card = gson.fromJson(cardJson, Card.class);
            cards.add(card);
        }
        return cards;
    }

    private void writeResult(String fanId) throws IOException {
        try (FileWriter fileWriter = new FileWriter(idsFile, true)) {
            fileWriter.write(fanId);
            String newLine = System.getProperty("line.separator");
            fileWriter.write(newLine);
        }
    }

    private static void writeResults(Set<String> fanIds) throws IOException {
        File file = new File("C:/Users/Mor/Documents/WeiboIds.txt");
        FileUtil.createMissingParentDirectories(file);
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            for (String fanId : fanIds) {
                fileWriter.write(fanId);
                String newLine = System.getProperty("line.separator");
                fileWriter.write(newLine);
            }
        }
    }

    private void writeCardCovered(Card card) throws IOException {
        try (FileWriter fileWriter = new FileWriter(cardsFile, true)) {
            File file = new File(cardsFilePath);
            FileUtil.createMissingParentDirectories(file);
            fileWriter.write(new Gson().toJson(card));
            String newLine = System.getProperty("line.separator");
            fileWriter.write(newLine);
        }
    }


    private void writeCardsCovered(Set<Card> cards) throws IOException {
        File file = new File(cardsFilePath);
        FileUtil.createMissingParentDirectories(file);

        try (FileWriter fileWriter = new FileWriter(file, true)) {
            for (Card card : cards) {
                fileWriter.write(new Gson().toJson(card));
                String newLine = System.getProperty("line.separator");
                fileWriter.write(newLine);
            }
        }
    }
}
