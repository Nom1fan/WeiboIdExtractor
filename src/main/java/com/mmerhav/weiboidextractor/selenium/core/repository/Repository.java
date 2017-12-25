package com.mmerhav.weiboidextractor.selenium.core.repository;

import com.mmerhav.weiboidextractor.selenium.core.model.Card;

import java.io.IOException;
import java.util.Set;

public interface Repository {
    Set<Card> getCards();

    Set<String> getIds();

    Card getCard(String id);

    void writeCardCovered(Card card);

    void writeCardsCovered(Set<Card> cards);

    void updateCardName(Card card);

    void deleteCard(String id);
}
