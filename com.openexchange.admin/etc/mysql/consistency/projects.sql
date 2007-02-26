#@(#) projects.sql


ALTER TABLE projects ENGINE = InnoDB;

ALTER TABLE backup_projects ENGINE = InnoDB;

ALTER TABLE projects_participants
    ADD FOREIGN KEY (intfield01, cid) REFERENCES projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE = InnoDB;

ALTER TABLE backup_projects_participants
    ADD FOREIGN KEY (intfield01, cid) REFERENCES backup_projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE = InnoDB;

ALTER TABLE projects_milestones
    ADD FOREIGN KEY (intfield01, cid) REFERENCES projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE backup_projects_milestones
    ADD FOREIGN KEY (intfield01, cid) REFERENCES backup_projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE projects_notes
    ADD FOREIGN KEY (intfield01, cid) REFERENCES projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE backup_projects_notes
    ADD FOREIGN KEY (intfield01, cid) REFERENCES backup_projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE projects_phases
    ADD FOREIGN KEY (intfield01, cid) REFERENCES projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE backup_projects_phases
    ADD FOREIGN KEY (intfield01, cid) REFERENCES backup_projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE projects_tasks
    ADD FOREIGN KEY (intfield01, cid) REFERENCES projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD FOREIGN KEY (intfield01, cid, phase_id) REFERENCES projects_phases (intfield01, cid, phase_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE backup_projects_tasks
    ADD FOREIGN KEY (intfield01, cid) REFERENCES backup_projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD FOREIGN KEY (intfield01, cid, phase_id) REFERENCES backup_projects_phases (intfield01, cid, phase_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE projects_dependencies
    ADD FOREIGN KEY (intfield01, cid) REFERENCES projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE backup_projects_dependencies
    ADD FOREIGN KEY (intfield01, cid) REFERENCES backup_projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE projects_antecessors
    ADD FOREIGN KEY (intfield01, cid) REFERENCES projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD FOREIGN KEY (intfield01, cid, id) REFERENCES projects_dependencies (intfield01, cid, id) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE backup_projects_antecessors
    ADD FOREIGN KEY (intfield01, cid) REFERENCES backup_projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD FOREIGN KEY (intfield01, cid, id) REFERENCES backup_projects_dependencies (intfield01, cid, id) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE projects_puids
    ADD FOREIGN KEY (intfield01, cid) REFERENCES projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;

ALTER TABLE backup_projects_puids
    ADD FOREIGN KEY (intfield01, cid) REFERENCES backup_projects (intfield01, cid) ON DELETE CASCADE ON UPDATE CASCADE,
    ENGINE=INNODB;
