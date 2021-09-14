package com.bealean.flashcardzap_api.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JdbcAreaDAOTest extends JdbcDAOTest {

    private static AreaDAO areaDAO;

    @BeforeEach
    void configureDatabase() {
        super.configureDatabase();
        areaDAO = new JdbcAreaDAO(jdbcTemplate);
    }

    @Test
    void addArea_validAreaName_areaAddedToDatabase() {
        String expectedAreaName = "JUnit Test Area";
        String actualAreaName = callAddAreaAndQueryDatabaseForAddedName(expectedAreaName);
        assertEquals(expectedAreaName, actualAreaName, "addArea adds valid Area name to database");
    }

    @Test
    void addArea_maxAllowedLength_areaAddedToDatabase() {
        String expectedAreaName = "JUnitArea1JUnitArea2JUnitArea3";
        String actualAreaName = callAddAreaAndQueryDatabaseForAddedName(expectedAreaName);
        assertEquals(expectedAreaName, actualAreaName, "addArea adds Area name of max allowed length to database");
    }

    @Test
    void addArea_allAllowedSpecialCharacters_areaAddedToDatabase() {
        String expectedAreaName = "Area-1_90 zZ~A.";
        String actualAreaName = callAddAreaAndQueryDatabaseForAddedName(expectedAreaName);
        assertEquals(expectedAreaName, actualAreaName, "addArea adds Area name with all allowed special characters to database");
    }

    @Test
    void addArea_duplicateName_returnsExistingIdAndAreaNotAddedToDatabase() {
        String areaName = "JUnit Test Area";
        long expectedAreaId = areaDAO.addArea(areaName);
        long actualAreaId = areaDAO.addArea(areaName);
        assertEquals(expectedAreaId, actualAreaId, "addArea returns existing ID when Area already exists");
        String sql = "SELECT count(area_name) FROM areas WHERE area_name = ?";
        Integer actualAreaCount = jdbcTemplate.queryForObject(sql, Integer.class, areaName);
        assertEquals(Integer.valueOf(1), actualAreaCount, "addArea does not add a duplicate Area to the database");
    }

    @Test
    void addArea_nullName_returnsNegativeIdAndAreaNotAddedToDatabase() {
        String sql = "SELECT count(*) FROM areas";
        Integer expectedAreaCount = jdbcTemplate.queryForObject(sql, Integer.class);
        long areaId = areaDAO.addArea(null);
        Integer actualAreaCount = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(-1L, areaId, "addArea returns -1L for null Area name");
        assertEquals(expectedAreaCount, actualAreaCount, "addArea does not add a null Area to the database");
    }

    @Test
    void addArea_emptyStringName_returnsNegativeIdAndAreaNotAddedToDatabase() {
        String areaName = "";
        String sql = "SELECT count(*) FROM areas";
        Integer expectedAreaCount = jdbcTemplate.queryForObject(sql, Integer.class);
        long areaId = areaDAO.addArea(areaName);
        Integer actualAreaCount = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(-1L, areaId, "addArea returns -1L for an empty String Area name");
        assertEquals(expectedAreaCount, actualAreaCount, "addArea does not add an empty String Area name to the database");
    }

    @Test
    void addArea_failureToAddArea_throwsResponseStatusException() {
        String expectedAreaName = "JunitArea1JunitArea2JunitArea3x";
        Exception exception = assertThrows(ResponseStatusException.class, () -> areaDAO.addArea(expectedAreaName),
                "addArea throws ResponseStatusException for Area name that exceeds the max allowed length");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Area failed to be added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addArea throws exception with expected status" +
                " and message when database insert fails");
    }

    @Test
    void getAreaIdByName_validName_returnsId() {
        String areaName = "JUnit Area";
        long expectedId = addArea(areaName);
        long actualId = areaDAO.getAreaIdByName(areaName);
        assertEquals(expectedId, actualId, "getAreaIdByName returns the expected id for a valid Area name");
    }

    @Test
    void getAreaIdByName_nameHasAllAllowedSpecialCharacters_returnsId() {
        String areaName = "Area-1_90 zZ~A.";
        long expectedId = addArea(areaName);
        long actualId = areaDAO.getAreaIdByName(areaName);
        assertEquals(expectedId, actualId, "getAreaIdByName returns the expected id for an Area name including all allowed special characters");
    }

    @Test
    void getAreaIdByName_areaNotExisting_returnsNegativeId() {
        String areaName = "Junit Not an Area";
        long expectedId = -1L;
        long actualId = areaDAO.getAreaIdByName(areaName);
        assertEquals(expectedId, actualId, "getAreaIdByName returns -1L for an Area that doesn't exist");
    }

    @Test
    void getAreaIdByName_nullArea_returnsNegativeId() {
        long expectedId = -1L;
        long actualId = areaDAO.getAreaIdByName(null);
        assertEquals(expectedId, actualId, "getAreaIdByName returns -1L for a null Area");
    }

    @Test
    void getAreaIdByName_emptyStringArea_returnsNegativeId() {
        long expectedId = -1L;
        long actualId = areaDAO.getAreaIdByName("");
        assertEquals(expectedId, actualId, "getAreaIdByName returns -1L for an empty String Area");
    }

    @Test
    void getAreaIdByName_badDataSourceURL_throwsException() {
        dataSource.destroy();
        isDatabaseConfigured = false;
        configureDatabase();
        dataSource.setUrl("badUrl");
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> areaDAO.getAreaIdByName("JUnitArea"),
                "getAreaIdByName throws ResponseStatusException if query to retrieve ID fails.");
        String expectedMessage = "500 INTERNAL_SERVER_ERROR \"Exception checking for Area in database.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage,
                "getAreaIdByName throws exception with expected status and message if query to retrieve ID fails");
        dataSource.destroy();
        isDatabaseConfigured = false;
        configureDatabase();
    }

    @Test
    void getAreas_validAreasAdded_returnsListOrderedByName() {
        addArea("ZUnit Area");
        addArea("AUnit Area");
        addArea("JUnit Area");
        String sql = "SELECT area_name FROM areas ORDER BY area_name";
        List<String> expectedAreas = new ArrayList<>();
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql);
        while (result.next()) {
            expectedAreas.add(result.getString("area_name"));
        }
        List<String> actualAreas = areaDAO.getAreas();
        assertEquals(expectedAreas, actualAreas,
                "getAreas with Areas added returns list of Areas " +
                        "in ascending order by name");
    }

    @Test
    void getAreas_validAreaAdded_listContainsArea() {
        String areaName = "JUnitArea1";
        assertTrue(addAreaAndCheckIfGetAreasReturnsArea(areaName), "getAreas returns list containing valid test Area");
    }

    @Test
    void getAreas_areaWithAllAllowedSpecialCharactersAdded_listContainsArea() {
        String areaName = "Area-1_90 zZ~A.";
        assertTrue(addAreaAndCheckIfGetAreasReturnsArea(areaName), "getAreas returns list containing test Area with all allowed special characters");
    }

    @Test
    void getAreas_areasAdded_sizeOfAreaListMatchesDatabaseCount() {
        addArea("JUnitArea1");
        addArea("JUnitArea2");
        String sql = "SELECT COUNT(*) FROM areas";
        Integer expectedSize = jdbcTemplate.queryForObject(sql, Integer.class);
        Integer actualSize = areaDAO.getAreas().size();
        assertEquals(expectedSize, actualSize);
    }

    String callAddAreaAndQueryDatabaseForAddedName(String areaName) {
        long areaId = areaDAO.addArea(areaName);
        String actualAreaName = "";
        if (areaId != -1) {
            String sql = "SELECT area_name FROM areas WHERE id = ?";
            actualAreaName = jdbcTemplate.queryForObject(sql, String.class, areaId);
        }
        return actualAreaName;
    }

    boolean addAreaAndCheckIfGetAreasReturnsArea(String areaName) {
        addArea(areaName);
        List<String> areas = areaDAO.getAreas();
        boolean isAreaInList = false;
        if (areas != null) {
            isAreaInList = areas.contains(areaName);
        }
        return isAreaInList;
    }
}