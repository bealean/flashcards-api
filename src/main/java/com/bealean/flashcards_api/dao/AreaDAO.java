package com.bealean.flashcardzap_api.dao;

import java.util.List;

public interface AreaDAO {
    long addArea(String areaName);
    long getAreaIdByName(String areaName);
    List<String> getAreas();
}
