package com.bealean.flashcardzap_api.dao;

import com.bealean.flashcardzap_api.model.Flashcard;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;

class JdbcFlashcardDAOGetNextTest extends JdbcDAOTest {

    private static FlashcardDAO flashcardDAO;

    @BeforeEach
    void configureDatabase() {
        super.configureDatabase();
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        flashcardDAO = new JdbcFlashcardDAO(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @Test
    void getNext_matchingViewedAndUnviewedCards_returnsCardWithNullLastViewed(){
        Flashcard unviewedFlashcard = getCardWithRequiredFields();
        String area = "JUnit Test Area";
        unviewedFlashcard.setArea(area);
        addArea(area);
        Long expectedId = addFlashcard(unviewedFlashcard).getId();
        Flashcard viewedFlashcard = getCardWithRequiredFields();
        viewedFlashcard.setArea(area);
        addFlashcard(viewedFlashcard);
        insertLastViewed(viewedFlashcard.getId());
        Long actualId = flashcardDAO.getNext(area,"all","all").getId();
        assertEquals(expectedId,actualId,"getNext returns unviewed card when there is also a matching viewed card");
    }

    @Test
    void getNext_matchingViewedCards_returnsCardWithLessRecentLastViewed(){
        Flashcard earlierViewedFlashcard = getCardWithRequiredFields();
        String area = "JUnit Test Area";
        earlierViewedFlashcard.setArea(area);
        addArea(area);
        Long expectedId = addFlashcard(earlierViewedFlashcard).getId();
        Flashcard laterViewedFlashcard = getCardWithRequiredFields();
        laterViewedFlashcard.setArea(area);
        addFlashcard(laterViewedFlashcard);
        Timestamp earlierViewedCardTimestamp = insertLastViewed(earlierViewedFlashcard.getId());
        Timestamp laterViewedCardTimestamp = insertLastViewed(laterViewedFlashcard.getId());
        assertTrue(laterViewedCardTimestamp.after(earlierViewedCardTimestamp),"Test setup issue: Last View Timestamp of Expected card " +
                "should be before Last View Timestamp of Other card, but it is not.");
        Long actualId = flashcardDAO.getNext(area,"all","all").getId();
        assertEquals(expectedId,actualId,"getNext returns less recently viewed card when there is also a matching more recently viewed card");
    }

    @Test
    void getNext_specificAreaCategoryAndSubcategoryViewedMatchingAndUnviewedUnmatching_returnsMatchingCard(){
        String expectedArea = "JUnit Expected Area";
        addArea(expectedArea);
        String expectedCategory = "JUnit Expected Category";
        addCategory(expectedCategory);
        String expectedSubcategory = "JUnit Expected Subcategory";
        addSubcategory(expectedSubcategory);

        String otherArea = "JUnit Another Area";
        addArea(otherArea);
        String otherCategory = "JUnit Another Category";
        addCategory(otherCategory);
        String otherSubcategory = "JUnit Another Subcategory";
        addSubcategory(otherSubcategory);

        Flashcard expectedFlashcard = addUnviewedCard(expectedArea,expectedCategory,expectedSubcategory);
        insertLastViewed(expectedFlashcard.getId());

        addUnviewedCard(otherArea,otherCategory,otherSubcategory);
        addUnviewedCard(null,null, null);
        addUnviewedCard(expectedArea,expectedCategory,otherSubcategory);
        addUnviewedCard(expectedArea,otherCategory,expectedSubcategory);
        addUnviewedCard(otherArea,expectedCategory,expectedSubcategory);
        addUnviewedCard(expectedArea,expectedCategory,null);
        addUnviewedCard(expectedArea,null,expectedSubcategory);
        addUnviewedCard(null,expectedCategory,expectedSubcategory);

        Flashcard actualFlashcard = flashcardDAO.getNext(expectedArea,expectedCategory,expectedSubcategory);
        assertEquals(expectedFlashcard.getId(),actualFlashcard.getId(),
                "getNext returns viewed card for specified Area, Category, and Subcategory " +
                        "where unviewed unmatching cards exist");
    }

    @Test
    void getNext_specificUntrimmedAreaCategoryAndSubcategory_returnsTrimmedMatch(){
        String expectedArea = " JUnit Expected Area ";
        addArea(expectedArea.trim());
        String expectedCategory = " JUnit Expected Category ";
        addCategory(expectedCategory.trim());
        String expectedSubcategory = " JUnit Expected Subcategory ";
        addSubcategory(expectedSubcategory.trim());

        Flashcard expectedFlashcard = addUnviewedCard(expectedArea.trim(),expectedCategory.trim(),expectedSubcategory.trim());

        Flashcard actualFlashcard = flashcardDAO.getNext(expectedArea,expectedCategory,expectedSubcategory);
        assertEquals(expectedFlashcard.getId(),actualFlashcard.getId(),
                "getNext returns card matching trimmed Area, Category, and Subcategory");
    }

    @Test
    void getNext_specificAreaWithViewedCardAndUnviewedCardForOtherArea_returnsCardWithMatchingArea(){
        String expectedArea = "JUnit Expected Area";
        addArea(expectedArea);
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        expectedFlashcard.setArea(expectedArea);
        addFlashcard(expectedFlashcard);
        String otherArea = "JUnit Another Area";
        addArea(otherArea);
        Flashcard otherAreaFlashcard = getCardWithRequiredFields();
        otherAreaFlashcard.setArea(otherArea);
        addFlashcard(otherAreaFlashcard);
        insertLastViewed(expectedFlashcard.getId());
        Flashcard actualFlashcard = flashcardDAO.getNext(expectedArea,"all","all");
        assertEquals(expectedFlashcard.getId(),actualFlashcard.getId(),
                "getNext returns viewed card for specified Area where another Area has an unviewed card");
    }

    @Test
    void getNext_allAreas_returnsCardsWithDifferentAreasAndNoArea(){
        assertTrue(doesGetNextReturnCardsWithDifferentAreasAndCardWithNoArea("all"),
                "getNext with 'all' Area returns cards from different Areas and card with no Area");
    }

    @Test
    void getNext_nullArea_returnsCardsWithDifferentAreasAndNoArea(){
        assertTrue(doesGetNextReturnCardsWithDifferentAreasAndCardWithNoArea(null),
                "getNext with null Area returns cards from different Areas and card with no Area");
    }

    @Test
    void getNext_emptyStringArea_returnsCardsWithDifferentAreasAndNoArea(){
        assertTrue(doesGetNextReturnCardsWithDifferentAreasAndCardWithNoArea(""),
                "getNext with empty String Area returns cards from different Areas and card with no Area");
    }

    private boolean doesGetNextReturnCardsWithDifferentAreasAndCardWithNoArea(String area) {
        String subcategory = "JUnit Subcategory";
        addSubcategory(subcategory);
        String area1 = "JUnit Expected Area 1";
        addArea(area1);
        Long expectedCard1Id = addUnviewedCard(area1,null,subcategory).getId();
        insertLastViewed(expectedCard1Id);
        String area2 = "JUnit Expected Area 2";
        addArea(area2);
        Long expectedCard2Id = addUnviewedCard(area2,null,subcategory).getId();
        insertLastViewed(expectedCard2Id);
        Long expectedCard3Id = addUnviewedCard(null,null,subcategory).getId();
        insertLastViewed(expectedCard3Id);

        Long actualCard1Id = flashcardDAO.getNext(area,"all",subcategory).getId();
        Long actualCard2Id = flashcardDAO.getNext(area,"all",subcategory).getId();
        Long actualCard3Id = flashcardDAO.getNext(area,"all",subcategory).getId();

        return expectedCard1Id.equals(actualCard1Id) && expectedCard2Id.equals(actualCard2Id)
                && expectedCard3Id.equals(actualCard3Id);
    }

    @Test
    void getNext_specificCategoryWithViewedCardAndUnviewedCardForOtherCategory_returnsCardWithMatchingCategory(){
        String expectedCategory = "JUnit Expected Category";
        addCategory(expectedCategory);
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        expectedFlashcard.setCategory(expectedCategory);
        addFlashcard(expectedFlashcard);
        String otherCategory = "JUnit Another Category";
        addCategory(otherCategory);
        Flashcard otherCategoryFlashcard = getCardWithRequiredFields();
        otherCategoryFlashcard.setCategory(otherCategory);
        addFlashcard(otherCategoryFlashcard);
        insertLastViewed(expectedFlashcard.getId());
        Flashcard actualFlashcard = flashcardDAO.getNext("all",expectedCategory,"all");
        assertEquals(expectedFlashcard.getId(),actualFlashcard.getId(),
                "getNext returns viewed card for specified Category where another Category has an unviewed card");
    }

    @Test
    void getNext_allCategories_returnsCardsWithDifferentCategoriesAndNoCategory(){
        assertTrue(doesGetNextReturnCardsWithDifferentCategoriesAndCardWithNoCategory("all"),
                "getNext with 'all' Category returns cards from different Categories and card with no Category");
    }

    @Test
    void getNext_nullCategory_returnsCardsWithDifferentCategoriesAndNoCategory(){
        assertTrue(doesGetNextReturnCardsWithDifferentCategoriesAndCardWithNoCategory(null),
                "getNext with null Category returns cards from different Categories and card with no Category");
    }

    @Test
    void getNext_emptyStringCategory_returnsCardsWithDifferentCategoriesAndNoCategory(){
        assertTrue(doesGetNextReturnCardsWithDifferentCategoriesAndCardWithNoCategory(""),
                "getNext with empty String Category returns cards from different Categories and card with no Category");
    }

    private boolean doesGetNextReturnCardsWithDifferentCategoriesAndCardWithNoCategory(String category) {
        String area = "JUnit Area";
        addArea(area);
        String category1 = "JUnit Expected Category 1";
        addCategory(category1);
        Long expectedCard1Id = addUnviewedCard(area,category1,null).getId();
        insertLastViewed(expectedCard1Id);
        String category2 = "JUnit Expected Category 2";
        addCategory(category2);
        Long expectedCard2Id = addUnviewedCard(area,category2,null).getId();
        insertLastViewed(expectedCard2Id);
        Long expectedCard3Id = addUnviewedCard(area,null,null).getId();
        insertLastViewed(expectedCard3Id);

        Long actualCard1Id = flashcardDAO.getNext(area,category,"all").getId();
        Long actualCard2Id = flashcardDAO.getNext(area,category,"all").getId();
        Long actualCard3Id = flashcardDAO.getNext(area,category,"all").getId();

        return expectedCard1Id.equals(actualCard1Id) && expectedCard2Id.equals(actualCard2Id)
                && expectedCard3Id.equals(actualCard3Id);
    }

    @Test
    void getNext_specificSubcategoryWithViewedCardAndUnviewedCardForOtherSubcategory_returnsCardWithMatchingSubcategory(){
        String expectedSubcategory = "JUnit Expected Subcategory";
        addSubcategory(expectedSubcategory);
        Flashcard expectedFlashcard = getCardWithRequiredFields();
        expectedFlashcard.setSubcategory(expectedSubcategory);
        addFlashcard(expectedFlashcard);
        String otherSubcategory = "JUnit Another Subcategory";
        addSubcategory(otherSubcategory);
        Flashcard otherSubcategoryFlashcard = getCardWithRequiredFields();
        otherSubcategoryFlashcard.setSubcategory(otherSubcategory);
        addFlashcard(otherSubcategoryFlashcard);
        insertLastViewed(expectedFlashcard.getId());
        Flashcard actualFlashcard = flashcardDAO.getNext("all","all",expectedSubcategory);
        assertEquals(expectedFlashcard.getId(),actualFlashcard.getId(),
                "getNext returns viewed card for specified Subcategory where another Subcategory has an unviewed card");
    }

    @Test
    void getNext_allSubcategories_returnsCardsWithDifferentSubcategoriesAndNoSubcategory(){
        assertTrue(doesGetNextReturnCardsWithDifferentSubcategoriesAndCardWithNoSubcategory("all"),
                "getNext with 'all' Subcategory returns cards from different Subcategories and card with no Subcategory");
    }

    @Test
    void getNext_nullSubcategory_returnsCardsWithDifferentSubcategoriesAndNoSubcategory(){
        assertTrue(doesGetNextReturnCardsWithDifferentSubcategoriesAndCardWithNoSubcategory(null),
                "getNext with null Subcategory returns cards from different Subcategories and card with no Subcategory");
    }

    @Test
    void getNext_emptyStringSubcategory_returnsCardsWithDifferentSubcategoriesAndNoSubcategory(){
        assertTrue(doesGetNextReturnCardsWithDifferentSubcategoriesAndCardWithNoSubcategory(""),
                "getNext with empty String Subcategory returns cards from different Subcategories and card with no Subcategory");
    }

    private boolean doesGetNextReturnCardsWithDifferentSubcategoriesAndCardWithNoSubcategory(String subcategory) {
        String category = "JUnit Category";
        addCategory(category);
        String subcategory1 = "JUnit Expected Subcategory 1";
        addSubcategory(subcategory1);
        Long expectedCard1Id = addUnviewedCard(null,category,subcategory1).getId();
        insertLastViewed(expectedCard1Id);
        String subcategory2 = "JUnit Expected Subcategory 2";
        addSubcategory(subcategory2);
        Long expectedCard2Id = addUnviewedCard(null,category,subcategory2).getId();
        insertLastViewed(expectedCard2Id);
        Long expectedCard3Id = addUnviewedCard(null,category,null).getId();
        insertLastViewed(expectedCard3Id);

        Long actualCard1Id = flashcardDAO.getNext("all",category,subcategory).getId();
        Long actualCard2Id = flashcardDAO.getNext("all",category,subcategory).getId();
        Long actualCard3Id = flashcardDAO.getNext("all",category,subcategory).getId();

        return expectedCard1Id.equals(actualCard1Id) && expectedCard2Id.equals(actualCard2Id)
                && expectedCard3Id.equals(actualCard3Id);
    }

    @Test
    void getNext_noMatchingCards_throwsResponseStatusExceptionWithExpectedMessage() {
        Exception exception = assertThrows(ResponseStatusException.class, ()-> flashcardDAO.getNext("Nonexistent Area",null,null),
                "getNext throws ResponseStatusException if there are no cards matching the request");
        String expectedMessage = "404 NOT_FOUND \"No cards available for requested Area, Category, and Subcategory combination. Send 'all' for these parameters to return all cards regardless of the value.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage,actualMessage, "getNext throws exception with expected status and message " +
                "if there are no cards matching the request");
    }

    @Test
    void getNext_badDataSourceURL_throwsResponseStatusExceptionWithExpectedMessage() {
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
        dataSource.setUrl("badUrl");
        Exception exception = assertThrows(ResponseStatusException.class,
                () -> flashcardDAO.getNext("all","all","all"),
                "getNext throws ResponseStatusException if query to retrieve next card fails.");
        String expectedMessage = "500 INTERNAL_SERVER_ERROR \"Exception getting next card from database.\"";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage,
                "getNext throws exception with expected status and message if query to retrieve next card fails");
        destroyDataSourceAndSetDBConfigFlagFalse();
        configureDatabase();
    }

    private Flashcard addUnviewedCard(String area, String category, String subcategory) {
        Flashcard flashcard = getCardWithRequiredFields();
        flashcard.setArea(area);
        flashcard.setCategory(category);
        flashcard.setSubcategory(subcategory);
        return addFlashcard(flashcard);
    }

    private Timestamp insertLastViewed(Long id) {
        String sql = "INSERT INTO flashcard_last_view (flashcard_id, view_timestamp) VALUES (?, clock_timestamp()) RETURNING view_timestamp";
        return jdbcTemplate.queryForObject(sql,Timestamp.class, id);
    }
}