package com.bealean.flashcardzap_api.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class JdbcCategoryDAO implements CategoryDAO {

    @Autowired
    AreaDAO areaDAO;

    private final JdbcTemplate jdbcTemplate;

    public JdbcCategoryDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.areaDAO = new JdbcAreaDAO(jdbcTemplate);
    }

    /* Not providing a method to get Categories for a Subcategory for now. */
    @Override
    public List<String> getCategoriesForArea(String areaName) {
        List<String> categoryList = new ArrayList<>();
        SqlRowSet results;

        if (areaName != null && areaName.trim().equalsIgnoreCase("all")) {
            /* Categories table has UNIQUE constraint on category_name.
             Case variations are allowed. If user decides to use a different case, it will be possible
             to update cards to new case and old case will be automatically removed when it is no longer
             referenced by any cards. */
            String sql = "SELECT category_name FROM categories ORDER BY category_name";
            results = jdbcTemplate.queryForRowSet(sql);
        } else {
            long areaId = areaDAO.getAreaIdByName(areaName);
            if (areaId < 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Area not found. Unable to get Categories for missing Area. " +
                                "Send Area 'all' to retrieve all Categories.");
            }
            String sql = "SELECT DISTINCT category_name FROM categories c " +
                    "JOIN area_category_subcategory acs ON c.id = acs.category_id " +
                    "WHERE acs.area_id = ? " +
                    "ORDER BY category_name";
            results = jdbcTemplate.queryForRowSet(sql, areaId);
        }
        while (results.next()) {
            categoryList.add(results.getString("category_name"));
        }
        return categoryList;
    }

    @Override
    public long addCategory(String categoryName) {
        /*  Don't attempt to add a null or empty name */
        if (categoryName == null || categoryName.trim().equals("")) {
            return -1L;
        }

        /* If category already exists,
           the existing id will be returned */
        Long categoryId = getCategoryIdByName(categoryName);

        /* If category doesn't exist, add it */
        if (categoryId < 0) {
            String sql = "INSERT INTO categories (category_name) VALUES (?) RETURNING id";
            try {
                categoryId = jdbcTemplate.queryForObject(sql, Long.class, categoryName);
            } catch (DataAccessException e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Category failed to be added.");
            }
        }
        return Objects.requireNonNullElse(categoryId, -1L);
    }

    @Override
    public long getCategoryIdByName(String categoryName) {
        Long categoryId;

        /* Use COALESCE to return a value, if name not found,
           so there won't be an exception from queryForObject
           when this method is used to check if a category exists or not. */
        String sql = "SELECT COALESCE(MAX(id),-1) FROM categories WHERE category_name = ?";

        try {
            categoryId = jdbcTemplate.queryForObject(sql, Long.class, categoryName);
        } catch (DataAccessException e) {
            e.printStackTrace();
            /* Don't return -1 if the query failed, because the Category could exist,
                  but an error may have been encountered in retrieving it. */
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Exception checking for Category in database.");
        }
        return Objects.requireNonNullElse(categoryId, -1L);
    }
}
