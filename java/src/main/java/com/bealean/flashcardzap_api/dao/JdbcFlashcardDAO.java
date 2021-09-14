package com.bealean.flashcardzap_api.dao;

import com.bealean.flashcardzap_api.model.Flashcard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Component
public class JdbcFlashcardDAO implements FlashcardDAO {
    @Autowired
    AreaDAO areaDAO;

    @Autowired
    CategoryDAO categoryDAO;

    @Autowired
    SubcategoryDAO subcategoryDAO;

    @Autowired
    AreaCategorySubcategoryDAO areaCategorySubcategoryDAO;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcFlashcardDAO(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.areaDAO = new JdbcAreaDAO(jdbcTemplate);
        this.categoryDAO = new JdbcCategoryDAO(jdbcTemplate);
        this.subcategoryDAO = new JdbcSubcategoryDAO(jdbcTemplate);
        this.areaCategorySubcategoryDAO = new JdbcAreaCategorySubcategoryDAO(namedParameterJdbcTemplate, jdbcTemplate);
    }

    /* Adding a Flashcard may require multiple database updates to add Area, Category, Subcategory,
       and mapping records. Transactional annotation will rollback all updates, if an
       individual update fails. */
    @Override
    @Transactional(rollbackFor = { ResponseStatusException.class })
    public Flashcard addFlashcard(Flashcard flashcard) {
        Long id;
        String front = flashcard.getFront();
        String back = flashcard.getBack();
        Long areaId;
        Long categoryId;
        Long subcategoryId;

        /* Setters for Flashcard handle setting any empty string Area, Category, or Subcategory values to null.
        * Set IDs to null if name is null, otherwise get an existing ID or set the ID to -1L,
        * if it doesn't already exist.*/

        String area = flashcard.getArea();
        if (area == null) {
            areaId = null;
        } else {
            areaId = areaDAO.getAreaIdByName(area);
        }
        String category = flashcard.getCategory();
        if (category == null) {
            categoryId = null;
        } else {
            categoryId = categoryDAO.getCategoryIdByName(category);
        }
        String subcategory = flashcard.getSubcategory();
        if (subcategory == null) {
            subcategoryId = null;
        } else {
            subcategoryId = subcategoryDAO.getSubcategoryIdByName(subcategory);
        }

        /* If a Category is specified without an Area, throw exception and don't add card. */
        if (area == null && category != null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Area must be specified, if Category is specified. Card not added.");
        }

        /* If a Subcategory is specified without a Category, throw exception and don't add card. */
        if (category == null && subcategory != null) {
            if (area == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Area and Category must be specified, if Subcategory is specified. Card not added.");
            } else {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Category must be specified, if Subcategory is specified. Card not added.");
            }
        }

        // If card with same details already exists, return it.
        Map<String, Object> params = new HashMap<>();
        params.put("front", flashcard.getFront());
        params.put("back", flashcard.getBack());
        params.put("area_id", areaId);
        params.put("category_id", categoryId);
        params.put("subcategory_id", subcategoryId);
        /*  Ids could be bigint or null. Added CAST to NULL checks because Postgres
        * couldn't determine the data type of null parameters otherwise.
        * This was the case even if all of the parameters were Long values and Long was
        * specified as the data type for the values in the parameter map */
        String sql = "SELECT COALESCE(MAX(id),-1) FROM flashcards " +
                "WHERE front = :front AND back = :back " +
                "AND (area_id = :area_id OR (CAST(:area_id AS bigint) IS NULL) AND area_id IS NULL) " +
                "AND (category_id = :category_id OR (CAST(:category_id AS bigint) IS NULL) AND category_id IS NULL) " +
                "AND (subcategory_id = :subcategory_id OR (CAST(:subcategory_id AS bigint) IS NULL) AND subcategory_id IS NULL)";
        try {
            id = namedParameterJdbcTemplate.queryForObject(sql,params, Long.class);
            if (id != null && id >= 0) {
                return queryForCardByIdAndMap(id);
            }
        } catch (DataAccessException e) {
            System.out.println("Caught Exception: "+ e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Failure checking if card already exists. Card not added.");
        }

        /* If no area, category, or subcategory is specified, add card without them. */
        if (area == null) {
            return insertCard(front, back, null, null, null);
        } else if (category == null && areaId >= 0){
            /* If area specified without category (or subcategory) and
               area already exists in database, insert card and return. */
                return insertCard(front, back, areaId, null, null);
        }

        // If card has existing area and category, and either a null subcategory or an existing subcategory
        if (areaId >= 0 && categoryId >= 0 &&
                (subcategoryId == null || subcategoryId >= 0)) {
                /* If the area_category_subcategory table already has a record corresponding
                   to the area, category, and subcategory of the card,
                   insert record for card in flashcards table and return flashcard. */
            if (areaCategorySubcategoryDAO.doesMappingExist(areaId, categoryId, subcategoryId)) {
                return insertCard(front, back, areaId, categoryId, subcategoryId);
            }
        }

        /* For other cases multiple database updates are required to insert card and dependencies in database.
         * catch exceptions from individual updates and rethrow with message that the flashcard and
         * dependencies were not added. */
        try {
            if (areaId < 0 ){
                areaId = areaDAO.addArea(area);
            }
            if (categoryId != null && categoryId < 0) {
                categoryId = categoryDAO.addCategory(category);
            }
            if (subcategoryId != null && subcategoryId < 0) {
                subcategoryId = subcategoryDAO.addSubcategory(subcategory);
            }
            // addMapping has logic to determine if new mapping is needed
            areaCategorySubcategoryDAO.addMapping(areaId, categoryId, subcategoryId);
            return insertCard(flashcard.getFront(),flashcard.getBack(), areaId, categoryId, subcategoryId);
        } catch (ResponseStatusException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Flashcard and dependencies not added. " + e.getReason());
        }
    }

    @Override
    public Flashcard getNext(String area, String category, String subcategory) {
        return null;
    }

    private Flashcard insertCard(String front, String back, Long areaId, Long categoryId, Long subcategoryId) {
        String sql = "INSERT INTO flashcards (front, back, area_id, category_id, subcategory_id) VALUES (?,?,?,?,?) RETURNING id";
        try {
            Long id =  jdbcTemplate.queryForObject(sql, Long.class, front, back, areaId, categoryId, subcategoryId);
            return queryForCardByIdAndMap(id);
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Flashcard failed to be added.");
        }
    }

    private Flashcard queryForCardByIdAndMap(Long id) {
        String sql = "SELECT f.id AS \"id\", front, back, a.area_name AS \"area\", c.category_name AS \"category\", s.subcategory_name " +
                "AS \"subcategory\", fv.view_timestamp AS \"lastViewed\" FROM flashcards f " +
                "LEFT OUTER JOIN areas a ON f.area_id = a.id " +
                "LEFT OUTER JOIN categories c ON f.category_id = c.id " +
                "LEFT OUTER JOIN subcategories s ON f.subcategory_id = s.id " +
                "LEFT OUTER JOIN flashcard_views fv ON f.id = fv.flashcard_id " +
                "WHERE f.id = ?";
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, id);
            Flashcard flashcard = new Flashcard();
            if (result.next()){
                flashcard.setId(result.getLong("id"));
                flashcard.setFront(result.getString("front"));
                flashcard.setBack(result.getString("back"));
                flashcard.setArea(result.getString("area"));
                flashcard.setCategory(result.getString("category"));
                flashcard.setSubcategory(result.getString("subcategory"));
                flashcard.setLastViewed(result.getTimestamp("lastViewed"));
            }
            return flashcard;
    }
}
