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

#### Tests

A Test Driven Development approach was used for this project. The tests for this project use JUnit 5. 

- An abstract JdbcDAOTest class includes configuration properties and methods, and helper methods used by multiple test classes that extend it.
- SingleConnectionDataSource is used to test data modification methods without committing the changes.
- The @Value annotation is used in the abstract JdbcDAOTest class to read the database properties from the application.properties file of the main application.
- Because @Value is not supported on static fields and the @BeforeAll method needs to be static, the Data Source is configured on the first run of the @BeforeEach method with a static boolean variable keeping track of whether the Data Source has already been configured. 
- The assertThrows assertion is used to check that the expected type of exceptions are thrown with the expected status and message.
- Spring's MockMvc is used to send requests to the REST Controller to test that the database updates in methods with the @Transactional annotation are fully rolled back if the specified exception type is thrown.
- An uninitialized MockBean of LocalValidatorFactoryBean is used in tests where needed to disable Bean Validation in order to send invalid values to force exceptions.
