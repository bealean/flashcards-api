package com.bealean.flashcards_api.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class JdbcAreaCategorySubcategoryDAOTest extends JdbcDAOTest {

    private static NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static AreaCategorySubcategoryDAO areaCategorySubcategoryDAO;

    @BeforeEach
    void configureDatabase() {
        super.configureDatabase();
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        areaCategorySubcategoryDAO = new JdbcAreaCategorySubcategoryDAO(namedParameterJdbcTemplate, jdbcTemplate);
    }

    @Test
    void doesMappingExist_allFieldsMapped_returnsTrue() {
        Long areaId = addArea("Junit Area");
        Long categoryId = addCategory("JUnit Category");
        Long subcategoryId = addSubcategory("JUnit Subcategory");
        addMapping(areaId, categoryId, subcategoryId);
        assertTrue(areaCategorySubcategoryDAO.doesMappingExist(areaId, categoryId, subcategoryId),
                "doesMappingExist returns true for existing mapping with all fields");
    }

    @Test
    void doesMappingExist_allFieldsUnmapped_returnsFalse() {
        Long areaId = addArea("Junit Area");
        Long categoryId = addCategory("JUnit Category");
        Long subcategoryId = addSubcategory("JUnit Subcategory");
        assertFalse(areaCategorySubcategoryDAO.doesMappingExist(areaId, categoryId, subcategoryId),
                "doesMappingExist returns false for mapping with all fields that does not exist");
    }

    @Test
    void doesMappingExist_areaAndCategoryMapped_returnsTrue() {
        Long areaId = addArea("Junit Area");
        Long categoryId = addCategory("JUnit Category");
        addMapping(areaId, categoryId, null);
        assertTrue(areaCategorySubcategoryDAO.doesMappingExist(areaId, categoryId, null),
                "doesMappingExist returns true for existing mapping with Area and Category only");
    }

    @Test
    void doesMappingExist_areaAndCategoryMappedUnmapped_returnsFalse() {
        Long areaId = addArea("Junit Area");
        Long categoryId = addCategory("JUnit Category");
        assertFalse(areaCategorySubcategoryDAO.doesMappingExist(areaId, categoryId, null),
                "doesMappingExist returns false for mapping with Area and Category only that does not exist");
    }

    @Test
    void doesMappingExist_areaOnly_returnsFalse() {
        Long areaId = addArea("Junit Area");
        assertFalse(areaCategorySubcategoryDAO.doesMappingExist(areaId, null, null),
                "doesMappingExist returns false with Area only");
    }

    @Test
    void doesMappingExist_categoryOnly_returnsFalse() {
        Long categoryId = addCategory("JUnit Category");
        assertFalse(areaCategorySubcategoryDAO.doesMappingExist(null, categoryId, null),
                "doesMappingExist returns false with Category only");
    }

    @Test
    void doesMappingExist_subcategoryOnly_returnsFalse() {
        Long subcategoryId = addSubcategory("JUnit Subcategory");
        assertFalse(areaCategorySubcategoryDAO.doesMappingExist(null, subcategoryId, null),
                "doesMappingExist returns false with Subcategory only");
    }

    @Test
    void doesMappingExist_allNull_returnsFalse() {
        assertFalse(areaCategorySubcategoryDAO.doesMappingExist(null, null, null),
                "doesMappingExist returns false with all nulls");
    }

    @Test
    void doesMappingExist_badDataSourceURL_throwsException() {
        Long areaId = addArea("Junit Area");
        Long categoryId = addCategory("JUnit Category");
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
        dataSource.setUrl("badUrl");
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> areaCategorySubcategoryDAO.doesMappingExist(areaId, categoryId, null),
                "doesMappingExist throws ResponseStatusException if query to check mapping fails");
        String expectedMessage = "500 INTERNAL_SERVER_ERROR \"Unable to check for Area, Category, Subcategory mapping in database.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage,
                "doesMappingExist throws exception with expected status and message if query to retrieve ID fails");
        destroyDataSourceAndSetDBConfigFlagFalse();
        /* Need to reconfigure DB here instead of in next test because AfterEach
         executes rollback on the data source connection. */
        configureDatabase();
    }

    @Test
    void addMapping_areaIdNotFound_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> areaCategorySubcategoryDAO.addMapping(-1L, null, null),
                "addMapping throws ResponseStatusException if provided Area Id does not exist.");
        String expectedMessage = "404 NOT_FOUND \"Area not found. Unable to add mapping for missing Area.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage,
                "addMapping throws exception with expected status and message if provided Area Id does not exist");
    }

    @Test
    void addMapping_categoryIdNotFound_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> areaCategorySubcategoryDAO.addMapping(null, -1L, null),
                "addMapping throws ResponseStatusException if provided Category Id does not exist.");
        String expectedMessage = "404 NOT_FOUND \"Category not found. Unable to add mapping for missing Category.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage,
                "addMapping throws exception with expected status and message if provided Category Id does not exist");
    }

    @Test
    void addMapping_subcategoryIdNotFound_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> areaCategorySubcategoryDAO.addMapping(null, null, -1L),
                "addMapping throws ResponseStatusException if provided Subcategory Id does not exist.");
        String expectedMessage = "404 NOT_FOUND \"Subcategory not found. Unable to add mapping for missing Subcategory.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage,
                "addMapping throws exception with expected status and message if provided Subcategory Id does not exist");
    }

    @Test
    void addMapping_iDCheckFailure_throwsException() {
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
        dataSource.setUrl("badUrl");
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> areaCategorySubcategoryDAO.addMapping(-1L, null, null),
                "addMapping throws ResponseStatusException if check for existence of IDs fails");
        String expectedMessage = "500 INTERNAL_SERVER_ERROR \"Unable to check if Area exists before mapping.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage,
                "addMapping throws exception with expected status and message if check for existence of IDs fails");
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
    }

    @Test
    void addMapping_duplicateMappingNullSubcategory_returns0AndRecordNotAdded() {
        Long areaId = addArea("JUnit Area");
        Long categoryId = addCategory("JUnit Category");
        addMapping(areaId, categoryId, null);
        String sql = "SELECT COUNT(*) FROM area_category_subcategory";
        Integer expectedCount = jdbcTemplate.queryForObject(sql, Integer.class);
        int actualInsertedMappings = areaCategorySubcategoryDAO.addMapping(areaId, categoryId, null);
        assertEquals(0, actualInsertedMappings, "addMapping returns 0 for an Area and Category mapping " +
                "that already exists");
        Integer actualCount = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(expectedCount, actualCount, "addMapping does not add a new mapping record for an " +
                "Area and Category mapping that already exists");
    }

    @Test
    void addMapping_duplicateMappingAllFields_returns0AndRecordNotAdded() {
        Long areaId = addArea("JUnit Area");
        Long categoryId = addCategory("JUnit Category");
        Long subcategoryId = addSubcategory("JUnit Subcategory");
        addMapping(areaId, categoryId, subcategoryId);
        String sql = "SELECT COUNT(*) FROM area_category_subcategory";
        Integer expectedCount = jdbcTemplate.queryForObject(sql, Integer.class);
        int actualInsertedMappings = areaCategorySubcategoryDAO.addMapping(areaId, categoryId, subcategoryId);
        assertEquals(0, actualInsertedMappings, "addMapping returns 0 for an Area, Category, and Subcategory mapping " +
                "that already exists");
        Integer actualCount = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(expectedCount, actualCount, "addMapping does not add a new mapping record for an " +
                "Area, Category, and Subcategory mapping that already exists");
    }

    @Test
    void addMapping_nullArea_returns0AndRecordNotAdded() {
        Long categoryId = addCategory("JUnit Category");
        Long subcategoryId = addSubcategory("JUnit Subcategory");
        String sql = "SELECT COUNT(*) FROM area_category_subcategory";
        Integer expectedCount = jdbcTemplate.queryForObject(sql, Integer.class);
        int actualInsertedMappings = areaCategorySubcategoryDAO.addMapping(null, categoryId, subcategoryId);
        assertEquals(0, actualInsertedMappings, "addMapping returns 0 for an null Area");
        Integer actualCount = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(expectedCount, actualCount, "addMapping does not add a new mapping record for a " +
                "null Area");
    }

    @Test
    void addMapping_nullCategory_returns0AndRecordNotAdded() {
        Long areaId = addArea("JUnit Area");
        Long subcategoryId = addSubcategory("JUnit Subcategory");
        String sql = "SELECT COUNT(*) FROM area_category_subcategory";
        Integer expectedCount = jdbcTemplate.queryForObject(sql, Integer.class);
        int actualInsertedMappings = areaCategorySubcategoryDAO.addMapping(areaId, null, subcategoryId);
        assertEquals(0, actualInsertedMappings, "addMapping returns 0 for an null Category");
        Integer actualCount = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(expectedCount, actualCount, "addMapping does not add a new mapping record for a " +
                "null Category");
    }

    @Test
    void addMapping_AreaCategoryNullSubcategory_returns1AndRecordAdded() {
        Long areaId = addArea("JUnit Area");
        Long categoryId = addCategory("JUnit Category");
        String sql = "SELECT COUNT(*) FROM area_category_subcategory " +
                "WHERE area_id = ? AND category_id = ? AND " +
                "subcategory_id IS NULL";
        Integer preCount = jdbcTemplate.queryForObject(sql, Integer.class, areaId, categoryId);
        int returnValue = areaCategorySubcategoryDAO.addMapping(areaId, categoryId, null);
        assertEquals(1, returnValue, "addMapping for existing Area and Category, and null Subcategory, " +
                "where the mapping does not exist, returns 1");
        Integer postCount = jdbcTemplate.queryForObject(sql, Integer.class, areaId, categoryId);
        assertTrue((preCount != null && preCount.equals(0) && (postCount != null && postCount.equals(1))),
                "addMapping for existing Area and Category, and null Subcategory, " +
                        "where the mapping does not exist, adds a mapping record");
    }

    @Test
    void addMapping_AreaCategorySubcategory_returns1AndRecordAdded() {
        Long areaId = addArea("JUnit Area");
        Long categoryId = addCategory("JUnit Category");
        Long subcategoryId = addSubcategory("JUnit Subcategory");
        String sql = "SELECT COUNT(*) FROM area_category_subcategory " +
                "WHERE area_id = ? AND category_id = ? AND " +
                "subcategory_id = ?";
        Integer preCount = jdbcTemplate.queryForObject(sql, Integer.class, areaId, categoryId, subcategoryId);
        int returnValue = areaCategorySubcategoryDAO.addMapping(areaId, categoryId, subcategoryId);
        assertEquals(1, returnValue, "addMapping for existing Area, Category, and Subcategory, " +
                "where the mapping does not exist, returns 1");
        Integer postCount = jdbcTemplate.queryForObject(sql, Integer.class, areaId, categoryId, subcategoryId);
        assertTrue((preCount != null && preCount.equals(0) && (postCount != null && postCount.equals(1))),
                "addMapping for existing Area, Category, and Subcategory, " +
                        "where the mapping does not exist, adds a mapping record");
    }
}