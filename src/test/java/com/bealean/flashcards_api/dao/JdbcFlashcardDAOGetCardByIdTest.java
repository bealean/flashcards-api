package com.bealean.flashcardzap_api.dao;

import com.bealean.flashcardzap_api.model.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class JdbcFlashcardDAOGetCardByIdTest extends JdbcDAOTest {

    private FlashcardDAO flashcardDAO;
    private static final String EXPECTED_FRONT = "JUnit Front";
    private static final String EXPECTED_BACK = "JUnit Back";
    private static final String EXPECTED_AREA = "JUnit Area";
    private static final String EXPECTED_CATEGORY = "JUnit Category";
    private static final String EXPECTED_SUBCATEGORY = "JUnit Subcategory";

    @BeforeEach
    void configureDatabase() {
        super.configureDatabase();
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        flashcardDAO = new JdbcFlashcardDAO(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @Test
    void getCardById_existingId_returnsExpectedCard() {
        Flashcard expectedFlashcard = new Flashcard();
        expectedFlashcard.setFront(EXPECTED_FRONT);
        expectedFlashcard.setBack(EXPECTED_BACK);
        expectedFlashcard.setArea(EXPECTED_AREA);
        expectedFlashcard.setCategory(EXPECTED_CATEGORY);
        expectedFlashcard.setSubcategory(EXPECTED_SUBCATEGORY);
        addArea(EXPECTED_AREA);
        addCategory(EXPECTED_CATEGORY);
        addSubcategory(EXPECTED_SUBCATEGORY);

        addFlashcard(expectedFlashcard);
        Flashcard actualFlashcard = flashcardDAO.getCardById(expectedFlashcard.getId());
        assertEquals(expectedFlashcard, actualFlashcard, "getCardById with an existing Id returns the card with that Id");
    }

    @Test
    void getCardById_noMatchingId_throwsResponseStatusExceptionWithNotFoundStatusAndExpectedMessage() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.getCardById(-1L),
                "getCardById throws ResponseStatusException if a card does not exist with the Id");
        String expectedMessage = "404 NOT_FOUND \"No cards exist with requested Id\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getCardById throws exception with NOT_FOUND status and expected message, " +
                "if a card does not exist matching the requested Id");
    }

    @Test
    void getCardById_nullId_throwsResponseStatusExceptionWithBadRequestStatusAndExpectedMessage() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.getCardById(null),
                "getCardById throws ResponseStatusException if called with a null Id");
        String expectedMessage = "400 BAD_REQUEST \"Id cannot be null\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getCardById throws exception with BAD_REQUEST status and expected message, " +
                "if called with a null Id");
    }

    @Test
    void getCardById_databaseUnavailable_throwsResponseStatusExceptionWithInternalServerErrorStatusAndExpectedMessage() {
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
        dataSource.setUrl("badUrl");
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.getCardById(1L),
                "getCardById throws ResponseStatusException if query to retrieve card by Id fails");
        String expectedMessage = "500 INTERNAL_SERVER_ERROR \"Exception retrieving card from database by Id\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getCardById throws exception with INTERNAL_SERVER_ERROR status and expected message, " +
                "if query to retrieve card from database fails");
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
    }
}