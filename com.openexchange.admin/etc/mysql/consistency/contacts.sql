#@(#) contacts.sql consistency

ALTER TABLE prg_contacts ENGINE = InnoDB;
ALTER TABLE del_contacts ENGINE = InnoDB;

ALTER TABLE prg_dlist ENGINE = InnoDB;
ALTER TABLE del_dlist ENGINE = InnoDB;

ALTER TABLE prg_contacts_linkage ENGINE = InnoDB;

ALTER TABLE prg_contacts_image ENGINE = InnoDB;
ALTER TABLE del_contacts_image ENGINE = InnoDB;
