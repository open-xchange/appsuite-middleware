#@(#) Tables for tasks

CREATE TABLE task (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    uid VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    private BOOLEAN NOT NULL,
    creating_date DATETIME NOT NULL,
    last_modified INT8 NOT NULL,
    created_from INT4 UNSIGNED NOT NULL,
    changed_from INT4 UNSIGNED NOT NULL,
    start DATETIME,
    end DATETIME,
    completed DATETIME,
    title VARCHAR(256),
    description TEXT,
    state INTEGER,
    priority INTEGER,
    progress INTEGER,
    categories VARCHAR(255),
    project INT4 UNSIGNED,
    target_duration BIGINT,
    actual_duration BIGINT,
    target_costs FLOAT,
    actual_costs FLOAT,
    currency VARCHAR(10),
    trip_meter VARCHAR(255),
    billing VARCHAR(255),
    companies VARCHAR(255),
    color_label INT1 UNSIGNED,
    recurrence_type INT1 UNSIGNED NOT NULL,
    recurrence_interval INT4 UNSIGNED,
    recurrence_days INT1 UNSIGNED,
    recurrence_dayinmonth INT1 UNSIGNED,
    recurrence_month INT1 UNSIGNED,
    recurrence_until DATETIME,
    recurrence_count INT2 UNSIGNED,
    number_of_attachments INT1 UNSIGNED NOT NULL,
    PRIMARY KEY (cid,id),
    INDEX (cid,last_modified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE task_folder (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    folder INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid,id,folder),
    INDEX (cid,folder),
    FOREIGN KEY (cid, id) REFERENCES task (cid, id),
    FOREIGN KEY (cid, folder) REFERENCES oxfolder_tree (cid, fuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE task_participant (
    cid INT4 UNSIGNED NOT NULL,
    task INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    group_id INT4 UNSIGNED,
    accepted INT1 UNSIGNED NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (cid,task,user),
    FOREIGN KEY (cid, task) REFERENCES task (cid, id),
    FOREIGN KEY (cid,user) REFERENCES user (cid,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE task_eparticipant (
    cid INT4 UNSIGNED NOT NULL,
    task INT4 UNSIGNED NOT NULL,
    mail VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    PRIMARY KEY (cid,task,mail),
    FOREIGN KEY (cid,task) REFERENCES task (cid,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE task_removedparticipant (
    cid INT4 UNSIGNED NOT NULL,
    task INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    group_id INT4 UNSIGNED,
    accepted INT1 UNSIGNED NOT NULL,
    description VARCHAR(255),
    folder INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid,task,user),
    INDEX (cid,folder),
    FOREIGN KEY (cid, task) REFERENCES task (cid, id),
    FOREIGN KEY (cid,user) REFERENCES user(cid,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_task (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    uid VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    private BOOLEAN NOT NULL,
    creating_date DATETIME NOT NULL,
    last_modified INT8 NOT NULL,
    created_from INT4 UNSIGNED NOT NULL,
    changed_from INT4 UNSIGNED NOT NULL,
    start DATETIME,
    end DATETIME,
    completed DATETIME,
    title VARCHAR(256),
    description TEXT,
    state INTEGER,
    priority INTEGER,
    progress INTEGER,
    categories VARCHAR(255),
    project INT4 UNSIGNED,
    target_duration BIGINT,
    actual_duration BIGINT,
    target_costs FLOAT,
    actual_costs FLOAT,
    currency VARCHAR(10),
    trip_meter VARCHAR(255),
    billing VARCHAR(255),
    companies VARCHAR(255),
    color_label INT1 UNSIGNED,
    recurrence_type INT1 UNSIGNED NOT NULL,
    recurrence_interval INT4 UNSIGNED,
    recurrence_days INT1 UNSIGNED,
    recurrence_dayinmonth INT1 UNSIGNED,
    recurrence_month INT1 UNSIGNED,
    recurrence_until DATETIME,
    recurrence_count INT2 UNSIGNED,
    number_of_attachments INT1 UNSIGNED NOT NULL,
    PRIMARY KEY (cid,id),
    INDEX (cid,last_modified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_task_folder (
    cid INT4 UNSIGNED NOT NULL,
    id INT4 UNSIGNED NOT NULL,
    folder INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    PRIMARY KEY (cid,id,folder),
    INDEX (cid,folder),
    FOREIGN KEY (cid, id) REFERENCES del_task (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_task_participant (
    cid INT4 UNSIGNED NOT NULL,
    task INT4 UNSIGNED NOT NULL,
    user INT4 UNSIGNED NOT NULL,
    group_id INT4 UNSIGNED,
    accepted INT1 UNSIGNED NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (cid,task,user),
    FOREIGN KEY (cid, task) REFERENCES del_task (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE del_task_eparticipant (
    cid INT4 UNSIGNED NOT NULL,
    task INT4 UNSIGNED NOT NULL,
    mail VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    PRIMARY KEY (cid,task,mail),
    FOREIGN KEY (cid, task) REFERENCES del_task (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
