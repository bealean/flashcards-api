package com.bealean.flashcardzap_api.model;

import com.bealean.flashcardzap_api.utility.InputScrubber;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

public class Flashcard {
    private Long id;
    @NotBlank(message = "Front must not be blank.")
    @Size(max = 1000, message = "Front cannot be more than 1000 characters.")
    private String front;
    @NotBlank(message = "Back must not be blank.")
    @Size(max = 1000, message = "Back cannot be more than 1000 characters.")
    private String back;
    @Size(max = 30, message = "Area cannot be more than 30 characters.")
    @Pattern(regexp = "((?!^[ ]*all[ ]*$).)*", message = "Area name cannot be 'all'. " +
            "Please enter a name that does not match 'all' when whitespace is trimmed and case is ignored.", flags = {Pattern.Flag.CASE_INSENSITIVE})
    @Pattern(regexp = "^[a-zA-Z0-9\\._~ -]*$", message = "Area includes invalid characters. Allowed characters are letters: A-Z a-z, numbers, and: -_ .~")
    private String area;
    @Size(max = 30, message = "Category cannot be more than 30 characters.")
    @Pattern(regexp = "((?!^[ ]*all[ ]*$).)*", message = "Category name cannot be 'all'. " +
            "Please enter a name that does not match 'all' when whitespace is trimmed and case is ignored.", flags = {Pattern.Flag.CASE_INSENSITIVE})
    @Pattern(regexp = "^[a-zA-Z0-9\\._~ -]*$", message = "Category includes invalid characters. Allowed characters are letters: A-Z a-z, numbers, and: -_ .~")
    private String category;
    @Size(max = 30, message = "Subcategory cannot be more than 30 characters.")
    @Pattern(regexp = "((?!^[ ]*all[ ]*$).)*", message = "Subcategory name cannot be 'all'. " +
            "Please enter a name that does not match 'all' when whitespace is trimmed and case is ignored.", flags = {Pattern.Flag.CASE_INSENSITIVE})
    @Pattern(regexp = "^[a-zA-Z0-9\\._~ -]*$", message = "Subcategory includes invalid characters. Allowed characters are letters: A-Z a-z, numbers, and: -_ .~")
    private String subcategory;
    private Timestamp lastViewed;

    public Long getId() {
        return id;
    }

    public String getFront() {
        return front;
    }

    public String getBack() {
        return back;
    }

    public String getArea() {
        return area;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public Timestamp getLastViewed() {
        return lastViewed;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public void setArea(String area) {
        this.area = InputScrubber.trimStringAndSetEmptyToNull(area);
    }

    public void setCategory(String category) {
        this.category = InputScrubber.trimStringAndSetEmptyToNull(category);
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = InputScrubber.trimStringAndSetEmptyToNull(subcategory);
    }

    public void setLastViewed(Timestamp lastViewed) {
        this.lastViewed = lastViewed;
    }

    @Override
    public String toString() {
        return "Flashcard{" +
                "id=" + id +
                ", front='" + front + '\'' +
                ", back='" + back + '\'' +
                ", area='" + area + '\'' +
                ", category='" + category + '\'' +
                ", subcategory='" + subcategory + '\'' +
                ", lastViewed='" + lastViewed + '\'' +
                '}';
    }
}
