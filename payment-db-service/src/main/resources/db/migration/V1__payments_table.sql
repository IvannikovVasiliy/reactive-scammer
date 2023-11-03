CREATE TABLE PAYMENTS (
                          id bigserial not null,
                          payer_card_number varchar(150) not null,
                          receiver_card_number varchar(150) not null,
                          latitude double precision not null,
                          longitude double precision not null,
                          date timestamp with time zone not null
);

ALTER TABLE PAYMENTS
    ADD CONSTRAINT PAYMENTS_PK PRIMARY KEY(id);

CREATE INDEX PAYMENTS_I1 ON PAYMENTS(date);