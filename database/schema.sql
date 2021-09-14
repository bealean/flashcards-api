BEGIN TRANSACTION;

DROP TABLE IF EXISTS flashcard_views;
DROP SEQUENCE IF EXISTS seq_flashcard_views_id;
DROP TABLE IF EXISTS area_category_subcategory;
DROP TABLE IF EXISTS flashcards;
DROP SEQUENCE IF EXISTS seq_flashcards_id;
DROP TABLE IF EXISTS subcategories;
DROP SEQUENCE IF EXISTS seq_subcategories_id;
DROP TABLE IF EXISTS categories;
DROP SEQUENCE IF EXISTS seq_categories_id;
DROP TABLE IF EXISTS areas;
DROP SEQUENCE IF EXISTS seq_areas_id;

CREATE SEQUENCE seq_areas_id
  INCREMENT BY 1
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;

CREATE SEQUENCE seq_categories_id
  INCREMENT BY 1
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;

CREATE SEQUENCE seq_subcategories_id
  INCREMENT BY 1
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;

CREATE SEQUENCE seq_flashcards_id
  INCREMENT BY 1
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;

CREATE SEQUENCE seq_flashcard_views_id
  INCREMENT BY 1
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;

CREATE TABLE areas (
    id bigint DEFAULT nextval('seq_areas_id'::regclass) NOT NULL,
    area_name varchar(30) NOT NULL UNIQUE,
    CONSTRAINT pk_areas PRIMARY KEY (id)
);

CREATE TABLE categories (
    id bigint DEFAULT nextval('seq_categories_id'::regclass) NOT NULL,
    category_name varchar(30) NOT NULL UNIQUE,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE subcategories (
    id bigint DEFAULT nextval('seq_subcategories_id'::regclass) NOT NULL,
    subcategory_name varchar(30) NOT NULL UNIQUE,
    CONSTRAINT pk_subcategories PRIMARY KEY (id)
);

CREATE TABLE area_category_subcategory (
    area_id bigint NOT NULL,
    category_id bigint NOT NULL,
    subcategory_id bigint,
    CONSTRAINT uc_area_category_subcategory_area_id_category_id_subcategory_id UNIQUE (area_id,category_id,subcategory_id),
    CONSTRAINT fk_area_category_subcategory_area FOREIGN KEY (area_id) REFERENCES areas (id),
    CONSTRAINT fk_area_category_subcategory_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_area_category_subcategory_subcategory FOREIGN KEY (subcategory_id) REFERENCES subcategories (id)
);

CREATE TABLE flashcards (
    id bigint DEFAULT nextval('seq_flashcards_id'::regclass) NOT NULL,
    front varchar(1000) NOT NULL,
    back varchar(1000) NOT NULL,
    area_id bigint,
    category_id bigint,
    subcategory_id bigint,
    CONSTRAINT pk_flashcards PRIMARY KEY (id),
    CONSTRAINT uc_flashcards_front_back_area_id__category_id_subcategory_id UNIQUE (front, back, area_id,category_id,subcategory_id),
    CONSTRAINT fk_flashcards_area FOREIGN KEY (area_id) REFERENCES areas (id),
    CONSTRAINT fk_flashcards_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_flashcards_subcategory FOREIGN KEY (subcategory_id) REFERENCES subcategories (id)
);

CREATE INDEX ix_fk_flashcards_category ON flashcards(category_id);
CREATE INDEX ix_fk_flashcards_subcategory ON flashcards(subcategory_id);

CREATE TABLE flashcard_views (
    id bigint DEFAULT nextval('seq_flashcard_views_id'::regclass) NOT NULL,
    flashcard_id bigint NOT NULL,
    view_timestamp timestamp with time zone NOT NULL,
    CONSTRAINT pk_flashcard_views PRIMARY KEY (id),
    CONSTRAINT fk_flashcard_views_flashcard FOREIGN KEY (flashcard_id) REFERENCES flashcards (id)
);

    CREATE INDEX ix_fk_flashcard_views_flashcard ON flashcard_views(flashcard_id);
    CREATE INDEX ix_flashcard_views_view_timestamp ON flashcard_views(view_timestamp);

COMMIT TRANSACTION;