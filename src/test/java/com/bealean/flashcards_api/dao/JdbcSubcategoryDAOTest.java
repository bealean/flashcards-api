package com.bealean.flashcardzap_api.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcSubcategoryDAOTest extends JdbcDAOTest {

    private static SubcategoryDAO subcategoryDAO;

    @BeforeEach
    void configureDatabase() {
        super.configureDatabase();
        subcategoryDAO = new JdbcSubcategoryDAO(jdbcTemplate);
    }

    @Test
    void getSubcategoryIdByName_existingSubcategory_returnsExpectedId() {
        String subcategoryName = "Junit Subcategory";
        Long expectedId = addSubcategory(subcategoryName);
        Long actualId = subcategoryDAO.getSubcategoryIdByName(subcategoryName);
        assertEquals(expectedId, actualId,
                "getSubcategoryIdByName with an existing Subcategory returns expected ID");
    }

    @Test
    void getSubcategoryIdByName_nameHasAllAllowedSpecialCharacters_returnsExpectedId() {
        String subcategoryName = "Subcat-1_90 zZ~A.";
        Long expectedId = addSubcategory(subcategoryName);
        Long actualId = subcategoryDAO.getSubcategoryIdByName(subcategoryName);
        assertEquals(expectedId, actualId,
                "getSubcategoryIdByName returns the expected id for a Subcategory name " +
                        "including all allowed special characters");
    }

    @Test
    void getSubcategoryIdByName_spaceAroundName_returnsIdForTrimmedName() {
        String subcategoryName = " JUnit Subcategory ";
        Long expectedId = addSubcategory(subcategoryName.trim());
        Long actualId = subcategoryDAO.getSubcategoryIdByName(subcategoryName);
        assertEquals(expectedId, actualId,
                "getSubcategoryIdByName returns the id for the trimmed Subcategory name");
    }

    @Test
    void getSubcategoryIdByName_nullSubcategory_returnsNegativeId() {
        Long expectedId = -1L;
        Long actualId = subcategoryDAO.getSubcategoryIdByName(null);
        assertEquals(expectedId, actualId,
                "getSubcategoryIdByName returns -1L for a null Subcategory name.");
    }

    @Test
    void getSubcategoryIdByName_emptyStringSubcategory_returnsNegativeId() {
        Long expectedId = -1L;
        Long actualId = subcategoryDAO.getSubcategoryIdByName("");
        assertEquals(expectedId, actualId,
                "getSubcategoryIdByName returns -1L for an empty String Subcategory name.");
    }

    @Test
    void getSubcategoryIdByName_nonexistentSubcategory_returnsNegativeId() {
        String subcategoryName = "Junit Subcategory";
        Long expectedId = -1L;
        Long actualId = subcategoryDAO.getSubcategoryIdByName(subcategoryName);
        assertEquals(expectedId, actualId,
                "getSubcategoryIdByName with nonexistent Subcategory returns -1L");
    }

    @Test
    void getSubcategoryIdByName_badDataSourceURL_throwsException() {
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
        dataSource.setUrl("badUrl");
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> subcategoryDAO.getSubcategoryIdByName("JUnit Subcategory"),
                "getSubcategoryIdByName throws ResponseStatusException if query to retrieve ID fails");
        String expectedMessage = "500 INTERNAL_SERVER_ERROR \"Exception checking for Subcategory in database.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage,
                "getSubcategoryIdByName throws exception with expected status and message if query to retrieve ID fails");
        destroyDataSourceAndSetDBConfigFlagFalse();
        /* Need to reconfigure DB here instead of in next test because AfterEach
         executes rollback on the data source connection. */
        configureDatabase();
    }

    @Test
    void getSubcategories_nullAreaAndCategory_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> subcategoryDAO.getSubcategories(null, null),
                "getSubcategories with null Category throws ResponseStatusException");
        String expectedMessage = "400 BAD_REQUEST \"Unable to get Subcategories for null Area and Category. Send 'all' for Area and Category if results should not be filtered.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getSubcategories with null Area and Category " +
                "throws exception with expected status and message");
    }

    @Test
    void getSubcategories_nullArea_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> subcategoryDAO.getSubcategories(null, "all"),
                "getSubcategories with null Category throws ResponseStatusException");
        String expectedMessage = "400 BAD_REQUEST \"Unable to get Subcategories for null Area. Send 'all' for Area if results should not be filtered on Area.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getSubcategories with null Area " +
                "throws exception with expected status and message");
    }

    @Test
    void getSubcategories_nullCategory_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> subcategoryDAO.getSubcategories("all", null),
                "getSubcategories with null Category throws ResponseStatusException");
        String expectedMessage = "400 BAD_REQUEST \"Unable to get Subcategories for null Category. Send 'all' for Category if results should not be filtered on Category.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getSubcategories with null Category " +
                "throws exception with expected status and message");
    }

    @Test
    void getSubcategories_nonexistentCategory_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> subcategoryDAO.getSubcategories("all", "Not a Category"),
                "getSubcategories with nonexistent Category throws ResponseStatusException");
        String expectedMessage = "404 NOT_FOUND \"Category not found. Unable to get Subcategories for missing Category. Send Category 'all' if results should not be filtered based on Category.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getSubcategories with nonexistent Category " +
                "throws exception with expected status and message");
    }

    @Test
    void getSubcategories_nonexistentArea_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> subcategoryDAO.getSubcategories("Not an Area", "all"),
                "getSubcategories with nonexistent Area throws ResponseStatusException");
        String expectedMessage = "404 NOT_FOUND \"Area not found. Unable to get Subcategories for missing Area. Send Area 'all' if results should not be filtered based on Area.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getSubcategories with nonexistent Area " +
                "throws exception with expected status and message");
    }

    @Test
    void getSubcategories_lowercaseAllCategoriesLowercaseAllAreas_returnsListSizeEqualToDistinctCountOfNamesInSubcategoriesTable() {
        addUnmappedSubcategoriesAndMultipleMappingSubcategoryAndMappingWithNullSubcategory();
        Integer expectedSubcategoryCount = getSubcategoryCount();
        Integer actualSubcategoryCount = subcategoryDAO.getSubcategories("all", "all").size();
        assertEquals(expectedSubcategoryCount, actualSubcategoryCount, "getSubcategories with lowercase 'all' specified for Area " +
                "and Category returns a list with size equal to the distinct count of subcategory names in the database");
    }

    @Test
    void getSubcategories_uppercaseAllCategoriesUppercaseAllAreas_returnsListSizeEqualToDistinctCountOfNamesInSubcategoriesTable() {
        addUnmappedSubcategoriesAndMultipleMappingSubcategoryAndMappingWithNullSubcategory();
        Integer expectedSubcategoryCount = getSubcategoryCount();
        Integer actualSubcategoryCount = subcategoryDAO.getSubcategories("ALL", "ALL").size();
        assertEquals(expectedSubcategoryCount, actualSubcategoryCount, "getSubcategories with uppercase 'ALL' specified for Area " +
                "and Category returns a list with size equal to the distinct count of subcategory names in the database");
    }

    @Test
    void getSubcategories_pascalCaseAllCategoriesPascalCaseAllAreas_returnsListSizeEqualToDistinctCountOfNamesInSubcategoriesTable() {
        addUnmappedSubcategoriesAndMultipleMappingSubcategoryAndMappingWithNullSubcategory();
        Integer expectedSubcategoryCount = getSubcategoryCount();
        Integer actualSubcategoryCount = subcategoryDAO.getSubcategories("All", "All").size();
        assertEquals(expectedSubcategoryCount, actualSubcategoryCount, "getSubcategories with Pascal case 'All' specified for Area " +
                "and Category returns a list with size equal to the distinct count of subcategory names in the database");
    }

    @Test
    void getSubcategories_allCategoriesAllAreas_returnsAllDistinctSubcategoriesOrderedAlphabeticallyByName() {
        addUnmappedSubcategoriesAndMultipleMappingSubcategoryAndMappingWithNullSubcategory();
        /* Specifying DISTINCT here, even though Subcategories table has
        UNIQUE constraint on subcategory_name. */
        String sql = "SELECT DISTINCT subcategory_name FROM subcategories ORDER BY subcategory_name";
        List<String> expectedSubcategories = new ArrayList<>();
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql);
        while (result.next()) {
            expectedSubcategories.add(result.getString("subcategory_name"));
        }
        List<String> actualSubcategories = subcategoryDAO.getSubcategories("all", "all");
        assertIterableEquals(expectedSubcategories, actualSubcategories,
                "getSubcategories for All Areas and Categories " +
                        "returns all distinct Subcategories ordered alphabetically by name");
    }

    @Test
    void getSubcategories_specificCategoryAllAreas_returnsDistinctSubcategoriesForCategoryInAscendingOrderByName() {
        // Add an unmapped Subcategory, which should not be returned for a specific category
        addSubcategory("JUnit Unmapped Subcategory");

        // Add a subcategory associated with the Test Category and first Area
        addAreaCategoryAndSubcategoryAndMap("JUnit Test", "JUnit SQL", "JUnit DML");

        // Add a subcategory for a category other than the Test Category
        addAreaCategoryAndSubcategoryAndMap("JUnit Dev", "JUnit Java", "JUnit Syntax");

        /* Add a second subcategory associated with the Test Category and a second Area
         Add Area "JUnit Dev", Category "JUnit SQL", Subcategory "JUnit DDL" */
        Long subcategoryId = addSubcategory("JUnit DDL");
        String sql = "SELECT id FROM categories WHERE category_name = ?";
        Long categoryId = jdbcTemplate.queryForObject(sql, Long.class, "JUnit SQL");
        sql = "SELECT id FROM areas WHERE area_name = ?";
        Long areaId = jdbcTemplate.queryForObject(sql, Long.class, "JUnit Dev");
        addMapping(areaId, categoryId, subcategoryId);

        /* Add a record for a subcategory that is already associated with the Test category
        but with a different Area to check that the list only includes the subcategory once.
        Add Area "JUnit Dev", Category "JUnit SQL", Subcategory "JUnit DML" */
        sql = "SELECT id FROM subcategories WHERE subcategory_name = ?";
        subcategoryId = jdbcTemplate.queryForObject(sql, Long.class, "JUnit DML");
        addMapping(areaId, categoryId, subcategoryId);

        // Add a mapping for the test Category with a null subcategory
        addMapping(areaId, categoryId, null);

        List<String> expectedSubcategories = new ArrayList<>();
        // DDL is before DML alphabetically, where DML was added first
        expectedSubcategories.add("JUnit DDL");
        expectedSubcategories.add("JUnit DML");
        List<String> actualSubcategories = subcategoryDAO.getSubcategories("all", "JUnit SQL");
        assertIterableEquals(expectedSubcategories, actualSubcategories,
                "getSubcategories with specific Category and all Areas " +
                        "returns all distinct Subcategories in ascending order by name " +
                        "for only the specified Category");
    }

    @Test
    void getSubcategories_specificAreaAllCategories_returnsDistinctSubcategoriesForAreaInAscendingOrderByName() {
        // Add an unmapped Subcategory, which should not be returned for a specific area
        addSubcategory("JUnit Unmapped Subcategory");

        // Add first subcategory for the test Area and the first category
        addAreaCategoryAndSubcategoryAndMap("JUnit Dev", "JUnit Java", "JUnit Syntax");

        /* Add a new subcategory for a different Area and a Category that will be mapped to the test Area
        Add subcategory "JUnit DML" to category "JUnit SQL" and area "JUnit Test." */
        addAreaCategoryAndSubcategoryAndMap("JUnit Test", "JUnit SQL", "JUnit DML");

        /* Add first subcategory for the test Area to a second category to check
        that the list only includes the subcategory once.
        Add subcategory "JUnit Syntax" to category "JUnit SQL" and area "JUnit Dev" */
        String sql = "SELECT id FROM subcategories WHERE subcategory_name = ?";
        Long subcategoryId = jdbcTemplate.queryForObject(sql, Long.class, "JUnit Syntax");
        sql = "SELECT id FROM categories WHERE category_name = ?";
        Long categoryId = jdbcTemplate.queryForObject(sql, Long.class, "JUnit SQL");
        sql = "SELECT id FROM areas WHERE area_name = ?";
        Long areaId = jdbcTemplate.queryForObject(sql, Long.class, "JUnit Dev");
        addMapping(areaId, categoryId, subcategoryId);

        /* Add second subcategory for the test Area and the category that is mapped to another Area
        Add subcategory "JUnit DDL" to category "JUnit SQL" and area "JUnit Dev." */
        subcategoryId = addSubcategory("JUnit DDL");
        addMapping(areaId, categoryId, subcategoryId);

        // Add mapping of test Area and a category with a null subcategory.
        addMapping(areaId, categoryId, null);

        List<String> expectedSubcategories = new ArrayList<>();
        // DDL is before Syntax alphabetically, where Syntax was added first
        expectedSubcategories.add("JUnit DDL");
        expectedSubcategories.add("JUnit Syntax");
        List<String> actualSubcategories = subcategoryDAO.getSubcategories("JUnit Dev", "all");
        assertIterableEquals(expectedSubcategories, actualSubcategories,
                "getSubcategories with specific Area and all Categories " +
                        "returns all distinct Subcategories in ascending order by name " +
                        "for only the specified Area");
    }

    @Test
    void getSubcategories_specificAreaSpecificCategory_returnsDistinctSubcategoriesForAreaAndCategoryInAscendingOrderByName() {
        addAreaCategoryAndSubcategoryAndMap("JUnit Dev", "JUnit SQL", "JUnit DML");

        // Add a different Subcategory for the same test Area and Category
        String sql = "SELECT id FROM areas WHERE area_name = ?";
        Long testAreaId = jdbcTemplate.queryForObject(sql, Long.class, "JUnit Dev");
        sql = "SELECT id FROM categories WHERE category_name = ?";
        Long testCategoryId = jdbcTemplate.queryForObject(sql, Long.class, "JUnit SQL");
        long subcategoryId = addSubcategory("JUnit DDL");
        addMapping(testAreaId, testCategoryId, subcategoryId);

        // Add a mapping of the test Area and Category with a null Subcategory
        addMapping(testAreaId, testCategoryId, null);

        // Add unmapped Subcategory
        addSubcategory("JUnit Unmapped Sub");

        // Add a mapping for a different Area, Category, and Subcategory
        addAreaCategoryAndSubcategoryAndMap("JUnit Test", "JUnit UFT", "JUnit OTA");

        // Add a Subcategory mapping for the test Area and a Category that is not the test Category
        long nonTestCategory = addCategory("JUnit Not Test Cat");
        subcategoryId = addSubcategory("JUnit Other Cat Sub");
        addMapping(testAreaId, nonTestCategory, subcategoryId);

        // Add a Subcategory mapping for the test Category and an Area that is not the test Area
        long nonTestArea = addArea("JUnit Not Test Area");
        subcategoryId = addSubcategory("JUnit Other Area Sub");
        addMapping(nonTestArea, testCategoryId, subcategoryId);

        List<String> expectedSubcategories = new ArrayList<>();
        // DDL is before DML alphabetically, where DML was added first
        expectedSubcategories.add("JUnit DDL");
        expectedSubcategories.add("JUnit DML");
        List<String> actualSubcategories = subcategoryDAO.getSubcategories("JUnit Dev", "JUnit SQL");
        assertIterableEquals(expectedSubcategories, actualSubcategories,
                "getSubcategories with specific Area and Category " +
                        "returns all distinct Subcategories in ascending order by name " +
                        "for only the specified Area and Category");
    }

    @Test
    void getSubcategories_extraSpaceAroundAreaAndCategoryNames_returnsSubcategoriesForTrimmedNames() {
        String untrimmedArea = " JUnit Dev ";
        String untrimmedCategory = " JUnit SQL ";
        addAreaCategoryAndSubcategoryAndMap(untrimmedArea.trim(), untrimmedCategory.trim(), "JUnit DML");

        List<String> expectedSubcategories = new ArrayList<>();
        expectedSubcategories.add("JUnit DML");
        List<String> actualSubcategories = subcategoryDAO.getSubcategories(untrimmedArea, untrimmedCategory);
        assertIterableEquals(expectedSubcategories, actualSubcategories,
                "getSubcategories with extra space around Area and Category names " +
                        "returns subcategory for trimmed Area and Category names");
    }

    @Test
    void getSubcategories_AreaWithNoSubcategories_returnsEmptyList() {
        String areaName = "JUnit Area";
        addArea(areaName);
        int expectedListSize = 0;
        int actualListSize = subcategoryDAO.getSubcategories(areaName, "all").size();
        assertEquals(expectedListSize, actualListSize, "getSubcategories for an Area with no Subcategories " +
                "returns an empty list");
    }

    @Test
    void addSubcategory_validSubcategoryName_subcategoryAddedToDatabase() {
        String expectedSubcategoryName = "JUnit Test Subcategory";
        String actualSubcategoryName = addSubcategoryAndReturnNameFromDatabase(expectedSubcategoryName);
        assertEquals(expectedSubcategoryName, actualSubcategoryName,
                "addSubcategory adds valid Subcategory name to database");
    }

    @Test
    void addSubcategory_maxAllowedLength_subcategoryAddedToDatabase() {
        String expectedSubcategoryName = "JUnit Subcategory Subcategoryx";
        String actualSubcategoryName = addSubcategoryAndReturnNameFromDatabase(expectedSubcategoryName);
        assertEquals(expectedSubcategoryName, actualSubcategoryName, "addSubcategory adds Subcategory name of max allowed length to database");
    }

    @Test
    void addSubcategory_allAllowedSpecialCharacters_subcategoryAddedToDatabase() {
        String expectedSubcategoryName = "JUnit Sub-1_90 zZ~A.";
        String actualSubcategoryName = addSubcategoryAndReturnNameFromDatabase(expectedSubcategoryName);
        assertEquals(expectedSubcategoryName, actualSubcategoryName, "addSubcategory adds Subcategory name with all allowed special characters to database");
    }

    @Test
    void addSubcategory_spaceAroundName_trimmedNameAddedToDatabase() {
        String untrimmedSubcategoryName = " JUnit Subcategory ";
        String actualSubcategoryName = addSubcategoryAndReturnNameFromDatabase(untrimmedSubcategoryName);
        String expectedSubcategoryName = untrimmedSubcategoryName.trim();
        assertEquals(expectedSubcategoryName, actualSubcategoryName, "addSubcategory adds trimmed Subcategory name to database");
    }

    @Test
    void addSubcategory_duplicateName_returnsExistingIdAndSubcategoryNotAddedToDatabase() {
        String subcategoryName = "JUnit Test Subcategory";
        long expectedSubcategoryId = subcategoryDAO.addSubcategory(subcategoryName);
        long actualSubcategoryId = subcategoryDAO.addSubcategory(subcategoryName);
        assertEquals(expectedSubcategoryId, actualSubcategoryId,
                "addSubcategory returns existing id when Subcategory already exists");
        String sql = "SELECT count(subcategory_name) FROM subcategories WHERE subcategory_name = ?";
        Integer actualSubcategoryCount = jdbcTemplate.queryForObject(sql, Integer.class, subcategoryName);
        assertEquals(Integer.valueOf(1), actualSubcategoryCount,
                "addSubcategory does not add a duplicate Subcategory to the database");
    }

    @Test
    void addSubcategory_nullName_returnsNegativeIdAndSubcategoryNotAddedToDatabase() {
        Integer expectedSubcategoryCount = getSubcategoryCount();
        long subcategoryId = subcategoryDAO.addSubcategory(null);
        Integer actualSubcategoryCount = getSubcategoryCount();
        assertEquals(-1L, subcategoryId, "addSubcategory returns -1L for null Subcategory name");
        assertEquals(expectedSubcategoryCount, actualSubcategoryCount,
                "addSubcategory does not add a null Subcategory to the database");
    }

    @Test
    void addSubcategory_emptyStringName_returnsNegativeIdAndSubcategoryNotAddedToDatabase() {
        String subcategoryName = "";
        Integer expectedSubcategoryCount = getSubcategoryCount();
        long subcategoryId = subcategoryDAO.addSubcategory(subcategoryName);
        Integer actualSubcategoryCount = getSubcategoryCount();
        assertEquals(-1L, subcategoryId, "addSubcategory returns -1L for an empty String Subcategory name");
        assertEquals(expectedSubcategoryCount, actualSubcategoryCount,
                "addSubcategory does not add an empty String Subcategory name to the database");
    }

    @Test
    void addSubcategory_failureToAddSubcategory_throwsResponseStatusException() {
        String expectedSubcategoryName = "JUnit Subcategory SubcategoryxE";
        Exception exception = assertThrows(ResponseStatusException.class, () -> subcategoryDAO.addSubcategory(expectedSubcategoryName),
                "addSubcategory throws ResponseStatusException for Subcategory name that exceeds the max allowed length");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Subcategory failed to be added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addSubcategory throws exception with expected status and message " +
                "when database insert fails");
    }

    private void addAreaCategoryAndSubcategoryAndMap(String areaName, String categoryName, String subcategoryName) {
        Long areaId = addArea(areaName);
        Long categoryId = addCategory(categoryName);
        Long subcategoryId = addSubcategory(subcategoryName);
        addMapping(areaId, categoryId, subcategoryId);
    }

    String addSubcategoryAndReturnNameFromDatabase(String subcategoryName) {
        long subcategoryId = subcategoryDAO.addSubcategory(subcategoryName);
        String actualSubcategoryName = "";
        if (subcategoryId != -1L) {
            String sql = "SELECT subcategory_name FROM subcategories WHERE id = ?";
            actualSubcategoryName = jdbcTemplate.queryForObject(sql, String.class, subcategoryId);
        }
        return actualSubcategoryName;
    }

    Integer getSubcategoryCount() {
        String sql = "SELECT COUNT(DISTINCT subcategory_name) FROM subcategories";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    void addUnmappedSubcategoriesAndMultipleMappingSubcategoryAndMappingWithNullSubcategory() {
        /*  This data ensures there are unmapped subcategories in the database
        and that there is at least one subcategory with multiple mappings.
        TD (TestData) prefix is used to avoid conflicts when test data is added
        for individual tests. */

        /* Unmapped Subcategories that have a record in the Subcategories table,
        but no mapping record in the area_category_subcategory table. */
        addSubcategory("TD jUnit Subcategory");
        addSubcategory("TD Another JUnit Subcategory");

        /* Subcategory with multiple mappings. Subcategory "TD JUnitDML" is mapped
        to Areas "TD JUnitDev" and "TD JUnitTest." Subcategory has a record in the Subcategories table
        and two records in the area_category_subcategory table. */
        long areaId = addArea("TD JUnitDev");
        long categoryId = addCategory("TD JUnitSQL");
        long subcategoryId = addSubcategory("TD JUnitDML");
        addMapping(areaId, categoryId, subcategoryId);
        areaId = addArea("TD JUnitTest");
        addMapping(areaId, categoryId, subcategoryId);

        // Add Area/Category mapping with null Subcategory
        addMapping(areaId, categoryId, null);
    }

}