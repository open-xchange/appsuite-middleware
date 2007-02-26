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
