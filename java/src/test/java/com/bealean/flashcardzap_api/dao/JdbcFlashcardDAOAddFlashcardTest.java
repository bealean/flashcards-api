package com.bealean.flashcardzap_api.dao;

import com.bealean.flashcardzap_api.model.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class JdbcFlashcardDAOAddFlashcardTest extends JdbcDAOTest {

    private static FlashcardDAO flashcardDAO;

    @BeforeEach
    void configureDatabase() {
        super.configureDatabase();
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        flashcardDAO = new JdbcFlashcardDAO(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @Test
    void addFlashcard_cardWithCategoryAndNoArea_throwsException() {
        Flashcard flashcard = getCardWithRequiredFields();
        flashcard.setCategory("Junit Category");
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.addFlashcard(flashcard),
                "addFlashcard with a Category and no Area throws ResponseStatusException");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Area must be specified, if Category is specified. Card not added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addFlashcard throws exception with expected status and message " +
                "for card with Category and no Area");
    }

    @Test
    void addFlashcard_cardWithSubcategoryAndNoCategoryOrArea_throwsException() {
        Flashcard flashcard = getCardWithRequiredFields();
        flashcard.setSubcategory("Junit Subcategory");
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.addFlashcard(flashcard),
                "addFlashcard with a Subcategory and no Category or Area throws ResponseStatusException");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Area and Category must be specified, if Subcategory is specified. Card not added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addFlashcard throws expected exception status with expected message " +
                "for card with Subcategory and no Area or Category");
    }

    @Test
    void addFlashcard_cardWithSubcategoryAndAreaButNoCategory_throwsException() {
        Flashcard flashcard = getCardWithRequiredFields();
        flashcard.setSubcategory("Junit Subcategory");
        flashcard.setArea("Junit Area");
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.addFlashcard(flashcard),
                "addFlashcard with a Subcategory and Area, but no Category throws ResponseStatusException");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Category must be specified, if Subcategory is specified. Card not added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addFlashcard throws exception with expected status and message " +
                "for card with Subcategory and Area, but no Category");
    }

    @Test
    void addFlashcard_duplicateCardRequiredFieldsOnly_returnsExistingCardAndDoesNotAddNewCard() {
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        expectedFlashcard = addFlashcard(expectedFlashcard);
        Integer expectedCount = getFlashcardCount();
        Flashcard actualFlashcard = getCardWithRequiredFields();
        actualFlashcard = flashcardDAO.addFlashcard(actualFlashcard);
        Integer actualCount = getFlashcardCount();
        assertEquals(expectedCount, actualCount, "addFlashcard does not add a new flashcard record to the database, " +
                "if the card already exists for a card with only the required fields");
        assertEquals(expectedFlashcard.toString(), actualFlashcard.toString(), "addFlashcard returns the existing card " +
                "if new card with only the required fields matches an existing card");
    }

    @Test
    void addFlashcard_notDuplicateCardNewCardNullArea_returnsNewCardAndAddsNewCard() {
        Flashcard existingFlashcard = getCardWithRequiredFields();
        existingFlashcard.setArea("Junit Area");
        addArea("Junit Area");
        addFlashcard(existingFlashcard);
        Integer expectedCount = getFlashcardCount();
        Flashcard newFlashcard = getCardWithRequiredFields();
        newFlashcard = flashcardDAO.addFlashcard(newFlashcard);
        Integer actualCount = getFlashcardCount();
        assertTrue(expectedCount.compareTo(actualCount) < 0, "addFlashcard adds a new flashcard record to the database, " +
                "if a new card is added that is a duplicate of an existing card, but has a NULL Area");
        assertNotEquals(newFlashcard.getId(), existingFlashcard.getId(), "addFlashcard returns a card " +
                "with a new ID, if a new card is added that is a duplicate of an existing card, " +
                "but has a NULL Area");
    }

    @Test
    void addFlashcard_notDuplicateCardNewCardDifferentArea_returnsNewCardAndAddsNewCard() {
        Flashcard existingFlashcard = getCardWithRequiredFields();
        existingFlashcard.setArea("Junit Area");
        addArea("Junit Area");
        addFlashcard(existingFlashcard);
        Integer expectedCount = getFlashcardCount();
        Flashcard newFlashcard = getCardWithRequiredFields();
        newFlashcard.setArea("JUnit New Area");
        newFlashcard = flashcardDAO.addFlashcard(newFlashcard);
        Integer actualCount = getFlashcardCount();
        assertTrue(expectedCount.compareTo(actualCount) < 0, "addFlashcard adds a new flashcard record to the database, " +
                "if a new card is added that is a duplicate of an existing card, but has a different Area");
        assertNotEquals(newFlashcard.getId(), existingFlashcard.getId(), "addFlashcard returns a card " +
                "with a new ID, if a new card is added that is a duplicate of an existing card, " +
                "but has a different Area");
    }

    @Test
    void addFlashcard_notDuplicateCardNewCardNullCategory_returnsNewCardAndAddsNewCard() {
        Flashcard existingFlashcard = getCardWithRequiredFields();
        existingFlashcard.setArea("JUnit Area");
        addArea("JUnit Area");
        existingFlashcard.setCategory("Junit Category");
        addCategory("Junit Category");
        addFlashcard(existingFlashcard);
        Integer expectedCount = getFlashcardCount();
        Flashcard newFlashcard = getCardWithRequiredFields();
        newFlashcard.setArea("JUnit Area");
        newFlashcard = flashcardDAO.addFlashcard(newFlashcard);
        Integer actualCount = getFlashcardCount();
        assertTrue(expectedCount.compareTo(actualCount) < 0, "addFlashcard adds a new flashcard record to the database, " +
                "if a new card is added that is a duplicate of an existing card, but has a NULL Category");
        assertNotEquals(newFlashcard.getId(), existingFlashcard.getId(), "addFlashcard returns a card " +
                "with a new ID, if a new card is added that is a duplicate of an existing card, " +
                "but has a NULL Category");
    }

    @Test
    void addFlashcard_notDuplicateCardNewCardDifferentCategory_returnsNewCardAndAddsNewCard() {
        Flashcard existingFlashcard = getCardWithRequiredFields();
        existingFlashcard.setArea("JUnit Area");
        addArea("JUnit Area");
        existingFlashcard.setCategory("Junit Category");
        addCategory("Junit Category");
        addFlashcard(existingFlashcard);
        Integer expectedCount = getFlashcardCount();
        Flashcard newFlashcard = getCardWithRequiredFields();
        newFlashcard.setArea("JUnit Area");
        newFlashcard.setCategory("JUnit New Category");
        newFlashcard = flashcardDAO.addFlashcard(newFlashcard);
        Integer actualCount = getFlashcardCount();
        assertTrue(expectedCount.compareTo(actualCount) < 0, "addFlashcard adds a new flashcard record to the database, " +
                "if a new card is added that is a duplicate of an existing card, but has a different Category");
        assertNotEquals(newFlashcard.getId(), existingFlashcard.getId(), "addFlashcard returns a card " +
                "with a new ID, if a new card is added that is a duplicate of an existing card, " +
                "but has a different Category");
    }

    @Test
    void addFlashcard_notDuplicateCardNewCardNullSubategory_returnsNewCardAndAddsNewCard() {
        Flashcard existingFlashcard = getCardWithAllUserDefinedFields();
        addArea(existingFlashcard.getArea());
        addCategory(existingFlashcard.getCategory());
        addSubcategory(existingFlashcard.getSubcategory());
        addFlashcard(existingFlashcard);
        Integer expectedCount = getFlashcardCount();
        Flashcard newFlashcard = getCardWithAllUserDefinedFields();
        newFlashcard.setSubcategory(null);
        newFlashcard = flashcardDAO.addFlashcard(newFlashcard);
        Integer actualCount = getFlashcardCount();
        assertTrue(expectedCount.compareTo(actualCount) < 0, "addFlashcard adds a new flashcard record to the database, " +
                "if a new card is added that is a duplicate of an existing card, but has a NULL Subcategory");
        assertNotEquals(newFlashcard.getId(), existingFlashcard.getId(), "addFlashcard returns a card " +
                "with a new ID, if a new card is added that is a duplicate of an existing card, " +
                "but has a NULL Subcategory");
    }

    @Test
    void addFlashcard_notDuplicateCardNewCardDifferentSubategory_returnsNewCardAndAddsNewCard() {
        Flashcard existingFlashcard = getCardWithAllUserDefinedFields();
        addArea(existingFlashcard.getArea());
        addCategory(existingFlashcard.getCategory());
        addSubcategory(existingFlashcard.getSubcategory());
        addFlashcard(existingFlashcard);
        Integer expectedCount = getFlashcardCount();
        Flashcard newFlashcard = getCardWithAllUserDefinedFields();
        newFlashcard.setSubcategory("JUnit New Subcategory");
        newFlashcard = flashcardDAO.addFlashcard(newFlashcard);
        Integer actualCount = getFlashcardCount();
        assertTrue(expectedCount.compareTo(actualCount) < 0, "addFlashcard adds a new flashcard record to the database, " +
                "if a new card is added that is a duplicate of an existing card, but has a different Subcategory");
        assertNotEquals(newFlashcard.getId(), existingFlashcard.getId(), "addFlashcard returns a card " +
                "with a new ID, if a new card is added that is a duplicate of an existing card, " +
                "but has a different Subcategory");
    }

    @Test
    void addFlashcard_duplicateCardAllUserDefinedFields_returnsExistingAndDoesNotAddNewCard() {
        Flashcard expectedFlashcard = getCardWithAllUserDefinedFields();
        addArea(expectedFlashcard.getArea());
        addCategory(expectedFlashcard.getCategory());
        addSubcategory(expectedFlashcard.getSubcategory());
        expectedFlashcard = addFlashcard(expectedFlashcard);
        Integer expectedCount = getFlashcardCount();
        Flashcard actualFlashcard = getCardWithAllUserDefinedFields();
        actualFlashcard = flashcardDAO.addFlashcard(actualFlashcard);
        Integer actualCount = getFlashcardCount();
        assertEquals(expectedCount, actualCount, "addFlashcard does not add a new flashcard record to the database, " +
                "if the card already exists for a card with all user defined fields");
        assertEquals(expectedFlashcard.toString(), actualFlashcard.toString(), "addFlashcard returns the existing card " +
                "if new card with all user defined fields matches an existing card");
    }

    @Test
    void addFlashcard_duplicateCardBadURL_throwsException() {
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        addFlashcard(expectedFlashcard);
        Flashcard actualFlashcard = getCardWithRequiredFields();
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
        dataSource.setUrl("BadURL");
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.addFlashcard(actualFlashcard),
                "addFlashcard duplicate card check failure throws ResponseStatusException");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Failure checking if card already exists. Card not added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addFlashcard throws exception with expected " +
                "type and message when the duplicate card check fails");
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
    }

    @Test
    void addFlashcard_noAreaCategoryOrSubcategory_flashcardAddedWithExpectedValues() {
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        Flashcard returnedFlashcard = flashcardDAO.addFlashcard(expectedFlashcard);
        expectedFlashcard.setId(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), returnedFlashcard.toString(),
                "addFlashcard with no Area, Category, or Subcategory returns Flashcard with expected values.");
        String sql = "SELECT f.id AS \"id\", front, back, area_id, category_id, subcategory_id, view_timestamp " +
                "FROM flashcards f LEFT OUTER JOIN flashcard_views fv ON f.id = fv.flashcard_id WHERE f.id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, expectedFlashcard.getId());
        Flashcard actualFlashcard = new Flashcard();
        if (results.next()) {
            actualFlashcard.setId(results.getLong("id"));
            actualFlashcard.setFront(results.getString("front"));
            actualFlashcard.setBack(results.getString("back"));
            actualFlashcard.setArea(results.getString("area_id"));
            actualFlashcard.setCategory(results.getString("category_id"));
            actualFlashcard.setSubcategory(results.getString("subcategory_id"));
            actualFlashcard.setLastViewed(results.getTimestamp("view_timestamp"));
        }
        assertEquals(expectedFlashcard.toString(), actualFlashcard.toString(),
                "addFlashcard with no Area, Category, or Subcategory adds Flashcard record with expected values");
    }

    @Test
    void addFlashcard_existingAreaNoCategoryOrSubcategory_flashcardAddedAndReturnedWithExpectedValuesAndCardAssociatedWithExistingArea() {
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        String expectedAreaName = "JUnit Area";
        expectedFlashcard.setArea(expectedAreaName);
        Long expectedAreaId = addArea(expectedAreaName);
        Flashcard returnedFlashcard = flashcardDAO.addFlashcard(expectedFlashcard);
        expectedFlashcard.setId(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), returnedFlashcard.toString(),
                "addFlashcard with existing Area returns Flashcard with expected values.");
        String sql = "SELECT COALESCE(MAX(area_id),-1) FROM flashcards WHERE id = ?";
        Long actualAreaId = jdbcTemplate.queryForObject(sql, Long.class, returnedFlashcard.getId());
        assertEquals(expectedAreaId, actualAreaId, "addFlashcard with existing Area adds Flashcard record with area_id" +
                "corresponding to existing id in areas table.");
        Flashcard actualFlashcard = queryForCardById(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), actualFlashcard.toString(),
                "addFlashcard with existing Area adds Flashcard record with expected values");
    }

    @Test
    void addFlashcard_existingAreaCategorySubcategoryExistingMapping_flashcardAddedAndReturnedWithExpectedValues() {
        Flashcard expectedFlashcard = createFlashcardWithAllMappings();
        Flashcard returnedFlashcard = flashcardDAO.addFlashcard(expectedFlashcard);
        expectedFlashcard.setId(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), returnedFlashcard.toString(), "addFlashcard with existing Area, Category, Subcategory, and mappings " +
                "returns Flashcard with expected values.");
        Flashcard actualFlashcard = queryForCardById(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), actualFlashcard.toString(), "addFlashcard with existing Area, Category, Subcategory, and mappings " +
                "adds Flashcard record with expected values.");
    }

    @Test
    void addFlashcard_existingAreaCategorySubcategoryNoMapping_mappingAdded() {
        Flashcard flashcard = getCardWithAllUserDefinedFields();
        addArea(flashcard.getArea());
        addCategory(flashcard.getCategory());
        addSubcategory(flashcard.getSubcategory());
        flashcardDAO.addFlashcard(flashcard);
        String sql = "SELECT COUNT(*) FROM area_category_subcategory acs " +
                "JOIN areas a ON a.id = acs.area_id " +
                "JOIN categories c ON c.id = acs.category_id " +
                "JOIN subcategories s ON s.id = acs.subcategory_id " +
                "WHERE a.area_name = ? AND " +
                "c.category_name = ? AND " +
                "s.subcategory_name = ?";
        Integer mappingCount = jdbcTemplate.queryForObject(sql, Integer.class, flashcard.getArea(), flashcard.getCategory(), flashcard.getSubcategory());
        assertTrue(mappingCount != null && mappingCount.equals(1));
    }

    @Test
    void addFlashcard_existingAreaCategoryNullSubcategoryExistingMapping_flashcardAddedAndReturnedWithExpectedValues() {
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        String expectedAreaName = "JUnit Area";
        expectedFlashcard.setArea(expectedAreaName);
        long expectedAreaId = addArea(expectedAreaName);
        String expectedCategoryName = "JUnit Category";
        expectedFlashcard.setCategory(expectedCategoryName);
        long expectedCategoryId = addCategory(expectedCategoryName);
        addMapping(expectedAreaId, expectedCategoryId, null);
        Flashcard returnedFlashcard = flashcardDAO.addFlashcard(expectedFlashcard);
        expectedFlashcard.setId(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), returnedFlashcard.toString(), "addFlashcard with existing Area and Category, null Subcategory, and mapping " +
                "returns Flashcard with expected values.");
        Flashcard actualFlashcard = queryForCardById(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), actualFlashcard.toString(),
                "addFlashcard with existing Area and Category, null Subcategory, and mapping " +
                        "adds Flashcard record with expected values.");
    }

    @Test
    void addFlashcard_existingAreaCategorySubcategoryExistingMappingsFrontNull_throwsException() {
        Flashcard expectedFlashcard = createFlashcardWithAllMappings();
        expectedFlashcard.setFront(null);
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.addFlashcard(expectedFlashcard),
                "addFlashcard invalid flashcard to insert throws ResponseStatusException");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Flashcard failed to be added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addFlashcard throws exception with expected status and message " +
                "when Flashcard record cannot be inserted in the database");
    }

    @Test
    void addFlashcard_newAreaCategorySubcategory_flashcardAddedAndReturnedWithExpectedValuesAndMappingRecordAdded() {
        Flashcard expectedFlashcard = getCardWithAllUserDefinedFields();
        Flashcard returnedFlashcard = flashcardDAO.addFlashcard(expectedFlashcard);
        expectedFlashcard.setId(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), returnedFlashcard.toString(), "addFlashcard with new Area, Category, and Subcategory returns Flashcard with expected values");
        Flashcard actualFlashcard = queryForCardById(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), actualFlashcard.toString(),
                "addFlashcard with new Area, Category, and Subcategory adds Flashcard record with expected values.");
        String sql = "SELECT COUNT(*) FROM area_category_subcategory acs " +
                "JOIN flashcards f ON acs.area_id = f.area_id AND " +
                "acs.category_id = f.category_id AND " +
                "acs.subcategory_id = f.subcategory_id " +
                "WHERE f.id = ?";
        Integer actualCount = jdbcTemplate.queryForObject(sql, Integer.class, returnedFlashcard.getId());
        assertTrue(actualCount != null && actualCount.equals(1), "addFlashcard with new Area, Category, and Subcategory adds mapping record");
    }

    @Test
    void addFlashcard_newArea_flashcardReturnedWithExpectedValuesAndAreaAdded() {
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        String expectedAreaName = "JUnit Area";
        expectedFlashcard.setArea(expectedAreaName);
        String sql = "SELECT COUNT(*) FROM areas WHERE area_name = ?";
        Integer areaNameCount = jdbcTemplate.queryForObject(sql, Integer.class, expectedAreaName);
        if (areaNameCount != null && areaNameCount > 0) {
            fail("addFlashcard_newArea Test Data Setup Error: Test Area already exists.");
        }
        Flashcard returnedFlashcard = flashcardDAO.addFlashcard(expectedFlashcard);
        expectedFlashcard.setId(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), returnedFlashcard.toString(), "addFlashcard with new Area returns Flashcard with expected values");
        sql = "SELECT area_name FROM areas a " +
                "JOIN flashcards f ON a.id = f.area_id " +
                "WHERE f.id = ?";
        String actualAreaName = jdbcTemplate.queryForObject(sql, String.class, returnedFlashcard.getId());
        assertEquals(expectedAreaName, actualAreaName, "addFlashcard with new Area adds Area and " +
                "associates it with the flashcard");
    }

    @Test
    void addFlashcard_areaAddFails_throwsException() {
        Flashcard flashcard = getCardWithRequiredFields();
        String area = "JUnit AreaJUnit AreaJUnit Areax";
        flashcard.setArea(area);
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.addFlashcard(flashcard),
                "addFlashcard returns ResponseStatusException if Area cannot be added.");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Flashcard and dependencies not added. Area failed to be added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addFlashcard throws exception with expected status and message when " +
                "new Area fails to be added");
    }

    @Test
    void addFlashcard_newCategory_flashcardReturnedWithExpectedValuesCategoryAndMappingAdded() {
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        String area = "JUnit Area";
        String expectedCategoryName = "JUnit Category";
        expectedFlashcard.setArea(area);
        expectedFlashcard.setCategory(expectedCategoryName);
        addArea(area);
        String sql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
        Integer categoryNameCount = jdbcTemplate.queryForObject(sql, Integer.class, expectedCategoryName);
        if (categoryNameCount != null && categoryNameCount > 0) {
            fail("addFlashcard_newCategory Test Data Setup Error: Test Category already exists.");
        }
        Flashcard returnedFlashcard = flashcardDAO.addFlashcard(expectedFlashcard);
        expectedFlashcard.setId(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), returnedFlashcard.toString(),
                "addFlashcard with a new Category returns a Flashcard with the expected values");

        sql = "SELECT category_name FROM categories c " +
                "JOIN flashcards f ON c.id = f.category_id " +
                "WHERE f.id = ?";
        String actualCategoryName = jdbcTemplate.queryForObject(sql, String.class, returnedFlashcard.getId());
        assertEquals(expectedCategoryName, actualCategoryName, "addFlashcard with new Category adds Category and " +
                "associates it with the flashcard");
        sql = "SELECT COUNT(*) FROM area_category_subcategory acs " +
                "JOIN flashcards f ON acs.area_id = f.area_id AND " +
                "acs.category_id = f.category_id " +
                "WHERE acs.subcategory_id IS NULL AND f.id = ?";
        Integer actualCount = jdbcTemplate.queryForObject(sql, Integer.class, returnedFlashcard.getId());
        assertTrue(actualCount != null && actualCount.equals(1),
                "addFlashcard with existing Area, new Category, and null Subcategory adds mapping record");
    }

    @Test
    void addFlashcard_categoryAddFails_throwsException() {
        Flashcard flashcard = getCardWithRequiredFields();
        String area = "JUnit Area";
        flashcard.setArea(area);
        String category = "JUnit Category JUnit Categoryxx";
        flashcard.setCategory(category);
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.addFlashcard(flashcard),
                "addFlashcard returns ResponseStatusException if Category cannot be added.");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Flashcard and dependencies not added. Category failed to be added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addFlashcard throws exception with expected status and message when " +
                "new Category fails to be added");
    }

    @Test
    void addFlashcard_newSubcategory_flashcardReturnedWithExpectedValuesSubcategoryAndMappingAdded() {
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        String area = "JUnit Area";
        String category = "JUnit Category";
        String expectedSubcategoryName = "JUnit Subcategory";
        expectedFlashcard.setArea(area);
        expectedFlashcard.setCategory(category);
        expectedFlashcard.setSubcategory(expectedSubcategoryName);
        addArea(area);
        addCategory(category);
        String sql = "SELECT COUNT(*) FROM subcategories WHERE subcategory_name = ?";
        Integer subcategoryNameCount = jdbcTemplate.queryForObject(sql, Integer.class, expectedSubcategoryName);
        if (subcategoryNameCount != null && subcategoryNameCount > 0) {
            fail("addFlashcard_newSubcategory Test Data Setup Error: Test Subcategory already exists.");
        }
        Flashcard returnedFlashcard = flashcardDAO.addFlashcard(expectedFlashcard);
        expectedFlashcard.setId(returnedFlashcard.getId());
        assertEquals(expectedFlashcard.toString(), returnedFlashcard.toString(),
                "addFlashcard with a new Subcategory returns a Flashcard with the expected values");

        sql = "SELECT subcategory_name FROM subcategories s " +
                "JOIN flashcards f ON s.id = f.subcategory_id " +
                "WHERE f.id = ?";
        String actualSubcategoryName = jdbcTemplate.queryForObject(sql, String.class, returnedFlashcard.getId());
        assertEquals(expectedSubcategoryName, actualSubcategoryName, "addFlashcard with new Subcategory adds Subcategory and " +
                "associates it with the flashcard");
        sql = "SELECT COUNT(*) FROM area_category_subcategory acs " +
                "JOIN flashcards f ON acs.area_id = f.area_id AND " +
                "acs.category_id = f.category_id AND " +
                "acs.subcategory_id = f.subcategory_id " +
                "WHERE f.id = ?";
        Integer actualCount = jdbcTemplate.queryForObject(sql, Integer.class, returnedFlashcard.getId());
        assertTrue(actualCount != null && actualCount.equals(1),
                "addFlashcard with existing Area, existing Category, and new Subcategory adds mapping record");
    }

    @Test
    void addFlashcard_subcategoryAddFails_throwsException() {
        Flashcard flashcard = getCardWithRequiredFields();
        String area = "JUnit Area";
        flashcard.setArea(area);
        String category = "JUnit Category";
        flashcard.setCategory(category);
        String subcategory = "JUnit Subcategory JUnit Subcategory";
        flashcard.setSubcategory(subcategory);
        Exception exception = assertThrows(ResponseStatusException.class, () -> flashcardDAO.addFlashcard(flashcard),
                "addFlashcard returns ResponseStatusException if Subcategory cannot be added.");
        String expectedMessage = "422 UNPROCESSABLE_ENTITY \"Flashcard and dependencies not added. Subcategory failed to be added.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage, "addFlashcard throws exception with expected status and message when " +
                "new Subcategory fails to be added");
    }

    private Flashcard mapResultsToCard(SqlRowSet results) {
        Flashcard flashcard = new Flashcard();
        if (results.next()) {
            flashcard.setId(results.getLong("id"));
            flashcard.setFront(results.getString("front"));
            flashcard.setBack(results.getString("back"));
            flashcard.setArea(results.getString("area_name"));
            flashcard.setCategory(results.getString("category_name"));
            flashcard.setSubcategory(results.getString("subcategory_name"));
            flashcard.setLastViewed(results.getTimestamp("view_timestamp"));
        }
        return flashcard;
    }

    private Flashcard createFlashcardWithAllMappings() {
        Flashcard flashcard = getCardWithRequiredFields();
        String expectedAreaName = "JUnit Area";
        flashcard.setArea(expectedAreaName);
        long expectedAreaId = addArea(expectedAreaName);
        String expectedCategoryName = "JUnit Category";
        flashcard.setCategory(expectedCategoryName);
        long expectedCategoryId = addCategory(expectedCategoryName);
        String expectedSubcategoryName = "JUnit Subcategory";
        flashcard.setSubcategory(expectedSubcategoryName);
        long expectedSubcategoryId = addSubcategory(expectedSubcategoryName);
        addMapping(expectedAreaId, expectedCategoryId, expectedSubcategoryId);
        return flashcard;
    }

    Flashcard getCardWithRequiredFields() {
        Flashcard flashcard = new Flashcard();
        flashcard.setFront("test");
        flashcard.setBack("test");
        return flashcard;
    }

    Flashcard getCardWithAllUserDefinedFields() {
        Flashcard flashcard = getCardWithRequiredFields();
        String expectedAreaName = "JUnit Area";
        flashcard.setArea(expectedAreaName);
        String expectedCategoryName = "JUnit Category";
        flashcard.setCategory(expectedCategoryName);
        String expectedSubcategoryName = "JUnit Subcategory";
        flashcard.setSubcategory(expectedSubcategoryName);
        return flashcard;
    }

    Integer getFlashcardCount() {
        String sql = "SELECT COUNT(*) FROM flashcards";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    Flashcard queryForCardById(Long id) {
        String sql = "SELECT f.id AS \"id\", front, back, area_name, category_name, subcategory_name, view_timestamp " +
                "FROM flashcards f " +
                "LEFT OUTER JOIN areas a ON a.id = f.area_id " +
                "LEFT OUTER JOIN categories c ON c.id = f.category_id " +
                "LEFT OUTER JOIN subcategories s ON s.id = f.subcategory_id " +
                "LEFT OUTER JOIN flashcard_views fv ON f.id = fv.flashcard_id " +
                "WHERE f.id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        return mapResultsToCard(results);
    }

}