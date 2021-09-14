package com.bealean.flashcardzap_api.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcCategoryDAOTest extends JdbcDAOTest {
    private static CategoryDAO categoryDAO;

    @BeforeEach
    void configureDatabase() {
        super.configureDatabase();
        categoryDAO = new JdbcCategoryDAO(jdbcTemplate);
    }

    @Test
    void getCategoriesForArea_lowercaseAllAreasCategoryWithNoArea_returnsCategoryInList() {
        String categoryName1 = "JUnitCategory1";
        addCategory(categoryName1);
        assertTrue(isCategoryReturnedByGetCategoriesForArea("all", categoryName1),
                "getCategoriesForArea called with Area of lowercase 'all' returns list " +
                        "containing test Category that is not associated with a specific Area");
    }

    @Test
    void getCategoriesForArea_uppercaseAllAreasCategoryWithNoArea_returnsCategoryInList() {
        String categoryName1 = "JUnitCategory1";
        addCategory(categoryName1);
        assertTrue(isCategoryReturnedByGetCategoriesForArea("ALL", categoryName1),
                "getCategoriesForArea called with Area of uppercase 'all' returns list " +
                        "containing test Category that is not associated with a specific Area");
    }

    @Test
    void getCategoriesForArea_pascalCaseAllAreasCategoryWithNoArea_returnsCategoryInList() {
        String categoryName1 = "JUnitCategory1";
        addCategory(categoryName1);
        assertTrue(isCategoryReturnedByGetCategoriesForArea("All", categoryName1),
                "getCategoriesForArea called with Area of Pascal case 'All' returns list " +
                        "containing test Category that is not associated with a specific Area");
    }

    @Test
    void getCategoriesForArea_allAreasMulitpleCategoriesExist_returnsCategoriesInAscendingAlphabeticalOrderByName() {
        addCategory("A JUnit Category");
        addCategory("another JUnit Category");
        addCategory("Z JUnit Category");
        addCategory("z JUnit Category");
        addCategory("1 JUnit Category");
        addCategory("2 JUnit Category");
        addCategory("10 JUnit Category");
        List<String> expectedCategoryList = new ArrayList<>();
        expectedCategoryList.add("1 JUnit Category");
        expectedCategoryList.add("10 JUnit Category");
        expectedCategoryList.add("2 JUnit Category");
        expectedCategoryList.add("A JUnit Category");
        expectedCategoryList.add("another JUnit Category");
        expectedCategoryList.add("z JUnit Category");
        expectedCategoryList.add("Z JUnit Category");
        List<String> fullCategoryList = categoryDAO.getCategoriesForArea("all");
        List<String> actualCategoryList = new ArrayList<>();
        for (String categoryName : fullCategoryList) {
            if (expectedCategoryList.contains(categoryName)) {
                actualCategoryList.add(categoryName);
            }
        }
        assertEquals(expectedCategoryList, actualCategoryList, "getCategoriesForArea for 'all' Areas returns " +
                "all added test Categories in ascending Alphabetical order by Category name");
    }

    @Test
    void getCategoriesForArea_allAreasCategoriesExist_returnsListWithSizeEqualToTotalNumberOfCategories() {
        addCategory("A JUnit Category");
        addCategory("another JUnit Category");
        Integer expectedCategoryListSize = getCategoryCount();
        Integer actualCategoryListSize = categoryDAO.getCategoriesForArea("all").size();
        assertEquals(expectedCategoryListSize, actualCategoryListSize, "getCategoriesForArea for 'all' Areas returns " +
                "a list with a size equal to the total number of Categories");
    }

    @Test
    void getCategoriesForArea_specificArea_returnsListContainingCategoryAddedToArea() {
        String areaName1 = "JUnitArea1";
        String categoryName1 = "JUnitCategory1";
        addAreaAndCategoryAndAssociate(areaName1, categoryName1);
        assertTrue(isCategoryReturnedByGetCategoriesForArea(areaName1, categoryName1),
                "getCategoriesForArea returns list containing Category in Area");
    }

    @Test
    void getCategoriesForArea_specificAreaHavingCategoriesWithMultipleMappings_returnsDistinctCategoriesForAreaInAscendingAlphabeticalOrderByName() {
        /* Add a category with two subcategories to an area to
           check that the same category is not returned more than once. */
        long areaId = addArea("JUnitDev");
        long categoryId = addCategory("SQLJUnit");
        long subcategoryId = addSubcategory("JUnitDDL");
        addMapping(areaId, categoryId, subcategoryId);
        subcategoryId = addSubcategory("JUnitDML");
        addMapping(areaId, categoryId, subcategoryId);
        //Add a different category for the same area
        categoryId = addCategory("JavaJUnit");
        addMapping(areaId, categoryId, null);
        // Add a different area with a different category
        addAreaAndCategoryAndAssociate("JUnitTest", "UFT");
        List<String> expectedCategoryList = new ArrayList<>();
        expectedCategoryList.add("JavaJUnit");
        expectedCategoryList.add("SQLJUnit");
        List<String> actualCategoryList = categoryDAO.getCategoriesForArea("JUnitDev");
        assertEquals(expectedCategoryList, actualCategoryList, "getCategoriesForArea for a specific Area returns " +
                "the distinct Categories for that area in ascending Alphabetical order by Category name");
    }

    @Test
    void getCategoriesForArea_specificAreaHavingCategoriesWithMultipleMappings_returnsListWithSizeEqualToDistinctNumberOfCategoriesForArea() {
        /* Add a category with two subcategories to an area to
           check that the same category is not returned more than once. */
        long areaId = addArea("JUnitDev");
        long categoryId = addCategory("JUnitSQL");
        long subcategoryId = addSubcategory("JUnitDDL");
        addMapping(areaId, categoryId, subcategoryId);
        subcategoryId = addSubcategory("JUnitDML");
        addMapping(areaId, categoryId, subcategoryId);
        //Add a different category for the same area
        categoryId = addCategory("JUnitJava");
        addMapping(areaId, categoryId, null);
        // Add a different area with a different category
        addAreaAndCategoryAndAssociate("JUnitTest", "UFT");
        Integer expectedListSize = 2;
        Integer actualListSize = categoryDAO.getCategoriesForArea("JUnitDev").size();
        assertEquals(expectedListSize, actualListSize, "getCategoriesForArea for a specific Area returns a list with a size equal to " +
                "the number of distinct Categories for only that Area");
    }

    @Test
    void getCategoriesForArea_areaWithAllAllowedSpecialCharacters_returnsListContainingCategoryAddedToArea() {
        String areaName1 = "Area-1_90 zZ~A.";
        String categoryName1 = "JUnitCategory1";
        addAreaAndCategoryAndAssociate(areaName1, categoryName1);
        assertTrue(isCategoryReturnedByGetCategoriesForArea(areaName1, categoryName1),
                "getCategoriesForArea for Area with all allowed special characters returns " +
                        "list containing Category in Area");
    }

    @Test
    void getCategoriesForArea_specificAreaWithoutTestCategory_returnsListWithoutTestCategory() {
        String areaName1 = "JUnitArea1";
        String categoryName1 = "JUnitTest1";
        addAreaAndCategoryAndAssociate(areaName1, categoryName1);
        String categoryName2 = "JUnitTest2";
        addCategory(categoryName2);
        assertFalse(isCategoryReturnedByGetCategoriesForArea(areaName1, categoryName2),
                "getCategoriesForArea return list does not contain a Category not in Area");
    }

    @Test
    void getCategoriesForArea_specificAreaWithoutAnyCategories_returnsEmptyList() {
        String areaName1 = "JUnitArea1";
        addArea(areaName1);
        int listSize = categoryDAO.getCategoriesForArea(areaName1).size();
        assertEquals(0, listSize, "getCategoriesForArea returns empty list for Area with no categories");
    }

    @Test
    void getCategoriesForArea_areaNotExisting_throwsException() {
        String areaName1 = "Area Not In DB";
        Exception exception = assertThrows(ResponseStatusException.class, () -> categoryDAO.getCategoriesForArea(areaName1),
                "getCategoriesForArea throws ResponseStatusException for Area not in database");
        String expectedMessage = "404 NOT_FOUND \"Area not found. Unable to get Categories for missing Area. Send Area 'all' to retrieve all Categories.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getCategoriesForArea throws exception with expected status and message for Area not in database");
    }

    @Test
    void getCategoriesForArea_areaEmptyString_throwsException() {
        String areaName1 = "";
        Exception exception = assertThrows(ResponseStatusException.class, () -> categoryDAO.getCategoriesForArea(areaName1),
                "getCategoriesForArea throws ResponseStatusException for empty String as Area");
        String expectedMessage = "404 NOT_FOUND \"Area not found. Unable to get Categories for missing Area. Send Area 'all' to retrieve all Categories.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getCategoriesForArea throws exception with expected status and message for empty String as Area");
    }

    @Test
    void getCategoriesForArea_nullArea_throwsException() {
        Exception exception = assertThrows(ResponseStatusException.class, () -> categoryDAO.getCategoriesForArea(null),
                "getCategoriesForArea throws ResponseStatusException for null Area");
        String expectedMessage = "404 NOT_FOUND \"Area not found. Unable to get Categories for missing Area. Send Area 'all' to retrieve all Categories.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "getCategoriesForArea throws exception with expected status and message for null Area");
    }

    @Test
    void addCategory_validCategoryName_categoryAddedToDatabase() {
        String expectedCategoryName = "JUnit Test Category";
        String actualCategoryName = addCategoryAndReturnNameFromDatabase(expectedCategoryName);
        assertEquals(expectedCategoryName, actualCategoryName,
                "addCategory adds valid Category name to database");
    }

    @Test
    void addCategory_maxAllowedLength_categoryAddedToDatabase() {
        String expectedCategoryName = "Category1 Category2 Category3x";
        String actualCategoryName = addCategoryAndReturnNameFromDatabase(expectedCategoryName);
        assertEquals(expectedCategoryName, actualCategoryName, "addCategory adds Category name of max allowed length to database");
    }

    @Test
    void addCategory_allAllowedSpecialCharacters_categoryAddedToDatabase() {
        String expectedCategoryName = "Cat-1_90 zZ~A.";
        String actualCategoryName = addCategoryAndReturnNameFromDatabase(expectedCategoryName);
        assertEquals(expectedCategoryName, actualCategoryName, "addCategory adds Category name with all allowed special characters to database");
    }

    @Test
    void addCategory_duplicateName_returnsExistingIdAndCategoryNotAddedToDatabase() {
        String categoryName = "JUnit Test Category";
        long expectedCategoryId = categoryDAO.addCategory(categoryName);
        long actualCategoryId = categoryDAO.addCategory(categoryName);
        assertEquals(expectedCategoryId, actualCategoryId,
                "addCategory returns existing id when Category already exists");
        String sql = "SELECT count(category_name) FROM categories WHERE category_name = ?";
        Integer actualCategoryCount = jdbcTemplate.queryForObject(sql, Integer.class, categoryName);
        assertEquals(Integer.valueOf(1), actualCategoryCount,
                "addCategory does not add a duplicate Category to the database");
    }

    @Test
    void addCategory_nullName_returnsNegativeIdAndCategoryNotAddedToDatabase() {
        Integer expectedCategoryCount = getCategoryCount();
        long categoryId = categoryDAO.addCategory(null);
        Integer actualCategoryCount = getCategoryCount();
        assertEquals(-1L, categoryId, "addCategory returns -1L for null Category name");
        assertEquals(expectedCategoryCount, actualCategoryCount,
                "addCategory does not add a null Category to the database");
    }

    @Test
    void addCategory_emptyStringName_returnsNegativeIdAndCategoryNotAddedToDatabase() {
        String categoryName = "";
        Integer expectedCategoryCount = getCategoryCount();
        long categoryId = categoryDAO.addCategory(categoryName);
        Integer actualCategoryCount = getCategoryCount();
        assertEquals(-1L, categoryId, "addCategory returns -1L for an empty String Category name");
        assertEquals(expectedCategoryCount, actualCategoryCount,
                "addCategory does not add an empty String Category name to the database");
    }

    @Test
    void addCategory_failureToAddCategory_throwsResponseStatusException() {
        String expectedCategoryName = "JUnit Cat1JUnit Cat2JUnit Cat3x";
        Exception exception = assertThrows(ResponseStatusException.class, () -> categoryDAO.addCategory(expectedCategoryName),
                "addCategory throws ResponseStatusException for Category name that exceeds the max allowed length");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Category failed to be added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addCategory throws exception with expected status and message " +
                "when database insert fails");
    }

    @Test
    void getCategoryIdByName_existingCategory_returnsExpectedId() {
        String categoryName = "JUnit Category";
        long expectedId = addCategory(categoryName);
        long actualId = categoryDAO.getCategoryIdByName(categoryName);
        assertEquals(expectedId, actualId, "getCategoryIdByName returns the expected id for an existing Category name");
    }

    @Test
    void getCategoryIdByName_nameHasAllAllowedSpecialCharacters_returnsExpectedId() {
        String categoryName = "Cat-1_90 zZ~A.";
        long expectedId = addCategory(categoryName);
        long actualId = categoryDAO.getCategoryIdByName(categoryName);
        assertEquals(expectedId, actualId,
                "getCategoryIdByName returns the expected id for a Category name " +
                        "including all allowed special characters");
    }

    @Test
    void getCategoryIdByName_categoryNotExisting_returnsNegativeId() {
        String categoryName = "Not a Category";
        long expectedId = -1L;
        long actualId = categoryDAO.getCategoryIdByName(categoryName);
        assertEquals(expectedId, actualId, "getCategoryIdByName returns -1L for a Category that doesn't exist");
    }

    @Test
    void getCategoryIdByName_nullCategory_returnsNegativeId() {
        long expectedId = -1L;
        long actualId = categoryDAO.getCategoryIdByName(null);
        assertEquals(expectedId, actualId, "getCategoryIdByName returns -1L for a null Category");
    }

    @Test
    void getCategoryIdByName_emptyStringCategory_returnsNegativeId() {
        long expectedId = -1L;
        long actualId = categoryDAO.getCategoryIdByName("");
        assertEquals(expectedId, actualId, "getCategoryIdByName returns -1L for an empty String Category");
    }

    @Test
    void getCategoryIdByName_badDataSourceURL_throwsException() {
        dataSource.destroy();
        isDatabaseConfigured = false;
        configureDatabase();
        dataSource.setUrl("badUrl");
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> categoryDAO.getCategoryIdByName("JUnitCategory"),
                "getCategoryIdByName throws ResponseStatusException if query to retrieve ID fails.");
        String expectedMessage = "500 INTERNAL_SERVER_ERROR \"Exception checking for Category in database.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage,
                "getCategoryIdByName throws exception with expected status and message if query to retrieve ID fails");
        dataSource.destroy();
        isDatabaseConfigured = false;
        configureDatabase();
    }

    boolean isCategoryReturnedByGetCategoriesForArea(String areaName, String categoryName) {
        List<String> categories = categoryDAO.getCategoriesForArea(areaName);
        boolean isCategoryInList = false;
        if (categories != null) {
            isCategoryInList = categories.contains(categoryName);
        }
        return isCategoryInList;
    }

    Integer getCategoryCount() {
        String sql = "SELECT count(*) FROM categories";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    String addCategoryAndReturnNameFromDatabase(String categoryName) {
        long categoryId = categoryDAO.addCategory(categoryName);
        String actualCategoryName = "";
        if (categoryId != -1L) {
            String sql = "SELECT category_name FROM categories WHERE id = ?";
            actualCategoryName = jdbcTemplate.queryForObject(sql, String.class, categoryId);
        }
        return actualCategoryName;
    }
}