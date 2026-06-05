ALTER TABLE plugin_package
    ADD COLUMN manifest_json LONGTEXT NULL;

ALTER TABLE plugin_skill
    ADD COLUMN market_metadata_json LONGTEXT NULL;
