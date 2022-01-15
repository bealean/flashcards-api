package com.bealean.flashcardzap_api.dao;

import com.bealean.flashcardzap_api.utility.InputScrubber;
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
public class JdbcAreaDAO implements AreaDAO {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAreaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long addArea(String areaName) {
        areaName = InputScrubber.trimStringAndSetEmptyToNull(areaName);
        /* Don't attempt to add a null name */
        if (areaName == null) {
            return -1L;
        }

        /* This call will return -1 if the area doesn't already exist.
           If the area already exists, store the existing id to return
           and don't add duplicate area. */
        long areaId = getAreaIdByName(areaName);

        if (areaId < 0) {
            String sql = "INSERT INTO areas (area_name) VALUES (?) RETURNING id";
            try {
                areaId = Objects.requireNonNullElse(jdbcTemplate.queryForObject(sql, Long.class, areaName), -1L);
            } catch (DataAccessException e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Area failed to be added.");
            }
        }
        return areaId;
    }

    @Override
    public long getAreaIdByName(String areaName) {
        long areaId = -1L;

        /* Use COALESCE to return a value, if name not found,
           so there won't be an exception from queryForObject
           when this method is used to check if an area exists or not. */
        String sql = "SELECT COALESCE(MAX(id),-1) FROM areas WHERE area_name = ?";

        /* Don't try to get id of null or empty String area names,
           even if they exist */
        areaName = InputScrubber.trimStringAndSetEmptyToNull(areaName);
        if (areaName != null) {
            try {
                areaId = Objects.requireNonNullElse(jdbcTemplate.queryForObject(sql, Long.class, areaName), -1L);
            } catch (DataAccessException e) {
                e.printStackTrace();
                /* Don't return -1 if the query failed, because the Area could exist,
                   but an error may have been encountered in retrieving it. */
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Exception checking for Area in database.");
            }
        }
        return areaId;
    }
/* Not providing a method to get Areas for a Category and/or Subcategory for now. */
    @Override
    public List<String> getAreas() {
        List<String> areaList = new ArrayList<>();
        /* Areas table has UNIQUE constraint on area_name.
         Case variations are allowed. If user decides to use a different case, it will be possible
         to update cards to new case and old case will be automatically removed when it is no longer
         referenced by any cards. */
        String sql = "SELECT area_name FROM areas ORDER BY area_name";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

        while (results.next()) {
            areaList.add(results.getString("area_name"));
        }
        return areaList;
    }
}
