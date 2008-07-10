
CREATE TABLE prg_dates (
	creating_date timestamp DEFAULT CURRENT_TIMESTAMP,
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
	creating_date timestamp DEFAULT CURRENT_TIMESTAMP,
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

alter table prg_dates DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

alter table prg_dates change column cid cid integer unsigned not null;
alter table prg_dates change column created_from created_from integer unsigned not null;
alter table prg_dates change column changed_from changed_from integer unsigned not null;
alter table prg_dates change column changing_date changing_date INT8 not null;
alter table prg_dates change column fid fid integer unsigned not null;
alter table prg_dates change column pflag pflag integer unsigned not null;
alter table prg_dates change column cid cid integer unsigned not null;
alter table prg_dates change column timestampfield01 timestampfield01 datetime not null;
alter table prg_dates change column timestampfield02 timestampfield02 datetime not null;
alter table prg_dates change column timezone timezone VARCHAR(64) not null;
alter table prg_dates change column intfield01 intfield01 integer unsigned not null;
alter table prg_dates change column intfield02 intfield02 integer unsigned;
alter table prg_dates change column intfield03 intfield03 integer unsigned;
alter table prg_dates change column intfield04 intfield04 integer unsigned;
alter table prg_dates change column intfield05 intfield05 integer unsigned;
alter table prg_dates change column intfield06 intfield06 integer unsigned not null;
alter table prg_dates change column intfield07 intfield07 integer unsigned;
alter table prg_dates change column intfield08 intfield08 integer unsigned;
alter table prg_dates change column field01 field01 VARCHAR(255);
alter table prg_dates change column field02 field02 VARCHAR(255);
alter table prg_dates change column field04 field04 TEXT;
alter table prg_dates change column field06 field06 VARCHAR(64);
alter table prg_dates change column field08 field08 VARCHAR(255);
alter table prg_dates change column field09 field09 VARCHAR(255);
alter table prg_dates DROP COLUMN field03;
alter table prg_dates DROP COLUMN field05;
alter table prg_dates DROP COLUMN field10;
alter table prg_dates DROP COLUMN field11;
alter table prg_dates DROP COLUMN field12;
alter table prg_dates DROP COLUMN field13;
alter table prg_dates DROP COLUMN field14;
alter table prg_dates DROP COLUMN field15;

alter table prg_dates DROP COLUMN group_right;
alter table prg_dates DROP COLUMN sid;
alter table prg_dates DROP COLUMN tid;
alter table prg_dates DROP COLUMN order_crit;

alter table del_dates DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

alter table del_dates change column cid cid integer unsigned not null;
alter table del_dates change column created_from created_from integer unsigned not null;
alter table del_dates change column changed_from changed_from integer unsigned not null;
alter table del_dates change column changing_date changing_date INT8 not null;
alter table del_dates change column fid fid integer unsigned not null;
alter table del_dates change column pflag pflag integer unsigned not null;
alter table del_dates change column cid cid integer unsigned not null;
alter table del_dates change column timestampfield01 timestampfield01 datetime;
alter table del_dates change column timestampfield02 timestampfield02 datetime;
alter table del_dates change column timezone timezone VARCHAR(64);
alter table del_dates change column intfield01 intfield01 integer unsigned not null;
alter table del_dates change column intfield02 intfield02 integer unsigned;
alter table del_dates change column intfield03 intfield03 integer unsigned;
alter table del_dates change column intfield04 intfield04 integer unsigned;
alter table del_dates change column intfield05 intfield05 integer unsigned;
alter table del_dates change column intfield06 intfield06 integer unsigned;
alter table del_dates change column intfield07 intfield07 integer unsigned;
alter table del_dates change column intfield08 intfield08 integer unsigned;
alter table del_dates change column field01 field01 VARCHAR(255);
alter table del_dates change column field02 field02 VARCHAR(255);
alter table del_dates change column field04 field04 TEXT;
alter table del_dates change column field06 field06 VARCHAR(255);
alter table del_dates change column field08 field08 VARCHAR(255);
alter table del_dates change column field09 field09 VARCHAR(255);
alter table del_dates DROP COLUMN field03;
alter table del_dates DROP COLUMN field05;
alter table del_dates DROP COLUMN field10;
alter table del_dates DROP COLUMN field11;
alter table del_dates DROP COLUMN field12;
alter table del_dates DROP COLUMN field13;
alter table del_dates DROP COLUMN field14;
alter table del_dates DROP COLUMN field15;

alter table del_dates DROP COLUMN group_right;
alter table del_dates DROP COLUMN sid;
alter table del_dates DROP COLUMN tid;
alter table del_dates DROP COLUMN order_crit;

alter table prg_dates_members DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

alter table prg_dates_members change column cid cid integer unsigned not null;
alter table prg_dates_members change column confirm confirm integer unsigned not null;
alter table prg_dates_members change column reminder reminder integer unsigned;
alter table prg_dates_members change column reason reason VARCHAR(255);

alter table del_dates_members DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

alter table del_dates_members change column confirm confirm integer unsigned not null;
alter table del_dates_members change column cid cid integer unsigned not null;
alter table del_dates_members change column reminder reminder integer unsigned;
alter table del_dates_members change column reason reason VARCHAR(255);

alter table prg_date_rights change column object_id object_id integer unsigned not null;
alter table prg_date_rights change column cid cid integer unsigned not null;
alter table prg_date_rights change column id id integer not null;
alter table prg_date_rights change column type type integer unsigned not null;
alter table prg_date_rights change column ma ma VARCHAR(286);
alter table prg_date_rights change column dn dn VARCHAR(64);

alter table del_date_rights change column object_id object_id integer unsigned not null;
alter table del_date_rights change column cid cid integer unsigned not null;
alter table del_date_rights change column id id integer not null;
alter table del_date_rights change column type type integer unsigned not null;
alter table del_date_rights change column ma ma VARCHAR(286);
alter table del_date_rights change column dn dn VARCHAR(64);


ALTER TABLE prg_dates_members ADD PRIMARY KEY (cid, object_id, member_uid);
ALTER TABLE del_dates_members ADD PRIMARY KEY (cid, object_id, member_uid);

ALTER TABLE prg_dates ADD PRIMARY KEY (cid, intfield01);
ALTER TABLE del_dates ADD PRIMARY KEY (cid, intfield01);
ALTER TABLE prg_date_rights ADD PRIMARY KEY (cid, object_id, id, type);
ALTER TABLE del_date_rights ADD PRIMARY KEY (cid, object_id, id, type);

alter table prg_dates add index (timestampfield01);
alter table prg_dates add index (timestampfield02);alter table prg_dates ENGINE=InnoDB; 
alter table del_dates ENGINE=InnoDB; 
alter table prg_date_rights ENGINE=InnoDB; 
alter table del_date_rights ENGINE=InnoDB; 

alter table prg_date_rights ENGINE=InnoDB; 
alter table del_date_rights ENGINE=InnoDB; 

alter table prg_dates_members ENGINE=InnoDB; 
alter table del_dates_members ENGINE=InnoDB; 

