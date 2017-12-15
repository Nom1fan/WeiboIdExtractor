package com.mmerhav.weiboidextractor;

import ch.qos.logback.core.util.FileUtil;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mmerhav.weiboidextractor.core.model.Card;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
//@Component
public class WeiboIdCrawlJaxExtractorRunner {

    private static CrawljaxRunner crawljax;

    @Bean
    public Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    private static final int N = 100;

    private static final String cardsFilePath = "C:/Users/Mor/Documents/WeiboCards.txt";

    private static final File cardsFile = new File(cardsFilePath);

    private static final File idsFile = new File("C:/Users/Mor/Documents/WeiboIds.txt");

    private static final File logFile = new File("C:/Users/Mor/Documents/log.txt");

    private static final long WAIT_TIME_AFTER_EVENT = 200;

    private static final long WAIT_TIME_AFTER_RELOAD = 20;

    private static String urlToCrawl;

    public void run() throws IOException {

        init();

        Set<String> fanIds = new HashSet<>();
//        Set<Card> alreadyAddedCards = loadAlreadyAddedCards();
//        Set<String> alreadyAddedNickNames = alreadyAddedCards.stream().map(Card::getName).collect(Collectors.toSet());

        try {

            while (fanIds.size() < N) {

                log.info("Processed {} ids out of {}", fanIds.size(), N);

                String fansButtonXPath = "//*[@id=\"app\"]/div[1]/div[1]/div[1]/div[2]/div[1]/div/div/div/ul/li[3]/span";

                try {
                    log.info("Waiting for fans button...");
//                    waitForElementToLoadByXPath(driver, fansButtonXPath);
                } catch (TimeoutException e) {
//                    if(!isOnMainPage(driver)) {
//                        driver.navigate().back();
                    }
                    continue;
                }

//                driver.findElement(By.xpath(fansButtonXPath)).click();

//                try {
//                    waitForAllCardsToLoad(driver);
//                } catch (TimeoutException e) {
//                    driver.navigate().refresh();
//                    continue;
//                }
//
//                List<WebElement> elements = driver.findElements(By.className("card-wrap"));
//                WebElement element = findNextCard(alreadyAddedNickNames, elements);
//
//                if (element == null) {
//                    driver.navigate().refresh();
//                    continue;
//                }
//
//                String nickName = getNickName(element);
//
//
//                element.click();
//
//                if (!isFanPageLoaded(driver)) {
//                    if (isOnMainPage(driver)) {
//                        driver.navigate().refresh();
//                    } else {
//                        driver.navigate().back();
//                        driver.navigate().refresh();
//                    }
//                    continue; //skip iteration
//                }
//
//                String id = driver.getCurrentUrl().replaceAll("^\\D*(\\d+).*", "$1");
//
//                Card addedCard = new Card(id, nickName);
//                alreadyAddedNickNames.add(nickName);
//
//                fanIds.add(id);
//                writeResult(id);
//                writeCardCovered(addedCard);
//
//                driver.navigate().back();
//                driver.navigate().refresh();
//            }
        } finally {
//            driver.close();
        }
    }

    private static void init() {
        log.info("Initiating...");
        FileUtil.createMissingParentDirectories(cardsFile);
        FileUtil.createMissingParentDirectories(idsFile);

        urlToCrawl = "https://m.weibo.cn/p/index?containerid=231051_-_fansrecomm_-_1730726637&luicode=10000011&lfid=1005051730726637";

        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(urlToCrawl);
        builder.setBrowserConfig(new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME, 1));
        builder.setMaximumDepth(1);
        builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);
        builder.crawlRules().click("div");
        crawljax = new CrawljaxRunner(builder.build());
        CrawlSession crawlSession = crawljax.call();
        Collection<List<Eventable>> crawlPaths = crawlSession.getCrawlPaths();
        StateFlowGraph stateFlowGraph = crawlSession.getStateFlowGraph();

    }

//    private static boolean isOnMainPage(WebDriver driver) {
//        try {
//            log.info("Checking if isOnMainPage");
//            new WebDriverWait(driver, 10).
//                    until(ExpectedConditions.elementToBeClickable(By.className("main-link")));
//            return true;
//        } catch (TimeoutException e) {
//            return false;
//        }
//    }
//
//    private static void waitForAllCardsToLoad(WebDriver driver) {
//        log.info("Waiting for all cards to load...");
//        new WebDriverWait(driver, 10).
//                until(ExpectedConditions.numberOfElementsToBe(By.className("card-wrap"), 20));
//    }
//
//    private static void waitForElementToLoadByXPath(WebDriver driver, String xPath) {
//        new WebDriverWait(driver, 10).
//                until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)));
//    }
//
//    private static WebElement findNextCard(Set<String> alreadyAddedNickNames, List<WebElement> elements) {
//        for (int i = 2; i < elements.size(); i++) {
//            WebElement element = elements.get(i);
//            String nickName = getNickName(element);
//            if (!alreadyAddedNickNames.contains(nickName)) {
//                return element;
//            }
//        }
//        return null;
//    }
//
//    private static Set<Card> loadAlreadyAddedCards() {
//        Set<Card> cards = new HashSet<>();
//        Gson gson = new Gson();
//
//        try {
//            List<String> cardJsons = Files.readAllLines(Paths.get(cardsFilePath));
//            for (String cardJson : cardJsons) {
//                Card card = gson.fromJson(cardJson, Card.class);
//                cards.add(card);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return cards;
//    }
//
//    private static boolean isFanPageLoaded(WebDriver driver) {
//        try {
//            log.info("Checking if fan page is loaded");
//            new WebDriverWait(driver, 10).
//                    until(ExpectedConditions.elementToBeClickable(By.className("btn")));
//            return true;
//        } catch (TimeoutException e) {
//            return false;
//        }
//    }
//
//    private static void writeResult(String fanId) throws IOException {
//        try (FileWriter fileWriter = new FileWriter(idsFile, true)) {
//            fileWriter.write(fanId);
//            String newLine = System.getProperty("line.separator");
//            fileWriter.write(newLine);
//        }
//    }
//
//    private static void writeResults(Set<String> fanIds) throws IOException {
//        File file = new File("C:/Users/Mor/Documents/WeiboIds.txt");
//        FileUtil.createMissingParentDirectories(file);
//        try (FileWriter fileWriter = new FileWriter(file, true)) {
//            for (String fanId : fanIds) {
//                fileWriter.write(fanId);
//                String newLine = System.getProperty("line.separator");
//                fileWriter.write(newLine);
//            }
//        }
//    }
//
//    private static void writeCardCovered(Card card) throws IOException {
//        try (FileWriter fileWriter = new FileWriter(cardsFile, true)) {
//            File file = new File(cardsFilePath);
//            FileUtil.createMissingParentDirectories(file);
//            fileWriter.write(new Gson().toJson(card));
//            String newLine = System.getProperty("line.separator");
//            fileWriter.write(newLine);
//        }
//    }

//
//    private static void writeCardsCovered(Set<Card> cards) throws IOException {
//        File file = new File(cardsFilePath);
//        FileUtil.createMissingParentDirectories(file);
//
//        try (FileWriter fileWriter = new FileWriter(file, true)) {
//            for (Card card : cards) {
//                fileWriter.write(new Gson().toJson(card));
//                String newLine = System.getProperty("line.separator");
//                fileWriter.write(newLine);
//            }
//        }
//    }
//
//    private static String getNickName(WebElement element) {
//        return element.findElements(By.className("m-text-cut")).get(0).getText();
//    }
}
