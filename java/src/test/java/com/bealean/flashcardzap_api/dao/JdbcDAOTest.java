package com.bealean.flashcardzap_api.dao;

import com.bealean.flashcardzap_api.FlashcardZapApiApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Objects;

@SpringBootTest(classes = FlashcardZapApiApplication.class)
public class JdbcDAOTest {

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
    public static void destroyDataSourceAndSetDBConfigFlagFalse() {
        dataSource.destroy();
        isDatabaseConfigured = false;
    }

    @AfterEach
    void tearDown() throws SQLException {
        dataSource.getConnection().rollback();
    }

    long addArea(String areaName) {
        String sql = "INSERT INTO areas (area_name) VALUES (?) RETURNING id";
        Long result = -1L;
        result = jdbcTemplate.queryForObject(sql, Long.class, areaName);
        return Objects.requireNonNullElse(result, -1L);
    }

    long addCategory(String categoryName) {
        String sql = "INSERT INTO categories (category_name) VALUES (?) RETURNING id";
        Long result = -1L;
        result = jdbcTemplate.queryForObject(sql, Long.class, categoryName);
        return Objects.requireNonNullElse(result, -1L);
    }

    long addSubcategory(String subcategoryName) {
        String sql = "INSERT INTO subcategories (subcategory_name) VALUES (?) RETURNING id";
        Long result = -1L;
        result = jdbcTemplate.queryForObject(sql, Long.class, subcategoryName);
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

}
