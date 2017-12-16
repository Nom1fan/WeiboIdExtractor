package com.mmerhav.weiboidextractor.selenium.core.repository;

import ch.qos.logback.core.util.FileUtil;
import com.google.gson.Gson;
import com.mmerhav.weiboidextractor.selenium.core.model.Card;
import lombok.extern.slf4j.Slf4j;
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

@Component
@Slf4j
public class RepositoryImpl implements Repository {

    @Value("${weibo.cards.path}")
    private String cardsFilePath;

    @Value("${weibo.ids.path}")
    private String idsFilePath;

    @PostConstruct
    public void init() {
        log.info("Initiating repository...");
        File cardsFile = new File(cardsFilePath);
        File idsFile = new File(idsFilePath);
        FileUtil.createMissingParentDirectories(cardsFile);
        FileUtil.createMissingParentDirectories(idsFile);
    }

    @Override
    public Set<Card> loadAlreadyAddedCards() throws IOException {
        Set<Card> cards = new HashSet<>();
        Gson gson = new Gson();
        File cardsFile = new File(cardsFilePath);
        if(cardsFile.exists()) {
            List<String> cardJsons = Files.readAllLines(Paths.get(cardsFilePath));
            for (String cardJson : cardJsons) {
                Card card = gson.fromJson(cardJson, Card.class);
                cards.add(card);
            }
        } else {
            cardsFile.createNewFile();
        }
        return cards;
    }

    @Override
    public void writeResult(String fanId) throws IOException {
        try (FileWriter fileWriter = new FileWriter(idsFilePath, true)) {
            fileWriter.write(fanId);
            String newLine = System.getProperty("line.separator");
            fileWriter.write(newLine);
        }
    }

    @Override
    public void writeResults(Set<String> fanIds) throws IOException {
        File file = new File(idsFilePath);
        FileUtil.createMissingParentDirectories(file);
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            for (String fanId : fanIds) {
                fileWriter.write(fanId);
                String newLine = System.getProperty("line.separator");
                fileWriter.write(newLine);
            }
        }
    }

    @Override
    public void writeCardCovered(Card card) throws IOException {
        try (FileWriter fileWriter = new FileWriter(cardsFilePath, true)) {
            File file = new File(cardsFilePath);
            FileUtil.createMissingParentDirectories(file);
            fileWriter.write(new Gson().toJson(card));
            String newLine = System.getProperty("line.separator");
            fileWriter.write(newLine);
        }
    }


    @Override
    public void writeCardsCovered(Set<Card> cards) throws IOException {
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
