package com.mmerhav.weiboidextractor.selenium.core.repository;

import com.mmerhav.weiboidextractor.selenium.core.model.Card;

import java.io.IOException;
import java.util.Set;

public interface Repository {
    Set<Card> loadAlreadyAddedCards() throws IOException;

    void writeResult(String fanId) throws IOException;

    void writeResults(Set<String> fanIds) throws IOException;

    void writeCardCovered(Card card) throws IOException;

    void writeCardsCovered(Set<Card> cards) throws IOException;
}
