package com.bealean.flashcards_api.controller;

import com.bealean.flashcards_api.dao.FlashcardDAO;
import com.bealean.flashcards_api.model.Flashcard;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest
class FlashcardControllerGetNextTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlashcardDAO flashcardDAO;

    private static final String TEST_AREA = "JUnit Test Area";
    private static final String TEST_CATEGORY = "JUnit Test Category";
    private static final String TEST_SUBCATEGORY = "JUnit Test Subcategory";

    /* Parameter values should not be required and no default values should be set.
    Null and empty String values are set to "all" in DAO method and this is tested in the DAO tests. */

    @Test
    public void getNextCard_noParameters_returnsOkCallsGetNextWithNullParameters() throws Exception {
        Flashcard flashcard = new Flashcard();
        String expectedCardFront = "Test No Parameters";
        flashcard.setFront(expectedCardFront);
        Mockito.when(flashcardDAO.getNext(null, null, null)).thenReturn(flashcard);

        MvcResult mvcResult = mockMvc.perform(get("/get-next-card"))
                .andExpect(result -> assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(),
                        "getNextCard does not require parameters and " +
                                "returns OK status for request with no parameters")).andReturn();

        String actualCardFront = getCardFrontFromResult(mvcResult);

        assertEquals(expectedCardFront, actualCardFront,
                "getNextCard for request with no parameters does not set default values " +
                        "and calls getNext with null parameters");
    }

    @Test
    public void getNextCard_noArea_returnsOkCallsGetNextWithNullArea() throws Exception {
        Flashcard flashcard = new Flashcard();
        String expectedCardFront = "Test No Area";
        flashcard.setFront(expectedCardFront);
        Mockito.when(flashcardDAO.getNext(null, TEST_CATEGORY, TEST_SUBCATEGORY)).thenReturn(flashcard);

        MvcResult mvcResult = mockMvc.perform(get("/get-next-card")
                .param("category", TEST_CATEGORY)
                .param("subcategory", TEST_SUBCATEGORY))
                .andExpect(result -> assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(),
                        "getNextCard returns OK status for request with no Area parameter")).andReturn();

        String actualCardFront = getCardFrontFromResult(mvcResult);

        assertEquals(expectedCardFront, actualCardFront,
                "getNextCard for request with no Area parameter does not set a default value " +
                        "and calls getNext with null Area parameter");
    }

    @Test
    public void getNextCard_noCategory_returnsOkCallsGetNextWithNullCategory() throws Exception {
        Flashcard flashcard = new Flashcard();
        String expectedCardFront = "Test No Category";
        flashcard.setFront(expectedCardFront);
        Mockito.when(flashcardDAO.getNext(TEST_AREA, null, TEST_SUBCATEGORY)).thenReturn(flashcard);

        MvcResult mvcResult = mockMvc.perform(get("/get-next-card")
                .param("area", TEST_AREA)
                .param("subcategory", TEST_SUBCATEGORY))
                .andExpect(result -> assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(),
                        "getNextCard returns OK status for request with no Category parameter")).andReturn();

        String actualCardFront = getCardFrontFromResult(mvcResult);

        assertEquals(expectedCardFront, actualCardFront,
                "getNextCard for request with no Category parameter does not set a default value " +
                        "and calls getNext with null Category parameter");
    }

    @Test
    public void getNextCard_noSubcategory_returnsOkCallsGetNextWithNullSubcategory() throws Exception {
        Flashcard flashcard = new Flashcard();
        String expectedCardFront = "Test No Subcategory";
        flashcard.setFront(expectedCardFront);
        Mockito.when(flashcardDAO.getNext(TEST_AREA, TEST_CATEGORY, null)).thenReturn(flashcard);

        MvcResult mvcResult = mockMvc.perform(get("/get-next-card")
                .param("area", TEST_AREA)
                .param("category", TEST_CATEGORY))
                .andExpect(result -> assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(),
                        "getNextCard returns OK status for request with no Subcategory parameter")).andReturn();

        String actualCardFront = getCardFrontFromResult(mvcResult);

        assertEquals(expectedCardFront, actualCardFront,
                "getNextCard for request with no Subcategory parameter does not set a default value " +
                        "and calls getNext with null Subcategory parameter");
    }

    @Test
    public void getNextCard_emptyArea_callsGetNextWithEmptyArea() throws Exception {
        Flashcard flashcard = new Flashcard();
        String expectedCardFront = "Test Empty Area";
        flashcard.setFront(expectedCardFront);
        Mockito.when(flashcardDAO.getNext("", TEST_CATEGORY, TEST_SUBCATEGORY)).thenReturn(flashcard);

        MvcResult mvcResult = mockMvc.perform(get("/get-next-card")
                .param("area", "")
                .param("category", TEST_CATEGORY)
                .param("subcategory", TEST_SUBCATEGORY))
                .andReturn();

        String actualCardFront = getCardFrontFromResult(mvcResult);

        assertEquals(expectedCardFront, actualCardFront,
                "getNextCard for request with empty String Area parameter does not set a default value " +
                        "and calls getNext with empty String Area parameter");
    }

    @Test
    public void getNextCard_emptyCategory_callsGetNextWithEmptyCategory() throws Exception {
        Flashcard flashcard = new Flashcard();
        String expectedCardFront = "Test Empty Category";
        flashcard.setFront(expectedCardFront);
        Mockito.when(flashcardDAO.getNext(TEST_AREA, "", TEST_SUBCATEGORY)).thenReturn(flashcard);

        MvcResult mvcResult = mockMvc.perform(get("/get-next-card")
                .param("area", TEST_AREA)
                .param("category", "")
                .param("subcategory", TEST_SUBCATEGORY))
                .andReturn();

        String actualCardFront = getCardFrontFromResult(mvcResult);

        assertEquals(expectedCardFront, actualCardFront,
                "getNextCard for request with empty String Category parameter does not set a default value " +
                        "and calls getNext with empty String Category parameter");
    }

    @Test
    public void getNextCard_emptySubcategory_callsGetNextWithEmptySubcategory() throws Exception {
        Flashcard flashcard = new Flashcard();
        String expectedCardFront = "Test Empty Subcategory";
        flashcard.setFront(expectedCardFront);
        Mockito.when(flashcardDAO.getNext(TEST_AREA, TEST_CATEGORY, "")).thenReturn(flashcard);

        MvcResult mvcResult = mockMvc.perform(get("/get-next-card")
                .param("area", TEST_AREA)
                .param("category", TEST_CATEGORY)
                .param("subcategory", ""))
                .andReturn();

        String actualCardFront = getCardFrontFromResult(mvcResult);

        assertEquals(expectedCardFront, actualCardFront,
                "getNextCard for request with empty String Subcategory parameter does not set a default value " +
                        "and calls getNext with empty String Subcategory parameter");
    }

    @Test
    public void getNextCard_allParametersWithValues_returnsOkCallsGetNextWithParameterValues() throws Exception {
        Flashcard flashcard = new Flashcard();
        String expectedCardFront = "Test All Parameters";
        flashcard.setFront(expectedCardFront);
        Mockito.when(flashcardDAO.getNext(TEST_AREA, TEST_CATEGORY, TEST_SUBCATEGORY)).thenReturn(flashcard);

        MvcResult mvcResult = mockMvc.perform(get("/get-next-card")
                .param("area", TEST_AREA)
                .param("category", TEST_CATEGORY)
                .param("subcategory", TEST_SUBCATEGORY))
                .andExpect(result -> assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(),
                        "getNextCard returns OK status for request with values specified for all parameters")).andReturn();

        String actualCardFront = getCardFrontFromResult(mvcResult);

        assertEquals(expectedCardFront, actualCardFront,
                "getNextCard for request with values for all parameters " +
                        "calls getNext with the parameter values");
    }

    private String getCardFrontFromResult(MvcResult mvcResult) throws UnsupportedEncodingException {
        String front = "";
        String response = mvcResult.getResponse().getContentAsString();
        if (mvcResult.getResponse().getContentType() != null && mvcResult.getResponse().getContentType().equals("application/json")) {
            front += JsonPath.parse(response).read("$.front");
        }
        return front;
    }

}