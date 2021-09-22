package com.bealean.flashcardzap_api.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class JdbcFlashcardDAOGetNextTest extends JdbcDAOTest {

    private static FlashcardDAO flashcardDAO;

    @BeforeEach
    void configureDatabase() {
        super.configureDatabase();
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        flashcardDAO = new JdbcFlashcardDAO(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @Test
    void getNext_allAreasAllCategoriesAllSubcategoriesUnviewedCard_returnsCardWithNullLastViewed(){

    }
}