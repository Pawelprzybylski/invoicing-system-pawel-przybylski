ALTER TABLE invoice_entry
    ADD COLUMN vat_rate_tmp character varying(10);

UPDATE invoice_entry
SET vat_rate_tmp = CONCAT('VAT_', (SELECT name FROM vat WHERE invoice_entry.vat_rate = id));

ALTER TABLE invoice_entry
    DROP vat_rate;

DROP TABLE vat;

ALTER TABLE invoice_entry
    ADD COLUMN vat_rate character varying(10);

UPDATE invoice_entry
SET vat_rate = vat_rate_tmp;

ALTER TABLE invoice_entry
    DROP vat_rate_tmp;

ALTER TABLE invoice_entry
    ALTER COLUMN vat_rate SET NOT NULL;