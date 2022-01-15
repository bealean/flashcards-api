package com.bealean.flashcardzap_api.dao;

import java.util.List;

public interface SubcategoryDAO {
    long getSubcategoryIdByName(String subcategoryName);
    List<String> getSubcategories(String areaName, String categoryName);
    long addSubcategory(String subcategoryName);
}
