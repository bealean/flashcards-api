package com.bealean.flashcardzap_api.dao;

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
        a view for it. */
        String sql = "SELECT COUNT(*) FROM flashcards WHERE id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
            if (count == null || !count.equals(1)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Provided Flashcard ID not found. View not recorded for Flashcard.");
            }
        } catch (DataAccessException e) {
            System.out.println("Caught Exception: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "View not recorded for Flashcard. Check for existence of Flashcard failed.");
        }
        sql = "INSERT INTO flashcard_views (flashcard_id, view_timestamp) VALUES (?, now())";
        try {
            return jdbcTemplate.update(sql, id);
        } catch (DataAccessException e) {
            System.out.println("Caught Exception: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "View Date and Time failed to be recorded.");
        }
    }
}
