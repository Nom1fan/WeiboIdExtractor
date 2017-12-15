package com.mmerhav.weiboidextractor;

import ch.qos.logback.core.util.FileUtil;
import com.google.gson.Gson;
import com.mmerhav.weiboidextractor.core.model.Card;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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

    @Value("${url.to.scrape}")
    private String url;

    @Value("${weibo.cards.path}")
    private String cardsFilePath;

    @Value("${weibo.ids.path}")
    private String idsFilePath;

    @Value("${wait.for.elem.timeout}")
    private int waitForElemTimeout;

    @Value("${num.ids.to.add}")
    private int numIdsToAdd;

    @Value("${max.user.scan.attempts}")
    private int maxUserScanAttempts;

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

        ChromeDriver driver = new ChromeDriver();
        try {

            driver.get(url);

            boolean first = true;
            while (fanIds.size() < numIdsToAdd) {
                try {

                    log.info("Processed {} ids out of {}", fanIds.size(), numIdsToAdd);

                    if (!isOnFanListPage(driver)) {
                        log.warn("Fan list page not found, refresh");
                        driver.navigate().refresh();

                        if (isOnFanListPage(driver)) {
                            continue;
                        }

                        log.info("Fan list page still not found, click back");
                        driver.navigate().back();
                        continue;
                    }

                    if (first) {
                        first = false;
                        clickOnFansTab(driver);
                    }

                    WebElement element = scanForElement(alreadyAddedNickNames, driver);

                    if (element == null) {

                        if (attempts >= maxUserScanAttempts) {
                            driver.navigate().refresh();
                            if (isOnFanListPage(driver)) {
                                clickOnFansTab(driver);
                            }
                        }
                        continue;
                    }

                    String nickName = getNickName(element);

                    try {
                        element.click();
                    } catch (WebDriverException e) {
                        log.error("failed to click on element:{}, error message:{}", element.getTagName(), e.getMessage());
                    }

                    if (!isFanPageLoaded(driver)) {
                        if (isOnFanListPage(driver)) {
                            driver.navigate().refresh();
                            if (isOnFanListPage(driver)) {
                                clickOnFansTab(driver);
                            }
                        } else {
                            driver.navigate().back();
                        }
                        continue;
                    }

                    String id = driver.getCurrentUrl().replaceAll("^\\D*(\\d+).*", "$1");

                    if (id.length() == 10) {
                        Card addedCard = new Card(id, nickName);
                        alreadyAddedNickNames.add(nickName);

                        fanIds.add(id);
                        writeResult(id);
                        writeCardCovered(addedCard);
                    }

                    driver.navigate().back();
                } catch (WebDriverException e) {
                    driver.get(url);
                }
            }
        }
        finally {
            driver.close();
        }
    }

    private WebElement scanForElement(Set<String> alreadyAddedNickNames, ChromeDriver driver) {
        List<WebElement> elements;
        WebElement element;
        attempts = 0;

        do {

            driver.executeScript("window.scrollBy(0,250)", "");
            elements = driver.findElements(By.className("card-wrap"));
            element = findNextCard(alreadyAddedNickNames, elements);

            attempts++;
            log.info("scan for card attempt {} of {}", attempts, maxUserScanAttempts);
        } while (element == null && attempts < maxUserScanAttempts);
        return element;
    }

    private void clickOnFansTab(ChromeDriver driver) {
        try {
            log.info("Click on fans tab");
            String fansButtonXPath = "//*[@id=\"app\"]/div[1]/div[1]/div[1]/div[2]/div[1]/div/div/div/ul/li[3]/span";
            driver.findElement(By.xpath(fansButtonXPath)).click();
        } catch (NoSuchElementException e) {
            log.error("Enable to find fans tab to click on. Error message:{}", e.getMessage());
        }
    }

    private boolean isOnFanListPage(WebDriver driver) {

        log.info("Checking if isOnFanListPage");
        if (driver.getCurrentUrl().equals(url)) {
            try {
                new WebDriverWait(driver, waitForElemTimeout).
                        until(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("card-list"), 1));
                return true;
            } catch (TimeoutException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    private void waitForElementToLoadByXPath(WebDriver driver, String xPath) {
        new WebDriverWait(driver, waitForElemTimeout).
                until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
    }

    private WebElement findNextCard(Set<String> alreadyAddedNickNames, List<WebElement> elements) {
        log.info("Finding next card...");
        for (int i = 2; i < elements.size(); i++) {
            WebElement element = elements.get(i);
            String nickName = getNickName(element);
            if (nickName != null && !alreadyAddedNickNames.contains(nickName)) {
                log.info("Next card found:{}", element);
                return element;
            }
        }
        log.warn("Next card not found");
        return null;
    }

    private Set<Card> loadAlreadyAddedCards() {
        Set<Card> cards = new HashSet<>();
        Gson gson = new Gson();

        try {
            List<String> cardJsons = Files.readAllLines(Paths.get(cardsFilePath));
            for (String cardJson : cardJsons) {
                Card card = gson.fromJson(cardJson, Card.class);
                cards.add(card);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cards;
    }

    private boolean isFanPageLoaded(WebDriver driver) {

        boolean btnFound;
        try {
            log.info("Checking if fan page is loaded");
            new WebDriverWait(driver, waitForElemTimeout).
                    until(ExpectedConditions.elementToBeClickable(By.className("btn")));
            btnFound = true;
        } catch (TimeoutException e) {
            btnFound = false;
        }

        if (btnFound) {
            return true;
        }

        try {
            new WebDriverWait(driver, waitForElemTimeout).
                    until(ExpectedConditions.elementToBeClickable(By.className("empty_failed")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
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

    private static String getNickName(WebElement element) {

        List<WebElement> elements = element.findElements(By.className("m-text-cut"));
        if (elements.size() > 0) {
            return elements.get(0).getText();
        }
        return null;
    }
}
