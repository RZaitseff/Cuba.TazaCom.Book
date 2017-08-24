-- begin TAZACOM_GENRE
create unique index IDX_TAZACOM_GENRE_UNIQ_NAME on TAZACOM_GENRE (NAME) ^
-- end TAZACOM_GENRE
-- begin TAZACOM_BOOK
alter table TAZACOM_BOOK add constraint FK_TAZACOM_BOOK_AUTHOR foreign key (AUTHOR_ID) references TAZACOM_AUTHOR(ID)^
create index IDX_TAZACOM_BOOK_AUTHOR on TAZACOM_BOOK (AUTHOR_ID)^
-- end TAZACOM_BOOK
-- begin TAZACOM_BOOK_GENRE_LINK
alter table TAZACOM_BOOK_GENRE_LINK add constraint FK_TBGL_BOOK foreign key (BOOK_ID) references TAZACOM_BOOK(ID)^
alter table TAZACOM_BOOK_GENRE_LINK add constraint FK_TBGL_GENRE foreign key (GENRE_ID) references TAZACOM_GENRE(ID)^
-- end TAZACOM_BOOK_GENRE_LINK
