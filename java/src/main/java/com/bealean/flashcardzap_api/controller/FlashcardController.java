package com.bealean.flashcardzap_api.controller;

import com.bealean.flashcardzap_api.dao.FlashcardDAO;
import com.bealean.flashcardzap_api.model.Flashcard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin
public class FlashcardController {

    @Autowired
    FlashcardDAO flashcardDAO;

    @RequestMapping(path = "/new-flashcard", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Flashcard addFlashcard(@Valid @RequestBody Flashcard flashcard) {
        return flashcardDAO.addFlashcard(flashcard);
    }

}
