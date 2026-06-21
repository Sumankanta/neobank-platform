-- V13: Add extended user profile fields to support full registration form
-- Fields captured: dob, gender, nationality, phone, id_type, id_number,
--                  street, city, state, zip_code, nominee_name,
--                  nominee_relationship, nominee_dob, account_type, currency

ALTER TABLE users
    ADD COLUMN phone            VARCHAR(15)     NULL AFTER full_name,
    ADD COLUMN dob              DATE            NULL AFTER phone,
    ADD COLUMN gender           VARCHAR(20)     NULL AFTER dob,
    ADD COLUMN nationality      VARCHAR(50)     NULL AFTER gender,
    ADD COLUMN id_type          VARCHAR(30)     NULL AFTER nationality,
    ADD COLUMN id_number        VARCHAR(50)     NULL AFTER id_type,
    ADD COLUMN street           VARCHAR(255)    NULL AFTER id_number,
    ADD COLUMN city             VARCHAR(100)    NULL AFTER street,
    ADD COLUMN state            VARCHAR(100)    NULL AFTER city,
    ADD COLUMN zip_code         VARCHAR(10)     NULL AFTER state,
    ADD COLUMN nominee_name     VARCHAR(100)    NULL AFTER zip_code,
    ADD COLUMN nominee_relation VARCHAR(50)     NULL AFTER nominee_name,
    ADD COLUMN nominee_dob      DATE            NULL AFTER nominee_relation,
    ADD COLUMN preferred_account_type VARCHAR(30) NULL AFTER nominee_dob,
    ADD COLUMN currency         VARCHAR(10)     NULL DEFAULT 'INR' AFTER preferred_account_type;
