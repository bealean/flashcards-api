package com.bealean.flashcardzap_api.utility;

import com.bealean.flashcardzap_api.model.Flashcard;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ImportUtility {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("What is the full path and name of the csv file? ");
        String csv = scanner.nextLine();
        ImportUtility importUtility = new ImportUtility();
        importUtility.importFlashcards(csv);
    }

    private void importFlashcards(String csv) {
        final int EXPECTED_FIELD_COUNT = 6;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        File file = new File(csv);
        try (Scanner scanner = new Scanner(file)) {
            //Skip Header
            if (scanner.hasNext()) {
                scanner.nextLine();
            }
            while (scanner.hasNext()) {
                Pattern pattern = Pattern.compile("((?<!\")\",\"|((?<=(?<!\")\"\")\",\")|((?<=(?<!\")\"\"\"\")\",\"))");
                /* Pattern matches the expected delimiter of: ","
                * Each field in the CSV is enclosed in double quotes and the fields are separated by commas.
                * Within fields any double quotes are represented by a pair of double quotes.
                *
                * "Field 1","Field 2 contains comma separated quoted words ""quoted word"",""another word""","Field 3"
                *
                * Only split where "," is not preceded by a double quote, or where it is preceded by an even number of double quotes.
                * Used Negative Lookbehind to check characters before "," without capturing them.
                * Specified separate cases because lookbehind needs to be fixed-length in Java.
                * 1. "," is preceded by a character other than a quote.
                * 2. "," is preceded by two quotes, preceded by a character other than a quote.
                * 3. "," is preceded by four quotes, preceded by a character other than a quote. */

                String flashcardLine = scanner.nextLine();
                String[] flashcardFields = pattern.split(flashcardLine);
                // Remove initial double quote
                String row = flashcardFields[0].substring(1);
                /* First field for any card row should be an integer.
                Confirm this for each card row before adding new card to database. */
                try {
                    Integer.parseInt(row);
                } catch (NumberFormatException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unexpected CSV format. " +
                            "CSV should have a a header row of any format and " +
                            "data rows with six comma separated fields with each field enclosed in double quotes: " +
                            "1. Integer representing row number, " +
                            "2. Card Front String, " +
                            "3. Card Back String, " +
                            "4. Card Area String, " +
                            "5. Card Category String, " +
                            "6. Card Subcategory String");
                }
                /* Card rows can span multiple lines due to line breaks.
                If the expected number of fields were not found on one line
                read more lines until the expected number of fields are found. */
                if (flashcardFields.length < EXPECTED_FIELD_COUNT) {
                    StringBuilder flashcardLines = new StringBuilder(flashcardLine);
                    int fieldCount = flashcardFields.length;
                    while (fieldCount < EXPECTED_FIELD_COUNT) {
                        flashcardLines.append("\n");
                        flashcardLines.append(scanner.nextLine());
                        fieldCount = pattern.split(flashcardLines).length;
                    }
                    flashcardFields = pattern.split(flashcardLines);
                }
                /* CSV has two double quotes for every double quote within the Front and Back fields,
                * so replace two consecutive double quotes with one double quote.
                * Double quotes not allowed for other fields. */
                String front = flashcardFields[1].replaceAll("\"\"", "\"");
                String back = flashcardFields[2].replaceAll("\"\"", "\"");
                Flashcard flashcard = new Flashcard();
                flashcard.setFront(front);
                flashcard.setBack(back);
                flashcard.setArea(flashcardFields[3]);
                flashcard.setCategory(flashcardFields[4]);
                String subcategory = flashcardFields[5];
                // Remove trailing double quote from last field
                flashcard.setSubcategory(subcategory.substring(0, subcategory.length() - 1));
                HttpEntity<Flashcard> entity = new HttpEntity<>(flashcard, headers);
                try {
                    restTemplate.postForObject("http://localhost:8080/new-flashcard", entity, Flashcard.class);
                } catch (Exception e) {
                    System.out.println("Caught exception: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Caught exception: " + e.getMessage());
        }
    }
}
