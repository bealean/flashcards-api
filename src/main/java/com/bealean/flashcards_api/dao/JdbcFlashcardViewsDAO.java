package com.bealean.flashcards_api.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class JdbcFlashcardViewsDAO implements FlashcardViewsDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcFlashcardViewsDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int recordView(Long id) {
        /* Check if flashcard exists with provided ID before attempting to record
         * a view for it. */
        String cardSql = "SELECT COUNT(*) FROM flashcards WHERE id = ?";

        /* Check if card has been viewed or not. */
        String viewSql = "SELECT COUNT(*) FROM flashcard_last_view WHERE flashcard_id = ?";
        Integer viewCount;

        String recordViewSQL = "";
        try {
            Integer count = jdbcTemplate.queryForObject(cardSql, Integer.class, id);
            if (count == null || !count.equals(1)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Provided Flashcard ID not found. View not recorded for Flashcard.");
            }
            viewCount = jdbcTemplate.queryForObject(viewSql, Integer.class, id);
            if (viewCount != null && viewCount.equals(0)) {
                /* Used clock_timestamp() instead of now() because now() is the timestamp for the
                 * start of the transaction, rather than the current time and tests can have
                 * multiple statements in the same transaction (e.g. and update test may do insert
                 * first, then update, and then compare timestamps). */
                recordViewSQL = "INSERT INTO flashcard_last_view (flashcard_id, view_timestamp) VALUES (?, clock_timestamp())";
            } else if (viewCount != null && viewCount.compareTo(0) > 0) {
                recordViewSQL = "UPDATE flashcard_last_view SET view_timestamp = clock_timestamp() WHERE flashcard_id = ?";
            }
            return jdbcTemplate.update(recordViewSQL, id);
        } catch (DataAccessException e) {
            System.out.println("Caught Exception: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Flashcard view failed to be recorded.");
        }
    }
}
