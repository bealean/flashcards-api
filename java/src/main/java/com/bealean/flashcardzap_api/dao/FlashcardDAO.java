package com.bealean.flashcardzap_api.dao;

import com.bealean.flashcardzap_api.model.Flashcard;

public interface FlashcardDAO {
    Flashcard addFlashcard(Flashcard flashcard);
    Flashcard getNext(String area, String category, String subcategory);
}
