package com.bealean.flashcardzap_api.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Component
public class JdbcAreaCategorySubcategoryDAO implements AreaCategorySubcategoryDAO {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public JdbcAreaCategorySubcategoryDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate, JdbcTemplate jdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean doesMappingExist(Long areaId, Long categoryId, Long subcategoryId) {
        String sql;
        Map<String, Object> params = new HashMap<>();

        if (areaId != null && categoryId != null && subcategoryId != null) {
            sql = "SELECT COUNT(*) FROM area_category_subcategory " +
                    "WHERE area_id = :area_id AND category_id = :category_id " +
                    "AND subcategory_id = :subcategory_id";
            params.put("area_id", areaId);
            params.put("category_id", categoryId);
            params.put("subcategory_id", subcategoryId);

        } else if (areaId != null && categoryId != null) {
            sql = "SELECT COUNT(*) FROM area_category_subcategory " +
                    "WHERE area_id = :area_id AND category_id = :category_id " +
                    "AND subcategory_id IS NULL";
            params.put("area_id", areaId);
            params.put("category_id", categoryId);
        } else {
            return false;
        }

        try {
            Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
            return count != null && count.equals(1);
        } catch (DataAccessException e) {
            System.out.println("Caught Exception: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to check for Area, Category, Subcategory mapping in database.");
        }
    }

    @Override
    public int addMapping(Long areaId, Long categoryId, Long subcategoryId) {
        int result = 0;
        String sql;

        /* Confirm non-null IDs exist before attempting to insert mapping record */
        if (areaId != null) {
            sql = "SELECT COUNT(*) FROM areas WHERE id = ?";
            checkIfIdExists(sql, areaId, "Area");
        }
        if (categoryId != null) {
            sql = "SELECT COUNT(*) FROM categories WHERE id = ?";
            checkIfIdExists(sql, categoryId, "Category");
        }
        if (subcategoryId != null) {
            sql = "SELECT COUNT(*) FROM subcategories WHERE id = ?";
            checkIfIdExists(sql, subcategoryId, "Subcategory");
        }
        /* Don't add mapping if it exists and only add mappings with at least an Area and Category.
           Mappings correspond to filter options. */
        if (!doesMappingExist(areaId, categoryId, subcategoryId) &&
                areaId != null && categoryId != null) {
            sql = "INSERT INTO area_category_subcategory (area_id, category_id, subcategory_id)" +
                    " VALUES (?, ?, ?)";
            try {
                result = jdbcTemplate.update(sql, areaId, categoryId, subcategoryId);
            } catch (DataAccessException e) {
                System.out.println("Caught Exception: " + e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unable to add Area, Category, Subcategory mapping in database.");
            }
        }
        return result;
    }

    void checkIfIdExists(String sql, Long id, String name) {
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
            if (count != null && count.equals(0)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        name + " not found. Unable to add mapping for missing " + name + ".");
            }
        } catch (DataAccessException e) {
            System.out.println("Caught Exception: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to check if " + name + " exists before mapping.");
        }
    }
}
