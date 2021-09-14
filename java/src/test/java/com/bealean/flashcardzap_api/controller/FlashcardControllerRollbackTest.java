package com.bealean.flashcardzap_api.controller;

import com.bealean.flashcardzap_api.FlashcardZapApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = FlashcardZapApiApplication.class)
@AutoConfigureMockMvc
class FlashcardControllerRollbackTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    // Disable Bean Validation to force errors and test transaction rollback
    @MockBean
    private LocalValidatorFactoryBean validator;

    @Test
    public void newFlashcard_insertTransactionWithInvalidCard_previousInsertsRolledBack() throws Exception {
        String flashcard = "{\n" +
                "    \"front\":null,\n" +
                "  \"back\": \"test back\",\n" +
                "  \"area\": \"test area\",\n" +
                "  \"category\": \"test category\",\n" +
                "  \"subcategory\": \"test subcategory\"\n" +
                "}";

            /* Add card with new Area, Category, and Subcategory. This should add a record to each of the corresponding tables
            and add a mapping record to the area_category_subcategory table before inserting a record to the flashcards table. The flashcards insert should fail
            because the front is null and the database has a not null constraint on that column. The four previous database inserts should be rolled back.*/
        String sql = "SELECT COUNT(*) FROM area_category_subcategory";
        Integer originalMappingCount = jdbcTemplate.queryForObject(sql,Integer.class);
        this.mockMvc.perform(post("/new-flashcard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flashcard));
        // Query for mapping count again after post - should not have changed
        Integer finalMappingCount = jdbcTemplate.queryForObject(sql,Integer.class);
        assertEquals(originalMappingCount,finalMappingCount,"newFlashcard with new area, category, subcategory, and mapping rolls back " +
                "mapping insert on flashcard insert failure");
        sql = "SELECT COUNT(*) FROM areas WHERE area_name = ?";
        Integer totalCount = jdbcTemplate.queryForObject(sql, Integer.class, "test area");
        sql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
        totalCount += jdbcTemplate.queryForObject(sql, Integer.class, "test category");
        sql = "SELECT COUNT(*) FROM subcategories WHERE subcategory_name = ?";
        totalCount += jdbcTemplate.queryForObject(sql, Integer.class, "test subcategory");
        assertEquals(0, totalCount, "newFlashcard with new area, category, subcategory, and mapping rolls back " +
                "area, category, and subcategory inserts on flashcard insert failure");
    }

    @Test
    public void newFlashcard_insertTransactionWithInvalidSubcategory_previousInsertsRolledBack() throws Exception {
        String flashcard = "{\n" +
                "    \"front\": \"test front\",\n" +
                "  \"back\": \"test back\",\n" +
                "  \"area\": \"test area\",\n" +
                "  \"category\": \"test category\",\n" +
                "  \"subcategory\": \"test subcategory test subcategory\"\n" +
                "}";

            /* Add card with new Area, Category, and invalid Subcategory. This should add a record to the areas and categories tables
            before inserting a record to the subcategories table. The subcategories insert should fail
            because the length exceeds the maximum column length in the database.
            The two previous database inserts should be rolled back.*/
        this.mockMvc.perform(post("/new-flashcard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flashcard));
        String sql = "SELECT COUNT(*) FROM areas WHERE area_name = ?";
        Integer totalCount = jdbcTemplate.queryForObject(sql, Integer.class, "test area");
        sql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
        totalCount += jdbcTemplate.queryForObject(sql, Integer.class, "test category");
        assertEquals(0, totalCount, "newFlashcard with new area, new category, and invalid subcategory rolls back previous inserts " +
                "on subcategory insert failure");
    }

    @Test
    public void newFlashcard_insertTransactionWithInvalidCategory_areaInsertRolledBack() throws Exception {
        String flashcard = "{\n" +
                "    \"front\": \"test front\",\n" +
                "  \"back\": \"test back\",\n" +
                "  \"area\": \"test area\",\n" +
                "  \"category\": \"test category test category test category\",\n" +
                "  \"subcategory\": \"test subcategory\"\n" +
                "}";

            /* Add card with new Area and invalid Category. This should add a record to the areas table
            before inserting a record to the categories table. The categories insert should fail
            because the length exceeds the maximum column length in the database.
            The previous area insert should be rolled back.*/
        this.mockMvc.perform(post("/new-flashcard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flashcard));
        String sql = "SELECT COUNT(*) FROM areas WHERE area_name = ?";
        Integer totalCount = jdbcTemplate.queryForObject(sql, Integer.class, "test area");
        assertEquals(0, totalCount, "newFlashcard with new area and invalid new category rolls back area insert " +
                "on category insert failure");
    }

}