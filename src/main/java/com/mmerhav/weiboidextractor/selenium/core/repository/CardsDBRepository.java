package com.mmerhav.weiboidextractor.selenium.core.repository;

import com.mmerhav.weiboidextractor.selenium.core.model.Card;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Slf4j
@org.springframework.stereotype.Repository
public class CardsDBRepository implements Repository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Set<Card> getCards() {
        String sql = "SELECT * FROM cards";
        return new HashSet<>(jdbcTemplate.query(sql, (resultSet, i) -> new Card(resultSet.getString("id"), resultSet.getString("name"))));
    }

    @Override
    public Set<String> getIds() {
        Set<Card> cards = getCards();
        return cards.stream().map(Card::getId).collect(Collectors.toSet());
    }

    @Override
    public Card getCard(String id) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        String sql = "SELECT * FROM cards WHERE id=:id";
        return namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource().addValue("id", id),
                (resultSet, i) -> new Card(resultSet.getString("id"), resultSet.getString("name")));
    }

    @Override
    public void writeCardCovered(Card card) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource());
        jdbcInsert.withTableName("cards");
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(card);
        jdbcInsert.execute(sqlParameterSource);
    }


    @Override
    public void writeCardsCovered(Set<Card> cards) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getDataSource());
        jdbcInsert.withTableName("cards");
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(cards.toArray());
        jdbcInsert.executeBatch(batch);
    }

    @Override
    public void updateCardName(Card card) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        String sql = "UPDATE cards SET name=:name WHERE id=:id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", card.getName());
        params.addValue("id", card.getId());
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public void deleteCard(String id) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        String sql = "DELETE FROM cards WHERE  id=:id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        namedParameterJdbcTemplate.update(sql, params);
    }
}
