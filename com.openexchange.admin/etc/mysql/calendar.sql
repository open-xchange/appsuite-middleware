
CREATE TABLE prg_dates (
	creating_date timestamp,
	created_from text,
	changing_date timestamp,
	changed_from text,
	group_right text,
	sid text,
	tid text,
	fid integer,
	pflag integer,
   cid integer,
	order_crit text,
	timestampfield01 timestamp,
	timestampfield02 timestamp,
	timezone text,
	intfield01 integer,
	intfield02 integer,
	intfield03 integer,
	intfield04 integer,
	intfield05 integer,
	intfield06 integer,
	intfield07 integer,
	intfield08 integer,
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
	field15 text
);

CREATE TABLE prg_date_rights (
	object_id integer,
	cid text,
	id text,
   type text,
	ma text,
	dn text
);


CREATE TABLE del_date_rights (
	object_id integer,
	cid text,
	id text,
   type text,
	ma text,
	dn text	
);

CREATE TABLE del_dates (
	creating_date timestamp,
	created_from text,
	changing_date timestamp,
	changed_from text,
	group_right text,
	sid text,
	tid text,
	fid integer,
	pflag integer,
   cid integer,
	order_crit text,
	timestampfield01 timestamp,
	timestampfield02 timestamp,
	timezone text,
	intfield01 integer,
	intfield02 integer,
	intfield03 integer,
	intfield04 integer,
	intfield05 integer,
	intfield06 integer,
	intfield07 integer,
	intfield08 integer,
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
	field15 text
);

CREATE TABLE del_dates_members (
	object_id int,
	member_uid integer,	
	confirm text,
	reason text,
	pfid integer,
        reminder integer,
        cid integer
);

CREATE TABLE prg_dates_members (
	object_id integer,
	member_uid integer,
	confirm VARCHAR(10),
	reason VARCHAR(100),
	pfid integer,
        reminder integer,
        cid integer
);

