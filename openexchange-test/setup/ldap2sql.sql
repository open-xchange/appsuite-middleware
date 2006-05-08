INSERT INTO groups (cid, id, identifier, displayName) VALUES
(1, select_serial_id(), 'users', 'Users'),
(1, select_serial_id(), 'kleinegruppe', 'kleinegruppe');

CREATE TABLE remember_id (
    id bigint NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO remember_id (id) VALUES (select_serial_id());
INSERT INTO user (cid, id, userPassword, mailEnabled, imapServer, smtpServer, mailDomain, shadowLastChange, mail, givenName, sureName, displayName, modifyTimestamp, timeZone, appointmentDays, taskDays, preferredLanguage, country) VALUES
(1, (SELECT id FROM remember_id), NULL, NULL, NULL, NULL, NULL, 0, 'bishoph@example.org', 'Martin', 'Kauss', 'Martin Kauss', '2006-05-02 10:45', 'Europe/Berlin', 5, 5, 'de', 'Tuxworld');
INSERT INTO login2user (cid, id, uid) VALUES
(1, (SELECT id FROM remember_id), 'bishoph');
INSERT INTO user_attribute (cid, id, name, value) VALUES
(1, (SELECT id FROM remember_id), 'alias', 'martin.kauss@example.org'),
(1, (SELECT id FROM remember_id), 'alias', 'kaussbauss@example.org'),
(1, (SELECT id FROM remember_id), 'alias', 'm.kauss@example.org'),
(1, (SELECT id FROM remember_id), 'alias', 'b00n@example.org');
INSERT INTO groups_member (cid, id, member) VALUES
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'users'), (SELECT id FROM remember_id));

UPDATE remember_id SET id = select_serial_id();
INSERT INTO user (cid, id, userPassword, mailEnabled, imapServer, smtpServer, mailDomain, shadowLastChange, mail, givenName, sureName, displayName, modifyTimestamp, timeZone, appointmentDays, taskDays, preferredLanguage, country) VALUES
(1, (SELECT id FROM remember_id), NULL, NULL, 'redhat.netline.de', 'redhat.netline.de', 'example.org', 0, 'mailadmin@example.org', 'Admin', 'Admin', 'Admin Admin', '2006-05-02 10:45', 'Europe/Berlin', 5, 5, 'de', 'Tuxworld');
INSERT INTO login2user (cid, id, uid) VALUES
(1, (SELECT id FROM remember_id), 'mailadmin');
INSERT INTO user_attribute (cid, id, name, value) VALUES
(1, (SELECT id FROM remember_id), 'alias', 'postmaster@example.org'),
(1, (SELECT id FROM remember_id), 'alias', 'root@example.org');
INSERT INTO groups_member (cid, id, member) VALUES
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'users'), (SELECT id FROM remember_id));

UPDATE remember_id SET id = select_serial_id();
INSERT INTO user (cid, id, userPassword, mailEnabled, imapServer, smtpServer, mailDomain, shadowLastChange, mail, givenName, sureName, displayName, modifyTimestamp, timeZone, appointmentDays, taskDays, preferredLanguage, country) VALUES
(1, (SELECT id FROM remember_id), 'yzOTz1k6gXQSvQFxExlaVUqtskU=', 'OK', NULL, NULL, NULL, 13258, 'marcus@example.org', 'Marcus', 'Klein', 'Marcus Klein', '2006-05-03 10:45', 'Europe/Berlin', 5, 5, 'de', 'Tuxworld');
INSERT INTO login2user (cid, id, uid) VALUES
(1, (SELECT id FROM remember_id), 'marcus');
INSERT INTO user_attribute (cid, id, name, value) VALUES
(1, (SELECT id FROM remember_id), 'alias', 'marcus@example.org');
INSERT INTO groups_member (cid, id, member) VALUES
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'users'), (SELECT id FROM remember_id));

UPDATE remember_id SET id = select_serial_id();
INSERT INTO user (cid, id, userPassword, mailEnabled, imapServer, smtpServer, mailDomain, shadowLastChange, mail, givenName, sureName, displayName, modifyTimestamp, timeZone, appointmentDays, taskDays, preferredLanguage, country) VALUES
(1, (SELECT id FROM remember_id), null, 'OK', null, null, null, 0, 'offspring@example.org', 'Sebastian', 'Kauss', 'Sebastian Kauss', '2006-05-04 00:00:00', 'Europe/Berlin', 5, 5, 'de', 'Tuxworld');
INSERT INTO login2user (cid, id, uid) VALUES
(1, (SELECT id FROM remember_id), 'offspring');
INSERT INTO groups_member (cid, id, member) VALUES
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'kleinegruppe'), (SELECT id FROM remember_id));

UPDATE remember_id SET id = select_serial_id();
INSERT INTO user (cid, id, userPassword, mailEnabled, imapServer, smtpServer, mailDomain, shadowLastChange, mail, givenName, sureName, displayName, modifyTimestamp, timeZone, appointmentDays, taskDays, preferredLanguage, country) VALUES
(1, (SELECT id FROM remember_id), null, 'OK', null, null, null, 0, 'viktor@example.org', 'Viktor', 'Pracht', 'Viktor Pracht', '2006-05-04 11:03:44', 'Europe/Berlin', 5, 5, 'de', 'Tuxworld');
INSERT INTO login2user (cid, id, uid) VALUES
(1, (SELECT id FROM remember_id), 'viktor');
INSERT INTO groups_member (cid, id, member) VALUES
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'kleinegruppe'), (SELECT id FROM remember_id));

DROP TABLE remember_id;

INSERT INTO resource_group (cid, id, identifier, displayName, available) VALUES
(1, select_serial_id(), 'Autos', 'Autos', true),
(1, select_serial_id(), 'Notebooks', 'Notebooks', true);

INSERT INTO resource (cid, id, identifier, displayName, available, description) VALUES
(1, select_serial_id(), 'Twingo', 'Twingo', true, null),
(1, select_serial_id(), 'Ford Focus', 'Ford Focus', true, null),
(1, select_serial_id(), 'mama', 'mama', true, null),
(1, select_serial_id(), 'testmoped', 'testmoped', true, null),
(1, select_serial_id(), 'resource2', 'resource2', true, null);

INSERT INTO resource_group_member (cid, id, member) VALUES
(1, (SELECT id FROM resource_group WHERE cid = 1 AND identifier = 'Autos'), (SELECT id FROM resource WHERE cid = 1 AND identifier = 'Twingo'));
