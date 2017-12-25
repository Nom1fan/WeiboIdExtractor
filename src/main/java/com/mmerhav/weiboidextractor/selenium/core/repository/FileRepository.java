//package com.mmerhav.weiboidextractor.selenium.core.repository;
//
//import ch.qos.logback.core.util.FileUtil;
//import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;
//import com.mmerhav.weiboidextractor.selenium.core.model.Card;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@Setter
//@Slf4j
//@Component
//public class FileRepository implements Repository {
//
//    @Value("${weibo.cards.path}")
//    private String cardsFilePath;
//
//    @Value("${weibo.ids.path}")
//    private String idsFilePath;
//
//    @Autowired
//    private Gson gson;
//
//    @PostConstruct
//    public void init() throws IOException {
//        log.info("Initiating repository...");
//        File cardsFile = new File(cardsFilePath);
//        File idsFile = new File(idsFilePath);
//        FileUtil.createMissingParentDirectories(cardsFile);
//        FileUtil.createMissingParentDirectories(idsFile);
//        if (!cardsFile.exists()) {
//            cardsFile.createNewFile();
//        }
//        if (!idsFile.exists()) {
//            idsFile.createNewFile();
//        }
//    }
//
//    @Override
//    public Set<Card> loadAlreadyAddedCards() {
//        Set<Card> cards = new HashSet<>();
//
//        List<String> cardJsons = null;
//        try {
//            cardJsons = Files.readAllLines(Paths.get(cardsFilePath));
//
//            for (String cardJson : cardJsons) {
//                Card card = gson.fromJson(cardJson, Card.class);
//                cards.add(card);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return cards;
//    }
//
//    @Override
//    public Set<String> loadAlreadyAddedIds() {
//        try {
//            return new HashSet<>(Files.readAllLines(Paths.get(idsFilePath)));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return new HashSet<>();
//    }
//
//    @Override
//    public void writeResult(String fanId) {
//        try (FileWriter fileWriter = new FileWriter(idsFilePath, true)) {
//            fileWriter.write(fanId);
//            String newLine = System.getProperty("line.separator");
//            fileWriter.write(newLine);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void writeResults(Set<String> fanIds) {
//        File file = new File(idsFilePath);
//        FileUtil.createMissingParentDirectories(file);
//        try (FileWriter fileWriter = new FileWriter(file, true)) {
//            for (String fanId : fanIds) {
//                fileWriter.write(fanId);
//                String newLine = System.getProperty("line.separator");
//                fileWriter.write(newLine);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void writeCardCovered(Card card) {
//        try (FileWriter fileWriter = new FileWriter(cardsFilePath, true)) {
//            File file = new File(cardsFilePath);
//            FileUtil.createMissingParentDirectories(file);
//            fileWriter.write(new Gson().toJson(card));
//            String newLine = System.getProperty("line.separator");
//            fileWriter.write(newLine);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Override
//    public void writeCardsCovered(Set<Card> cards) {
//        File file = new File(cardsFilePath);
//        FileUtil.createMissingParentDirectories(file);
//
//        try (FileWriter fileWriter = new FileWriter(file, true)) {
//            for (Card card : cards) {
//                fileWriter.write(new Gson().toJson(card));
//                String newLine = System.getProperty("line.separator");
//                fileWriter.write(newLine);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
