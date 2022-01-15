package com.bealean.flashcardzap_api.utility;

public abstract class InputScrubber {

    public static String trimStringAndSetEmptyToNull(String stringToCheck) {
        if (stringToCheck != null) {
            if (stringToCheck.trim().equals("")) {
                return null;
            } else {
                return stringToCheck.trim();
            }
        } else {
            return null;
        }
    }
}
