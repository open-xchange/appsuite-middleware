
CREATE TABLE ical_principal (
		object_id int,
		cid int,
		principal text,
		calendarfolder int,
		taskfolder int
);

CREATE TABLE ical_ids (
		object_id int,
		cid int,
		principal_id int,
		client_id text,
		target_object_id int,
		module int
);

CREATE TABLE vcard_principal (
		object_id int,
		cid int,
		principal text,
		contactfolder int
);

CREATE TABLE vcard_ids (
		object_id int,
		cid int,
		principal_id int,
		client_id text,
		target_object_id int
);
