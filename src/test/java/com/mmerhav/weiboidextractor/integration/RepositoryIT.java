package com.mmerhav.weiboidextractor.integration;

import com.mmerhav.weiboidextractor.selenium.core.model.Card;
import com.mmerhav.weiboidextractor.selenium.core.repository.CardsDBRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RepositoryIT {

	@Autowired
	CardsDBRepository cardsDBRepository;

	@Test
	public void writeDuplicateThenUpdateName() {
        String id = "1000199580";
        String newName = "newName";
        try {

		    cardsDBRepository.deleteCard(id);
			cardsDBRepository.writeCardCovered(new Card(id, null));
			Card expectedCard = new Card(id, null);
			Card actualCard = cardsDBRepository.getCard(id);
			Assert.assertEquals(expectedCard, actualCard);
			cardsDBRepository.writeCardCovered(new Card(id, newName));

		} catch (DuplicateKeyException e) {
			cardsDBRepository.updateCardName(new Card(id, newName));
			Card expectedCard = new Card(id, newName);
			Card actualCard = cardsDBRepository.getCard(id);
			Assert.assertEquals(expectedCard, actualCard);
		}

	}

}
