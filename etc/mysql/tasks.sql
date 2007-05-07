#@(#) tasks.sql 

CREATE TABLE task (
    cid INTEGER,
    id INTEGER,
    private BOOLEAN,
    creating_date TIMESTAMP,
    last_modified BIGINT,
    created_from INTEGER,
    changed_from INTEGER,
    start TIMESTAMP,
    end TIMESTAMP,
    completed TIMESTAMP,
    title TEXT,
    description TEXT,
    state INTEGER,
    priority INTEGER,
    progress INTEGER,
    categories TEXT,
    project INTEGER,
    target_duration BIGINT,
    actual_duration BIGINT,
    target_costs FLOAT,
    actual_costs FLOAT,
    currency TEXT,
    trip_meter TEXT,
    billing TEXT,
    companies TEXT,
    color_label INTEGER,
    recurrence_type INTEGER,
    recurrence_interval INTEGER,
    recurrence_days INTEGER,
    recurrence_dayinmonth INTEGER,
    recurrence_month INTEGER,
    recurrence_until TIMESTAMP,
    recurrence_count INTEGER,
    number_of_attachments INTEGER
);

CREATE TABLE task_folder (
    cid INTEGER,
    id INTEGER,
    folder INTEGER,
    user INTEGER
);

CREATE TABLE task_participant (
    cid INTEGER,
    task INTEGER,
    user INTEGER,
    group_id INTEGER,
    accepted INTEGER,
    description TEXT
);

CREATE TABLE task_eparticipant (
    cid INTEGER,
    task INTEGER,
    mail TEXT,
    display_name TEXT
);

CREATE TABLE task_removedparticipant (
    cid INTEGER,
    task INTEGER,
    user INTEGER,
    group_id INTEGER,
    accepted INTEGER,
    description TEXT,
    folder INTEGER
);

CREATE TABLE del_task (
    cid INTEGER,
    id INTEGER,
    private BOOLEAN,
    creating_date TIMESTAMP,
    last_modified BIGINT,
    created_from INTEGER,
    changed_from INTEGER,
    start TIMESTAMP,
    end TIMESTAMP,
    completed TIMESTAMP,
    title TEXT,
    description TEXT,
    state INTEGER,
    priority INTEGER,
    progress INTEGER,
    categories TEXT,
    project INTEGER,
    target_duration BIGINT,
    actual_duration BIGINT,
    target_costs FLOAT,
    actual_costs FLOAT,
    currency TEXT,
    trip_meter TEXT,
    billing TEXT,
    companies TEXT,
    color_label INTEGER,
    recurrence_type INTEGER,
    recurrence_interval INTEGER,
    recurrence_days INTEGER,
    recurrence_dayinmonth INTEGER,
    recurrence_month INTEGER,
    recurrence_until TIMESTAMP,
    recurrence_count INTEGER,
    number_of_attachments INTEGER
);

CREATE TABLE del_task_folder (
    cid INTEGER,
    id INTEGER,
    folder INTEGER,
    user INTEGER
);


CREATE TABLE del_task_participant (
    cid INTEGER,
    task INTEGER,
    user INTEGER,
    group_id INTEGER,
    accepted INTEGER,
    description TEXT
);

CREATE TABLE del_task_eparticipant (
    cid INTEGER,
    task INTEGER,
    mail TEXT,
    display_name TEXT
);
#@(#) tasks.sql optimizations

ALTER TABLE task
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY creating_date DATETIME,
    MODIFY last_modified INT8,
    MODIFY created_from INT4 UNSIGNED,
    MODIFY changed_from INT4 UNSIGNED,
    MODIFY start DATETIME,
    MODIFY end DATETIME,
    MODIFY completed DATETIME,
    MODIFY title VARCHAR(128),
    MODIFY description TEXT CHARACTER SET utf8 COLLATE utf8_unicode_ci,
    MODIFY categories VARCHAR(255),
    MODIFY project INT4 UNSIGNED,
    MODIFY currency VARCHAR(10),
    MODIFY trip_meter VARCHAR(255),
    MODIFY billing VARCHAR(255),
    MODIFY companies VARCHAR(255),
    MODIFY color_label INT1 UNSIGNED,
    MODIFY recurrence_type INT1 UNSIGNED,
    MODIFY recurrence_interval INT4 UNSIGNED,
    MODIFY recurrence_days INT1 UNSIGNED,
    MODIFY recurrence_dayinmonth INT1 UNSIGNED,
    MODIFY recurrence_month INT1 UNSIGNED,
    MODIFY recurrence_until DATETIME,
    MODIFY recurrence_count INT2 UNSIGNED,
    MODIFY number_of_attachments INT1 UNSIGNED,
    ADD PRIMARY KEY (cid,id),
    ADD INDEX (cid,last_modified),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE task_folder
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY folder INT4 UNSIGNED,
    MODIFY user INT4 UNSIGNED,
    ADD PRIMARY KEY (cid,id,folder),
    ADD INDEX (cid,folder),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE task_participant
    MODIFY cid INT4 UNSIGNED,
    MODIFY task INT4 UNSIGNED,
    MODIFY user INT4 UNSIGNED,
    MODIFY group_id INT4 UNSIGNED,
    MODIFY accepted INT1 UNSIGNED,
    MODIFY description VARCHAR(255),
    ADD PRIMARY KEY (cid,task,user),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE task_eparticipant
    MODIFY cid INT4 UNSIGNED,
    MODIFY task INT4 UNSIGNED,
    MODIFY mail VARCHAR(255),
    MODIFY display_name VARCHAR(255),
    ADD PRIMARY KEY (cid,task,mail),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE task_removedparticipant
    MODIFY cid INT4 UNSIGNED,
    MODIFY task INT4 UNSIGNED,
    MODIFY user INT4 UNSIGNED,
    MODIFY group_id INT4 UNSIGNED,
    MODIFY accepted INT1 UNSIGNED,
    MODIFY folder INT4 UNSIGNED,
    ADD PRIMARY KEY (cid,task,user),
    ADD INDEX (cid,folder);
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE del_task
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY creating_date DATETIME,
    MODIFY last_modified INT8,
    MODIFY created_from INT4 UNSIGNED,
    MODIFY changed_from INT4 UNSIGNED,
    MODIFY start DATETIME,
    MODIFY end DATETIME,
    MODIFY completed DATETIME,
    MODIFY title VARCHAR(128),
    MODIFY description TEXT CHARACTER SET utf8 COLLATE utf8_unicode_ci,
    MODIFY categories VARCHAR(255),
    MODIFY project INT4 UNSIGNED,
    MODIFY currency VARCHAR(10),
    MODIFY trip_meter VARCHAR(255),
    MODIFY billing VARCHAR(255),
    MODIFY companies VARCHAR(255),
    MODIFY color_label INT1 UNSIGNED,
    MODIFY recurrence_type INT1 UNSIGNED,
    MODIFY recurrence_interval INT4 UNSIGNED,
    MODIFY recurrence_days INT1 UNSIGNED,
    MODIFY recurrence_dayinmonth INT1 UNSIGNED,
    MODIFY recurrence_month INT1 UNSIGNED,
    MODIFY recurrence_until DATETIME,
    MODIFY recurrence_count INT2 UNSIGNED,
    MODIFY number_of_attachments INT1 UNSIGNED,
    ADD PRIMARY KEY (cid,id),
    ADD INDEX (cid,last_modified),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE del_task_folder
    MODIFY cid INT4 UNSIGNED,
    MODIFY id INT4 UNSIGNED,
    MODIFY folder INT4 UNSIGNED,
    MODIFY user INT4 UNSIGNED,
    ADD PRIMARY KEY (cid,id,folder),
    ADD INDEX (cid,folder),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

ALTER TABLE del_task_participant
    MODIFY cid INT4 UNSIGNED,
    MODIFY task INT4 UNSIGNED,
    MODIFY user INT4 UNSIGNED,
    MODIFY group_id INT4 UNSIGNED,
    MODIFY accepted INT1 UNSIGNED,
    MODIFY description VARCHAR(255),
    ADD PRIMARY KEY (cid,task,user),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
    
ALTER TABLE del_task_eparticipant
    MODIFY cid INT4 UNSIGNED,
    MODIFY task INT4 UNSIGNED,
    MODIFY mail VARCHAR(255),
    MODIFY display_name VARCHAR(255),
    ADD PRIMARY KEY (cid,task,mail),
    DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
#@(#) tasks.sql consistency

ALTER TABLE task
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY creating_date DATETIME NOT NULL,
    MODIFY last_modified INT8 NOT NULL,
    MODIFY created_from INT4 UNSIGNED NOT NULL,
    MODIFY private BOOLEAN NOT NULL,
    MODIFY recurrence_type INT1 UNSIGNED NOT NULL,
    MODIFY number_of_attachments INT1 UNSIGNED NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE task_folder
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY folder INT4 UNSIGNED NOT NULL,
    MODIFY user INT4 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (cid, id) REFERENCES task (cid, id),
    ADD FOREIGN KEY (cid, folder) REFERENCES oxfolder_tree (cid, fuid),
    ENGINE=InnoDB;

ALTER TABLE task_participant
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY task INT4 UNSIGNED NOT NULL,
    MODIFY user INT4 UNSIGNED NOT NULL,
    MODIFY accepted INT1 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (cid, task) REFERENCES task (cid, id),
    ADD FOREIGN KEY (cid,user) REFERENCES user(cid,id),
    ENGINE=InnoDB;

ALTER TABLE task_eparticipant
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY task INT4 UNSIGNED NOT NULL,
    MODIFY mail VARCHAR(255) NOT NULL,
    ADD FOREIGN KEY (cid,task) REFERENCES task (cid,id),
    ENGINE=InnoDB;

ALTER TABLE task_removedparticipant
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY task INT4 UNSIGNED NOT NULL,
    MODIFY user INT4 UNSIGNED NOT NULL,
    MODIFY accepted INT1 UNSIGNED NOT NULL,
    MODIFY folder INT4 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (cid, task) REFERENCES task (cid, id),
    ADD FOREIGN KEY (cid,user) REFERENCES user(cid,id),
    ENGINE=InnoDB;

ALTER TABLE del_task
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY creating_date DATETIME NOT NULL,
    MODIFY last_modified INT8 NOT NULL,
    MODIFY created_from INT4 UNSIGNED NOT NULL,
    MODIFY private BOOLEAN NOT NULL,
    MODIFY recurrence_type INT1 UNSIGNED NOT NULL,
    MODIFY number_of_attachments INT1 UNSIGNED NOT NULL,
    ENGINE=InnoDB;

ALTER TABLE del_task_folder
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY id INT4 UNSIGNED NOT NULL,
    MODIFY folder INT4 UNSIGNED NOT NULL,
    MODIFY user INT4 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (cid, id) REFERENCES del_task (cid, id),
    ENGINE=InnoDB;

ALTER TABLE del_task_participant
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY task INT4 UNSIGNED NOT NULL,
    MODIFY user INT4 UNSIGNED NOT NULL,
    MODIFY accepted INT1 UNSIGNED NOT NULL,
    ADD FOREIGN KEY (cid, task) REFERENCES del_task (cid, id),
    ENGINE=InnoDB;

ALTER TABLE del_task_eparticipant
    MODIFY cid INT4 UNSIGNED NOT NULL,
    MODIFY task INT4 UNSIGNED NOT NULL,
    MODIFY mail VARCHAR(255) NOT NULL,
    ADD FOREIGN KEY (cid,task) REFERENCES del_task (cid,id),
    ENGINE=InnoDB;
