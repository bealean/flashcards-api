package com.bealean.flashcards_api.dao;

import java.util.List;

public interface CategoryDAO {
    List<String> getCategoriesForArea(String areaName);
    long addCategory(String categoryName);
    long getCategoryIdByName(String categoryName);
}
