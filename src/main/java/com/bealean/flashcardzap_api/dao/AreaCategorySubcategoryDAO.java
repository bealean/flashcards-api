package com.bealean.flashcardzap_api.dao;

public interface AreaCategorySubcategoryDAO {
    boolean doesMappingExist(Long areaId, Long categoryId, Long subcategoryId);
    int addMapping(Long areaId, Long categoryId, Long subcategoryId);
}
