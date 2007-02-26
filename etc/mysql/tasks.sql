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
