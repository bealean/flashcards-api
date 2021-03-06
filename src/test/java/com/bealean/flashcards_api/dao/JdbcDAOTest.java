package com.bealean.flashcards_api.dao;

import com.bealean.flashcards_api.FlashcardsApiApplication;
import com.bealean.flashcards_api.model.Flashcard;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.SQLException;
import java.util.Objects;

@SpringBootTest(classes = FlashcardsApiApplication.class)
public abstract class JdbcDAOTest {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    protected static boolean isDatabaseConfigured = false;
    protected static SingleConnectionDataSource dataSource;
    protected static JdbcTemplate jdbcTemplate;

    @BeforeEach
    void configureDatabase() {
        if (!isDatabaseConfigured) {
            dataSource = new SingleConnectionDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setAutoCommit(false);
            jdbcTemplate = new JdbcTemplate(dataSource);
            isDatabaseConfigured = true;
        }
    }

    @AfterAll
    static void destroyDataSourceAndSetDBConfigFlagFalse() {
        dataSource.destroy();
        isDatabaseConfigured = false;
    }

    @AfterEach
    void tearDown() throws SQLException {
        dataSource.getConnection().rollback();
    }

    long addArea(String areaName) {
        String sql = "INSERT INTO areas (area_name) VALUES (?) RETURNING id";
        Long result = jdbcTemplate.queryForObject(sql, Long.class, areaName);
        return Objects.requireNonNullElse(result, -1L);
    }

    long addCategory(String categoryName) {
        String sql = "INSERT INTO categories (category_name) VALUES (?) RETURNING id";
        Long result = jdbcTemplate.queryForObject(sql, Long.class, categoryName);
        return Objects.requireNonNullElse(result, -1L);
    }

    long addSubcategory(String subcategoryName) {
        String sql = "INSERT INTO subcategories (subcategory_name) VALUES (?) RETURNING id";
        Long result = jdbcTemplate.queryForObject(sql, Long.class, subcategoryName);
        return Objects.requireNonNullElse(result, -1L);
    }

    void addMapping(Long areaId, Long categoryId, Long subcategoryId) {
        String sql = "INSERT INTO area_category_subcategory (area_id, category_id, subcategory_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, areaId, categoryId, subcategoryId);
    }

    void addAreaAndCategoryAndAssociate(String areaName, String categoryName) {
        long areaId = addArea(areaName);
        long categoryId = addCategory(categoryName);
        String sql = "INSERT INTO area_category_subcategory (area_id, category_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, areaId, categoryId);
    }

    Long getAreaIdByName(String areaName) {
        String sql = "SELECT id FROM areas WHERE area_name = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, areaName);
    }

    Long getCategoryIdByName(String categoryName) {
        String sql = "SELECT id FROM categories WHERE category_name = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, categoryName);
    }

    Long getSubcategoryIdByName(String subcategoryName) {
        String sql = "SELECT id FROM subcategories WHERE subcategory_name = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, subcategoryName);
    }

    Flashcard addFlashcard(Flashcard flashcard) {
        Long areaId = null;
        if (flashcard.getArea() != null) {
            areaId = getAreaIdByName(flashcard.getArea());
        }
        Long categoryId = null;
        if (flashcard.getCategory() != null) {
            categoryId = getCategoryIdByName(flashcard.getCategory());
        }
        Long subcategoryId = null;
        if (flashcard.getSubcategory() != null) {
            subcategoryId = getSubcategoryIdByName(flashcard.getSubcategory());
        }
        String sql = "INSERT INTO flashcards (front, back, area_id, category_id, subcategory_id) VALUES (?,?,?,?,?) RETURNING id";
        Long result = jdbcTemplate.queryForObject(sql, Long.class, flashcard.getFront(),
                flashcard.getBack(), areaId, categoryId, subcategoryId);
        flashcard.setId(result);
        return flashcard;
    }

    Flashcard getCardWithRequiredFields() {
        Flashcard flashcard = new Flashcard();
        flashcard.setFront("test");
        flashcard.setBack("test");
        return flashcard;
    }

}
