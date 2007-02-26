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
alter table prg_dates change column field07 field07 VARCHAR(255);
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
alter table del_dates change column field07 field07 VARCHAR(255);
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
alter table prg_date_rights change column ma ma VARCHAR(64);
alter table prg_date_rights change column dn dn VARCHAR(64);

alter table del_date_rights change column object_id object_id integer unsigned not null;
alter table del_date_rights change column cid cid integer unsigned not null;
alter table del_date_rights change column id id integer not null;
alter table del_date_rights change column type type integer unsigned not null;
alter table del_date_rights change column ma ma VARCHAR(64);
alter table del_date_rights change column dn dn VARCHAR(64);


ALTER TABLE prg_dates_members ADD PRIMARY KEY (cid, object_id, member_uid);
ALTER TABLE del_dates_members ADD PRIMARY KEY (cid, object_id, member_uid);

ALTER TABLE prg_dates ADD PRIMARY KEY (cid, intfield01);
ALTER TABLE del_dates ADD PRIMARY KEY (cid, intfield01);
ALTER TABLE prg_date_rights ADD PRIMARY KEY (cid, object_id, id, type);
ALTER TABLE del_date_rights ADD PRIMARY KEY (cid, object_id, id, type);

alter table prg_dates add index (timestampfield01);
alter table prg_dates add index (timestampfield02);