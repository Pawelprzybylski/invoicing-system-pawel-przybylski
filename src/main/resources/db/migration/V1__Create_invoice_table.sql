CREATE TABLE public.invoice
(
    id      bigserial           NOT NULL,
    date    date                NOT NULL,
    number  character varying(50)   NOT NULL,
    PRIMARY KEY (id)
    );