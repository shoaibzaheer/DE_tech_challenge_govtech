
CREATE DATABASE tech_challenge
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

BEGIN;


CREATE TABLE IF NOT EXISTS transactions
(
    transaction_id serial NOT NULL,
    member_id integer,
    total_price numeric(10, 2) NOT NULL,
    total_weight numeric(6, 2) NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    status character varying(10)  NOT NULL,
    modified_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT transactions_pkey PRIMARY KEY (transaction_id)
);

CREATE TABLE IF NOT EXISTS members
(
    name character varying(100) NOT NULL,
    email character varying(100)  NOT NULL,
    date_of_birth character varying(8)  NOT NULL,
    mobile_no character varying(8)  NOT NULL,
    first_name character varying(50)  NOT NULL,
    last_name character varying(50)  NOT NULL,
    above_18 boolean NOT NULL,
    membership_id integer NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    modified_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT members_pkey PRIMARY KEY (membership_id),
    CONSTRAINT members_email_key UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS transactionitems
(
    transaction_id integer NOT NULL,
    item_id integer NOT NULL,
    quantity integer NOT NULL,
    CONSTRAINT transactionitems_pkey PRIMARY KEY (transaction_id, item_id)
);

CREATE TABLE IF NOT EXISTS items
(
    item_id serial NOT NULL,
    item_name character varying(100) NOT NULL,
    item_manufacturer character varying(100) NOT NULL,
    item_cost numeric(10, 2) NOT NULL,
    item_weight numeric(6, 2) NOT NULL,
    created_at timestamp  NOT NULL DEFAULT now(),
    modified_at timestamp  NOT NULL DEFAULT now(),
    CONSTRAINT items_pkey PRIMARY KEY (item_id)
);

ALTER TABLE IF EXISTS public.transactions
    ADD CONSTRAINT transactions_member_id_fkey FOREIGN KEY (member_id)
    REFERENCES public.members (membership_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.transactionitems
    ADD CONSTRAINT transactionitems_item_id_fkey FOREIGN KEY (item_id)
    REFERENCES public.items (item_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;


ALTER TABLE IF EXISTS public.transactionitems
    ADD CONSTRAINT transactionitems_transaction_id_fkey FOREIGN KEY (transaction_id)
    REFERENCES public.transactions (transaction_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

END;