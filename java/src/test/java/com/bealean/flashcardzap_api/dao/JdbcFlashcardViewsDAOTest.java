package com.bealean.flashcardzap_api.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class JdbcFlashcardViewsDAOTest extends JdbcDAOTest {

    private static FlashcardViewsDAO flashcardViewsDAO;

    @BeforeEach
    void configureDatabase() {
        super.configureDatabase();
        flashcardViewsDAO = new JdbcFlashcardViewsDAO(jdbcTemplate);
    }

    @Test
    void recordView_flashcardNotFound_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> flashcardViewsDAO.recordView(-1L), "recordView with non-existent ID throws ResponseStatusException");
        String expectedMessage = "404 NOT_FOUND \"Provided Flashcard ID not found. View not recorded for Flashcard.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "recordView with non-existent ID throws " +
                "exception with expected status and message");
    }

    @Test
    void recordView_badDatabaseURL_throwsException() {
        Long id = insertCard();
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
        dataSource.setUrl("badURL");
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> flashcardViewsDAO.recordView(id),
                "recordView throws ResponseStatusException if database is unavailable");
        String expectedMessage = "500 INTERNAL_SERVER_ERROR \"View not recorded for Flashcard. Check for existence of Flashcard failed.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "recordView with bad Database URL throws " +
                "exception with expected status and message");
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
    }

    @Test
    void recordView_validID_recordsViewTimestampAndReturns1() {
        Timestamp priorTimestamp = new Timestamp(System.currentTimeMillis());
        Long id = insertCard();
        int actualReturnValue = flashcardViewsDAO.recordView(id);
        int expectedReturnValue = 1;
        assertEquals(expectedReturnValue, actualReturnValue, "recordView returns 1 for valid ID");
        String sql = "SELECT MAX(view_timestamp) FROM flashcard_views WHERE flashcard_id = ?";
        Timestamp actualTimeStamp = jdbcTemplate.queryForObject(sql, Timestamp.class, id);
        Timestamp postTimestamp = new Timestamp(System.currentTimeMillis());
        assertTrue(priorTimestamp.before(actualTimeStamp) && postTimestamp.after(actualTimeStamp),
                "recordView with valid ID adds a view record for the card with the expected Timestamp");
    }

    private Long insertCard() {
        String sql = "INSERT INTO flashcards (front, back) VALUES ('test front', 'test back') RETURNING id";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}