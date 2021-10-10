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
        String expectedMessage = "500 INTERNAL_SERVER_ERROR \"Flashcard view failed to be recorded.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "recordView with bad Database URL throws " +
                "exception with expected status and message");
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
    }

    @Test
    void recordView_existingCardNoPriorViews_insertsNewRecordToFlashcardLastViewWithViewTimestampAndReturns1() {
        /* Tests were originally checking database Timestamps against a Timestamp created with
        "new Timestamp(System.currentTimeMillis())", but there were intermittent false failures
        due to slight discrepancies between the Timestamps.
        Tests were updated to use PostgreSQL clock_timestamp() function instead. */

        Timestamp priorTimestamp = getDatabaseTimestamp();
        Long id = insertCard();
        Integer expectedFlashcardLastViewRowCountForCard = 1;

        int expectedReturnValue = 1;
        int actualReturnValue = flashcardViewsDAO.recordView(id);
        assertEquals(expectedReturnValue, actualReturnValue, "recordView returns 1 for existing card with no prior views");

        Integer actualFlashcardLastViewRowCountForCard = getFlashcardLastViewRowCountForCard(id);
        assertEquals(expectedFlashcardLastViewRowCountForCard, actualFlashcardLastViewRowCountForCard,
                "recordView inserts a new row into flashcard_last_view for an existing card with no views");

        String sql = "SELECT view_timestamp FROM flashcard_last_view WHERE flashcard_id = ?";
        Timestamp actualFlashcardLastViewTimeStampForCard = jdbcTemplate.queryForObject(sql, Timestamp.class, id);

        Timestamp postTimestamp = getDatabaseTimestamp();
        assertTrue(priorTimestamp.before(actualFlashcardLastViewTimeStampForCard) && postTimestamp.after(actualFlashcardLastViewTimeStampForCard),
                "recordView for an existing card with no views adds the expected Timestamp to flashcard_last_view for the card");
    }

    @Test
    void recordView_existingCardNoPriorViews_insertsNewRecordToFlashcardViewsWithViewTimestamp() {
        Timestamp priorTimestamp = getDatabaseTimestamp();
        Long id = insertCard();
        flashcardViewsDAO.recordView(id);
        Integer expectedFlashcardViewsRowCountForCard = 1;
        Integer actualFlashcardViewsRowCountForCard = getFlashcardViewsRowCountForCard(id);
        assertEquals(expectedFlashcardViewsRowCountForCard, actualFlashcardViewsRowCountForCard,
                "recordView inserts a new row into flashcard_views for an existing card with no views");

        String sql = "SELECT MAX(view_timestamp) FROM flashcard_views WHERE flashcard_id = ?";
        Timestamp actualFlashcardViewsMaxTimeStampForCard = jdbcTemplate.queryForObject(sql, Timestamp.class, id);
        Timestamp postTimestamp = getDatabaseTimestamp();
        assertTrue(priorTimestamp.before(actualFlashcardViewsMaxTimeStampForCard) && postTimestamp.after(actualFlashcardViewsMaxTimeStampForCard),
                "recordView inserts expected view_timestamp into flashcard_views for an existing card with no views");
    }

    @Test
    void recordView_cardWithPriorViews_updatesExistingFlashcardLastViewRecordWithViewTimestampAndReturns1() {
        Long id = insertCard();
        flashcardViewsDAO.recordView(id);
        String selectTimestampFromFlashcardLastViewSql = "SELECT view_timestamp FROM flashcard_last_view WHERE flashcard_id = ?";
        Timestamp priorFlashcardLastViewTimestampForCard = jdbcTemplate.queryForObject(selectTimestampFromFlashcardLastViewSql, Timestamp.class, id);
        Integer expectedFlashcardLastViewRowCountForCard = 1;

        int expectedReturnValue = 1;
        int actualReturnValue = flashcardViewsDAO.recordView(id);
        assertEquals(expectedReturnValue, actualReturnValue, "recordView returns 1 for existing card with prior views");

        Integer actualFlashcardLastViewRowCountForCard = getFlashcardLastViewRowCountForCard(id);
        assertEquals(expectedFlashcardLastViewRowCountForCard, actualFlashcardLastViewRowCountForCard,
                "recordView does not insert a new row into flashcard_last_view for a card with an existing record");

        Timestamp actualFlashcardLastViewTimestampForCard = jdbcTemplate.queryForObject(selectTimestampFromFlashcardLastViewSql, Timestamp.class, id);
        Timestamp postTimestamp = getDatabaseTimestamp();
        assertTrue(priorFlashcardLastViewTimestampForCard != null && priorFlashcardLastViewTimestampForCard.before(actualFlashcardLastViewTimestampForCard)
                        && postTimestamp.after(actualFlashcardLastViewTimestampForCard),
                "recordView for a card with previous views updates the view_timestamp for the card in flashcard_last_view");
    }

    @Test
    void recordView_cardWithPriorViews_insertsNewFlashcardViewsRecordWithViewTimestamp() {
        Long id = insertCard();
        flashcardViewsDAO.recordView(id);

        Integer expectedFlashcardViewsRowCountForCard = 2;
        flashcardViewsDAO.recordView(id);
        Integer actualFlashcardViewsRowCountForCard = getFlashcardViewsRowCountForCard(id);
        assertEquals(expectedFlashcardViewsRowCountForCard, actualFlashcardViewsRowCountForCard,
                "recordView inserts a new row into flashcard_views for a previously viewed card");

        String selectTimestampFromFlashcardLastViewSql = "SELECT view_timestamp FROM flashcard_last_view WHERE flashcard_id = ?";
        Timestamp expectedFlashcardViewsMaxTimestampForCard = jdbcTemplate.queryForObject(selectTimestampFromFlashcardLastViewSql, Timestamp.class, id);
        String sql = "SELECT MAX(view_timestamp) FROM flashcard_views WHERE flashcard_id = ?";
        Timestamp actualFlashcardViewsMaxTimestampForCard = jdbcTemplate.queryForObject(sql, Timestamp.class, id);
        assertTrue(actualFlashcardViewsMaxTimestampForCard != null && actualFlashcardViewsMaxTimestampForCard.equals(expectedFlashcardViewsMaxTimestampForCard),
                "recordView for a card with previous views inserts a record into flashcard_views for the card with the same timestamp as the updated flashcard_last_view record");
    }

    private Long insertCard() {
        String sql = "INSERT INTO flashcards (front, back) VALUES ('test front', 'test back') RETURNING id";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private Integer getFlashcardLastViewRowCountForCard(Long id) {
        String sql = "SELECT COUNT(*) FROM flashcard_last_view WHERE flashcard_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id);
    }

    private Integer getFlashcardViewsRowCountForCard(Long id) {
        String sql = "SELECT COUNT(*) FROM flashcard_views WHERE flashcard_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id);
    }

    private Timestamp getDatabaseTimestamp() {
        String sql = "SELECT clock_timestamp()";
        return jdbcTemplate.queryForObject(sql, Timestamp.class);
    }
}