# FlashcardZap API

## Introduction
Java API for managing and retrieving flashcards. 

The original FlashcardZap is a standalone Java web application using JavaServer Pages. This API version is designed for use with different types of client applications.

---
## Architecture

Java API uses:

- Spring MVC,
- Spring JdbcTemplate & NamedParameterJdbcTemplate,
- JUnit 5,
- and a PostgreSQL database.

### Details

#### Bean Validation

- JSON objects received from client requests are validated using the @Valid annotation on the controller method and Bean Validation on the corresponding Java model classes, including @Pattern annotations to check that values match expected regular expression patterns. 
- Bean Validation is disabled for exception handling testing using a MockBean of LocalValidatorFactoryBean.

#### Transactional Rollback

- The @Transactional annotation is used to rollback all database updates performed in methods with the annotation if any individual updates threw a ResponseStatusException.
- The ResponseStatusException with the details of the failed update is caught by the calling method and re-thrown with the additional information that the transaction as a whole was not successful.

#### NamedParameterJdbcTemplate

- NamedParameterJdbcTemplate is used with a Map of parameters instead of JdbcTemplate to improve readability in WHERE clauses handling variable values that could have a value or could be NULL.
- NamedParameterJdbcTemplate is also used where different queries with different numbers of parameters are executed conditionally.

#### Recording Views

- The flashcard_last_view table includes the most recent view Timestamp for each card. The most recent view of cards is accessed frequently in deciding which card to display next. Insert and Update triggers on that table populate the flashcard_views table, which stores all card views for reporting. 
- Both triggers call the same predefined trigger function. The schema.sql script must be run from a tool that supports function creation, such as pgAdmin (DBVisualizer Free does not support function creation).
- When recording Timestamps in the database, the PostgreSQL clock_timestamp() function is used to return the current timestamp, rather than the now() function, which returns the timestamp for the start of the transaction.  The update tests execute multiple statements in a single transaction and compare the Timestamps of different records, so the now() function could not be used in those cases.
- Tests were originally checking database Timestamps against a Timestamp created with "new Timestamp(System.currentTimeMillis())", but there were intermittent false failures due to slight discrepancies between the Timestamps. Tests were updated to use the PostgreSQL clock_timestamp() function instead.

#### Import Utility

- Internal ImportUtility available in the "utility" folder. It reads cards from a CSV and adds the cards to the database.
- Utility prompts for the path and name of the input file.
- CSV should have a a header row of any format and data rows with six comma separated fields with each field enclosed in double quotes:
    1. Integer representing row number,
    2. Card Front String,
    3. Card Back String,
    4. Card Area String,
    5. Card Category String,
    6. Card Subcategory String
- Any double quote within the Front or Back fields should be represented by two double quotes. The API does not allow double quotes within the other fields.
- Line breaks and blank lines within Front and Back fields are supported.
- The expected field delimiter is double quote, comma, double quote (",").
- The regex pattern for splitting the lines of the csv into fields only matches the delimiter if it is not preceded by a double quote, or if it is preceded by an even number of double quotes,  because any double quotes within the fields will be duplicated. 
- Negative Lookbehind is used to check the characters before the delimiter. To limit memory consumption, the upper limit for matching preceding sets of double quotes is specified as 2 sets (or 4 preceding double quotes). This is adjustable with the LOOKBEHIND_QUOTE_PAIRS_UPPER_LIMIT constant.

#### Tests

A Test Driven Development approach was used for this project. The tests for this project use JUnit 5. 

##### DAO Tests
- An abstract JdbcDAOTest class includes configuration properties and methods, and helper methods used by multiple test classes that extend it.
- SingleConnectionDataSource is used to test data modification methods without committing the changes.
- The @Value annotation is used in the abstract JdbcDAOTest class to read the database properties from the application.properties file of the main application.
- Because @Value is not supported on static fields and the @BeforeAll method needs to be static, the Data Source is configured on the first run of the @BeforeEach method with a static boolean variable keeping track of whether the Data Source has already been configured. 
- The assertThrows assertion is used to check that the expected type of exceptions are thrown with the expected status and message.

##### REST Controller Tests
- Tests for REST Controller methods use Mockito to stub method calls to mocked DAO objects in order to specify what is returned.
- MockMvc is used to send requests to the REST Controller and verify the response.
- Custom JUnit assertion messages are specified for the MockMvc tests in this project. The TEnmo project has examples of using the MockMvcResultMatchers assertions and default messages.
- The @WebMvcTest annotation is used to instantiate only the web layer and the configuration needed for MVC tests.

##### Transactional Rollback Tests
- Transactional rollback is tested by calling the transactional method externally through the REST Controller using MockMvc requests.
- An uninitialized MockBean of LocalValidatorFactoryBean is used to disable Bean Validation in order to send invalid values to force exceptions.
- The @SpringBootTest and @AutoConfigureMockMvc annotations are used, rather than @WebMvcTest, because the tests require the full application context to be loaded.  

