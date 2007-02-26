
CREATE TABLE projects (
        intfield01 bigint,
        cid bigint,
        fuid bigint,
        creation_date timestamp,
        created_from text,
        changing_date timestamp,
        changed_from text,
        user_right text,
        sid text,
        tid text,
        project_startdate timestamp,
        project_enddate timestamp,
        project_deadline timestamp,
        project_reminder timestamp,
        project_budget text,
        project_effort text,
        project_effort_unit text,
        project_currency text,
        project_type text,
        project_kickoff timestamp,
        project_description text,
        project_goal text,
        project_customer text,
        project_name text,
        project_status text,
        project_phase text,
        project_number_of_attachments integer,
        project_task_folder_id bigint,
        project_manage_permissions integer,
        project_reminder_id bigint,
        dynamicfield01 text,
        dynamicfield02 text,
        dynamicfield03 text,
        dynamicfield04 text,
        dynamicfield05 text,
        dynamicfield06 text,
        dynamicfield07 text,
        dynamicfield08 text,
        dynamicfield09 text,
        dynamicfield10 text
);

CREATE TABLE backup_projects (
        intfield01 bigint,
        cid bigint,
        fuid bigint,
        creation_date timestamp,
        created_from text,
        changing_date timestamp,
        changed_from text,
        user_right text,
        sid text,
        tid text,
        project_startdate timestamp,
        project_enddate timestamp,
        project_deadline timestamp,
        project_reminder timestamp,
        project_budget text,
        project_effort text,
        project_effort_unit text,
        project_currency text,
        project_type text,
        project_kickoff timestamp,
        project_description text,
        project_goal text,
        project_customer text,
        project_name text,
        project_status text,
        project_phase text,
        project_number_of_attachments integer,
        project_task_folder_id bigint,
        project_manage_permissions integer,
        project_reminder_id bigint,
        dynamicfield01 text,
        dynamicfield02 text,
        dynamicfield03 text,
        dynamicfield04 text,
        dynamicfield05 text,
        dynamicfield06 text,
        dynamicfield07 text,
        dynamicfield08 text,
        dynamicfield09 text,
        dynamicfield10 text
);

CREATE TABLE projects_participants (
        intfield01 bigint,
        cid bigint,
        id varchar(64),
        name text,
        role integer,
        ptype integer,
        group_id varchar(64),
        merged_permission integer
);

CREATE TABLE backup_projects_participants (
        intfield01 bigint,
        cid bigint,
        id varchar(64),
        name text,
        role integer,
        ptype integer,
        group_id varchar(64),
        merged_permission integer
);

CREATE TABLE projects_milestones (
        intfield01 bigint,
        cid bigint,
        id bigint,
        name text,
        description text,
        mdate timestamp
);

CREATE TABLE backup_projects_milestones (
        intfield01 bigint,
        cid bigint,
        id bigint,
        name text,
        description text,
        mdate timestamp
);

CREATE TABLE projects_notes (
        intfield01 bigint,
        cid bigint,
        note_id varchar(64),
        member_id text
);

CREATE TABLE backup_projects_notes (
        intfield01 bigint,
        cid bigint,
        note_id varchar(64),
        member_id text
);

CREATE TABLE projects_phases (
        intfield01 bigint,
        cid bigint,
        phase_id bigint,
        phase_name text
);

CREATE TABLE backup_projects_phases (
        intfield01 bigint,
        cid bigint,
        phase_id bigint,
        phase_name text
);

CREATE TABLE projects_tasks (
        intfield01 bigint,
        cid bigint,
        task_id bigint,
        phase_id bigint
);

CREATE TABLE backup_projects_tasks (
        intfield01 bigint,
        cid bigint,
        task_id bigint,
        phase_id bigint
);

CREATE TABLE projects_dependencies (
        intfield01 bigint,
        cid bigint,
        id bigint,
        successor varchar(64),
        dependency_type integer,
        object_type integer
);

CREATE TABLE backup_projects_dependencies (
        intfield01 bigint,
        cid bigint,
        id bigint,
        successor varchar(64),
        dependency_type integer,
        object_type integer
);

CREATE TABLE projects_antecessors (
        intfield01 bigint,
        cid bigint,
        id bigint,
        antecessor varchar(64),
        object_type integer
);

CREATE TABLE backup_projects_antecessors (
        intfield01 bigint,
        cid bigint,
        id bigint,
        antecessor varchar(64),
        object_type integer
);

CREATE TABLE projects_puids (
        intfield01 bigint,
        cid bigint,
        puid bigint,
        entity text,
        fuid bigint
);

CREATE TABLE backup_projects_puids (
        intfield01 bigint,
        cid bigint,
        puid bigint,
        entity text,
        fuid bigint
);

CREATE TABLE prg_projects_milestones (
	object_id int,
	sid int,
	created_from text,
	creating_date timestamp ,
	changed_from text,
	changing_date timestamp ,
	user_right text,
	timestampfield01 timestamp ,
	timestampfield02 timestamp ,
	title text,
	description text
);

CREATE TABLE prg_projects (
	creating_date timestamp ,
	created_from text,
	changing_date timestamp ,
	changed_from text,
	user_right text,
	group_right text,
	sid text,
	tid text,
	order_crit text,
	timestampfield01 timestamp ,
	timestampfield02 timestamp ,
	timestampfield03 timestamp ,
	intfield01 integer,
	intfield02 double precision,
	intfield03 double precision,
	intfield04 real,
	intfield05 integer,
	intfield06 integer,
	intfield07 integer,
	field01 text,
	field02 text,
	field03 text,
	field04 text,
	field05 text,
	field06 text,
	field07 text,
	field08 text,
	field09 text,
	field10 text,
	field11 text,
	field12 text,
	field13 text,
	field14 text,
	field15 text,
	field16 text,
	field17 text,
	field18 text,
	field19 text,
	field20 text
);

CREATE TABLE prg_projects_members (
	object_id integer,
	member_uid text,
	member_name text,
	confirm text,
	reason text
);

CREATE TABLE prg_projects_notification (
	object_id integer,
	member_uid text,
	notification text
);

CREATE TABLE del_projects (
	creating_date timestamp ,
	created_from text,
	changing_date timestamp ,
	changed_from text,
	user_right text,
	group_right text,
	sid text,
	tid text,
	order_crit text,
	timestampfield01 timestamp ,
	timestampfield02 timestamp ,
	timestampfield03 timestamp ,
	intfield01 integer,
	intfield02 double precision,
	intfield03 double precision,
	intfield04 double precision,
	intfield05 double precision,
	intfield06 integer,
	intfield07 integer,
	field01 text,
	field02 text,
	field03 text,
	field04 text,
	field05 text,
	field06 text,
	field07 text,
	field08 text,
	field09 text,
	field10 text,
	field11 text,
	field12 text,
	field13 text,
	field14 text,
	field15 text,
	field16 text,
	field17 text,
	field18 text,
	field19 text,
	field20 text
);

CREATE TABLE del_projects_members (
	object_id integer,
	member_uid text,
	member_name text,
	confirm text,
	reason text
);

CREATE TABLE del_projects_milestones (
	object_id int,
	sid int,
	created_from text,
	creating_date timestamp ,
	changed_from text,
	changing_date timestamp ,
	user_right text,
	timestampfield01 timestamp ,
	timestampfield02 timestamp ,
	title text,
	description text
);
