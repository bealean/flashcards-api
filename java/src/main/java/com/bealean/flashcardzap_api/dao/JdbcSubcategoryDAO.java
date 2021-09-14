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
public class JdbcSubcategoryDAO implements SubcategoryDAO {

    @Autowired
    AreaDAO areaDAO;

    @Autowired
    CategoryDAO categoryDAO;

    private final JdbcTemplate jdbcTemplate;

    public JdbcSubcategoryDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.areaDAO = new JdbcAreaDAO(jdbcTemplate);
        this.categoryDAO = new JdbcCategoryDAO(jdbcTemplate);
    }

    @Override
    public long getSubcategoryIdByName(String subcategoryName) {
        Long subcategoryId;

        /* Use COALESCE to return a value, if name not found,
           so there won't be an exception from queryForObject
           when this method is used to check if a subcategory exists or not. */
        String sql = "SELECT COALESCE(MAX(id),-1) FROM subcategories WHERE subcategory_name = ?";

        try {
            subcategoryId = jdbcTemplate.queryForObject(sql, Long.class, subcategoryName);
        } catch (DataAccessException e) {
            e.printStackTrace();
            /* Don't return -1 if the query failed, because the Subcategory could exist,
                  but an error may have been encountered in retrieving it. */
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Exception checking for Subcategory in database.");
        }
        return Objects.requireNonNullElse(subcategoryId, -1L);
    }

    @Override
    public List<String> getSubcategories(String areaName, String categoryName) {
        List<String> subcategoryList = new ArrayList<>();
        SqlRowSet results;
        String sql;
        Long categoryId = null;
        Long areaId = null;

        if (areaName == null || categoryName == null) {
            if (areaName == null && categoryName == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unable to get Subcategories for null Area and Category. " +
                                "Send 'all' for Area and Category if results should not be filtered.");
            } else if (areaName == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unable to get Subcategories for null Area. " +
                                "Send 'all' for Area if results should not be filtered on Area.");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unable to get Subcategories for null Category. " +
                                "Send 'all' for Category if results should not be filtered on Category.");
            }

        }
        if (categoryName.trim().equalsIgnoreCase("all")) {
            categoryName = "all";
        }
        if (areaName.trim().equalsIgnoreCase("all")) {
            areaName = "all";
        }
        if (!categoryName.equals("all")) {
            categoryId = categoryDAO.getCategoryIdByName(categoryName);
            if (categoryId < 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Category not found. Unable to get Subcategories for missing Category. " +
                                "Send Category 'all' if results should not be filtered " +
                                "based on Category.");
            }
        }

        if (!areaName.equals("all")) {
            areaId = areaDAO.getAreaIdByName(areaName);
            if (areaId < 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Area not found. Unable to get Subcategories for missing Area. " +
                                "Send Area 'all' if results should not be filtered " +
                                "based on Area.");
            }
        }

        if (categoryName.equals("all") &&
                areaName.equals("all")) {
            /* Subcategories table has UNIQUE constraint on subcategory_name.
             Case variations are allowed. If user decides to use a different case, it will be possible
             to update cards to new case and old case will be automatically removed when it is no longer
             referenced by any cards. */
            sql = "SELECT subcategory_name FROM subcategories ORDER BY subcategory_name";
            results = jdbcTemplate.queryForRowSet(sql);
        } else if (areaName.equals("all")) {
            sql = "SELECT DISTINCT subcategory_name FROM subcategories s " +
                    "JOIN area_category_subcategory acs ON s.id = acs.subcategory_id " +
                    "WHERE acs.category_id = ? " +
                    "ORDER BY subcategory_name";
            results = jdbcTemplate.queryForRowSet(sql, categoryId);
        } else if (categoryName.equals("all")) {
            sql = "SELECT DISTINCT subcategory_name FROM subcategories s " +
                    "JOIN area_category_subcategory acs ON s.id = acs.subcategory_id " +
                    "WHERE acs.area_id = ? " +
                    "ORDER BY subcategory_name";
            results = jdbcTemplate.queryForRowSet(sql, areaId);
        } else {
            sql = "SELECT subcategory_name FROM subcategories s " +
                    "JOIN area_category_subcategory acs ON s.id = acs.subcategory_id " +
                    "WHERE acs.area_id = ? AND acs.category_id = ? " +
                    "ORDER BY subcategory_name";
            results = jdbcTemplate.queryForRowSet(sql, areaId, categoryId);
        }
        while (results.next()) {
            subcategoryList.add(results.getString("subcategory_name"));
        }
        return subcategoryList;
    }

    @Override
    public long addSubcategory(String subcategoryName) {
        if (subcategoryName == null || subcategoryName.trim().equals("")) {
            return -1L;
        }
        Long subcategoryId = getSubcategoryIdByName(subcategoryName);
        if (subcategoryId < 0) {
            String sql = "INSERT INTO subcategories (subcategory_name) VALUES (?) RETURNING id";
            try {
                subcategoryId = jdbcTemplate.queryForObject(sql, Long.class, subcategoryName);
            } catch (DataAccessException e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Subcategory failed to be added.");
            }
        }
        return Objects.requireNonNullElse(subcategoryId, -1L);
    }

}
