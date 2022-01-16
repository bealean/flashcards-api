package com.bealean.flashcards_api.controller;

import com.bealean.flashcards_api.FlashcardsApiApplication;
import com.bealean.flashcards_api.model.Flashcard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = FlashcardsApiApplication.class)
@AutoConfigureMockMvc
class FlashcardControllerRollbackTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_FRONT = "test front";
    private static final String VALID_BACK = "test back";
    private static final String VALID_AREA = "test area";
    private static final String VALID_CATEGORY = "test category";
    private static final String VALID_SUBCATEGORY = "test subcategory";


    /* Call @Transactional method externally through controller to test transactional rollback.
       Disable Bean Validation to force errors. */
    @MockBean
    private LocalValidatorFactoryBean validator;

    @Test
    public void newFlashcard_insertTransactionWithInvalidCard_previousInsertsRolledBack() throws Exception {
        Flashcard flashcard = new Flashcard();
        flashcard.setBack(VALID_BACK);
        flashcard.setArea(VALID_AREA);
        flashcard.setCategory(VALID_CATEGORY);
        flashcard.setSubcategory(VALID_SUBCATEGORY);
        String flashcardContent = mapper.writeValueAsString(flashcard);

            /* Add card with new Area, Category, and Subcategory. This should add a record to each of the corresponding tables
            and add a mapping record to the area_category_subcategory table before inserting a record to the flashcards table. The flashcards insert should fail
            because the front is null and the database has a not null constraint on that column. The four previous database inserts should be rolled back.*/
        String sql = "SELECT COUNT(*) FROM area_category_subcategory";
        Integer originalMappingCount = jdbcTemplate.queryForObject(sql,Integer.class);
        mockMvc.perform(post("/new-flashcard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flashcardContent));
        // Query for mapping count again after post - should not have changed
        Integer finalMappingCount = jdbcTemplate.queryForObject(sql,Integer.class);
        // Remove test mapping if transactional rollback failed
        if (finalMappingCount != null && originalMappingCount != null &&
                finalMappingCount.compareTo(originalMappingCount) > 0) {
            removeMapping();
        }
        sql = "SELECT COUNT(*) FROM areas WHERE area_name = ?";
        Integer areaCount = jdbcTemplate.queryForObject(sql, Integer.class, VALID_AREA);
        // Remove test area if transactional rollback failed
        if (areaCount != null && areaCount.compareTo(0) > 0) {
            removeArea();
        }
        sql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
        Integer categoryCount = jdbcTemplate.queryForObject(sql, Integer.class, VALID_CATEGORY);
        // Remove test category if transactional rollback failed
        if (categoryCount != null && categoryCount.compareTo(0) > 0) {
            removeCategory();
        }
        sql = "SELECT COUNT(*) FROM subcategories WHERE subcategory_name = ?";
        Integer subcategoryCount = jdbcTemplate.queryForObject(sql, Integer.class, VALID_SUBCATEGORY);
        // Remove test subcategory if transactional rollback failed
        if (subcategoryCount != null && subcategoryCount.compareTo(0) > 0) {
            removeSubcategory();
        }
        assertEquals(originalMappingCount,finalMappingCount,"newFlashcard with new area, category, subcategory, and mapping rolls back " +
                "mapping insert on flashcard insert failure");
        int totalCount = Objects.requireNonNullElse(areaCount,0) +
                Objects.requireNonNullElse(categoryCount,0) +
                Objects.requireNonNullElse(subcategoryCount, 0);
        assertEquals(0, totalCount, "newFlashcard with new area, category, subcategory, and mapping rolls back " +
                "area, category, and subcategory inserts on flashcard insert failure");
    }

    @Test
    public void newFlashcard_insertTransactionWithInvalidSubcategory_previousInsertsRolledBack() throws Exception {
        Flashcard flashcard = new Flashcard();
        flashcard.setFront(VALID_FRONT);
        flashcard.setBack(VALID_BACK);
        flashcard.setArea(VALID_AREA);
        flashcard.setCategory(VALID_CATEGORY);
        flashcard.setSubcategory("test subcategory test subcategory");
        String flashcardContent = mapper.writeValueAsString(flashcard);

            /* Add card with new Area, Category, and invalid Subcategory. This should add a record to the areas and categories tables
            before inserting a record to the subcategories table. The subcategories insert should fail
            because the length exceeds the maximum column length in the database.
            The two previous database inserts should be rolled back.*/
        mockMvc.perform(post("/new-flashcard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flashcardContent));
        String sql = "SELECT COUNT(*) FROM areas WHERE area_name = ?";
        Integer areaCount = jdbcTemplate.queryForObject(sql, Integer.class, VALID_AREA);
        if (areaCount != null && areaCount.compareTo(0) > 0) {
            removeArea();
        }
        sql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
        Integer categoryCount = jdbcTemplate.queryForObject(sql, Integer.class, VALID_CATEGORY);
        if (categoryCount != null && categoryCount.compareTo(0) > 0) {
            removeCategory();
        }
        int totalCount = Objects.requireNonNullElse(areaCount,0) +
                Objects.requireNonNullElse(categoryCount,0);

        assertEquals(0, totalCount, "newFlashcard with new area, new category, and invalid subcategory rolls back previous inserts " +
                "on subcategory insert failure");
    }

    @Test
    public void newFlashcard_insertTransactionWithInvalidCategory_areaInsertRolledBack() throws Exception {
        Flashcard flashcard = new Flashcard();
        flashcard.setFront(VALID_FRONT);
        flashcard.setBack(VALID_BACK);
        flashcard.setArea(VALID_AREA);
        flashcard.setCategory("test category test category test category");
        flashcard.setSubcategory(VALID_SUBCATEGORY);
        String flashcardContent = mapper.writeValueAsString(flashcard);

            /* Add card with new Area and invalid Category. This should add a record to the areas table
            before inserting a record to the categories table. The categories insert should fail
            because the length exceeds the maximum column length in the database.
            The previous area insert should be rolled back.*/
        mockMvc.perform(post("/new-flashcard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flashcardContent));
        String sql = "SELECT COUNT(*) FROM areas WHERE area_name = ?";
        Integer areaCount = jdbcTemplate.queryForObject(sql, Integer.class, "test area");
        if (areaCount != null && areaCount.compareTo(0) > 0) {
            removeArea();
        }
        assertEquals(0, areaCount, "newFlashcard with new area and invalid new category rolls back area insert " +
                "on category insert failure");
    }


    private void removeMapping() {
        String sql = "DELETE FROM area_category_subcategory acs " +
                "USING areas a, categories c, subcategories s " +
                "WHERE (acs.area_id = a.id AND a.area_name = ?) " +
                "AND (acs.category_id = c.id AND c.category_name = ?) " +
                "AND (acs.subcategory_id = s.id AND s.subcategory_name = ?)";
        jdbcTemplate.update(sql,VALID_AREA,VALID_CATEGORY,VALID_SUBCATEGORY);
    }

    private void removeArea() {
        String sql = "DELETE FROM areas WHERE area_name = ?";
        jdbcTemplate.update(sql,VALID_AREA);
    }

    private void removeCategory() {
        String sql = "DELETE FROM categories WHERE category_name = ?";
        jdbcTemplate.update(sql,VALID_CATEGORY);
    }

    private void removeSubcategory() {
        String sql = "DELETE FROM subcategories WHERE subcategory_name = ?";
        jdbcTemplate.update(sql,VALID_SUBCATEGORY);
    }

}