package com.bealean.flashcards_api.dao;

import com.bealean.flashcards_api.model.Flashcard;

public interface FlashcardDAO {
    Flashcard addFlashcard(Flashcard flashcard);
    Flashcard getCardById(Long id);
    Flashcard getNext(String area, String category, String subcategory);
}
