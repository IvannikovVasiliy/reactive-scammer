CREATE TABLE LOGS (
                          id bigserial not null,
                          id_payer_card_number varchar(150) not null,
                          query varchar(8000) not null,
                          action varchar(15) not null
);

ALTER TABLE LOGS
    ADD CONSTRAINT LOGS_PK PRIMARY KEY(id);