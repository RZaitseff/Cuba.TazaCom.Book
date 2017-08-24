-- begin TAZACOM_AUTHOR
create table TAZACOM_AUTHOR (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    STATE varchar(50),
    FIRST_NAME varchar(50),
    MIDDLE_NAME varchar(50),
    LAST_NAME varchar(50),
    --
    primary key (ID)
)^
-- end TAZACOM_AUTHOR
-- begin TAZACOM_GENRE
create table TAZACOM_GENRE (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    STATE varchar(50),
    NAME varchar(50),
    --
    primary key (ID)
)^
-- end TAZACOM_GENRE
-- begin TAZACOM_BOOK
create table TAZACOM_BOOK (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    STATE varchar(50),
    AUTHOR_ID varchar(36),
    NAME varchar(50),
    YEAR_ integer,
    EDITION varchar(255),
    --
    primary key (ID)
)^
-- end TAZACOM_BOOK
-- begin TAZACOM_IMPORTING
create table TAZACOM_IMPORTING (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    STATE varchar(50) not null,
    URL varchar(255),
    DATE_ date not null,
    QUANTITY integer,
    --
    primary key (ID)
)^
-- end TAZACOM_IMPORTING
-- begin TAZACOM_BOOK_GENRE_LINK
create table TAZACOM_BOOK_GENRE_LINK (
    BOOK_ID varchar(36) not null,
    GENRE_ID varchar(36) not null,
    primary key (BOOK_ID, GENRE_ID)
)^
-- end TAZACOM_BOOK_GENRE_LINK
