INSERT INTO groups (cid, id, identifier, displayName) VALUES
(1, select_serial_id(), 'users', 'Users'),
(1, select_serial_id(), 'kleinegruppe', 'kleinegruppe');

INSERT INTO users (cid, id, uid, userPassword, mailEnabled, imapServer, smtpServer, mailDomain, shadowLastChange, mail, alias, givenName, sureName, displayName, modifyTimestamp, timeZone, appointmentDays, taskDays, preferredLanguage) VALUES
(1, select_serial_id(), 'bishoph', NULL, NULL, NULL, NULL, NULL, 0, 'bishoph@example.org', 'martin.kauss@example.org,kaussbauss@example.org,m.kauss@example.org,b00n@example.org', 'Martin', 'Kauss', 'Martin Kauss', '2006-05-02 10:45', 'Europe/Berlin', 5, 5, 'de'),
(1, select_serial_id(), 'mailadmin', NULL, NULL, 'redhat.netline.de', 'redhat.netline.de', 'example.org', 0, 'mailadmin@example.org', 'postmaster@example.org,root@example.org', 'Admin', 'Admin', 'Admin Admin', '2006-05-02 10:45', 'Europe/Berlin', 5, 5, 'de'),
(1, select_serial_id(), 'marcus', 'yzOTz1k6gXQSvQFxExlaVUqtskU=', 'OK', NULL, NULL, NULL, 13258, 'marcus@example.org', 'marcus@example.org', 'Marcus', 'Klein', 'Marcus Klein', '2006-05-03 10:45', 'Europe/Berlin', 5, 5, 'de'),
(1, select_serial_id(), 'offspring', null, 'OK', null, null, null, 0, 'offspring@example.org', null, 'Sebastian', 'Kauss', 'Sebastian Kauss', '2006-05-04 00:00:00', 'Europe/Berlin', 5, 5, 'de'),
(1, select_serial_id(), 'viktor', null, 'OK', null, null, null, 0, 'viktor@example.org', null, 'Viktor', 'Pracht', 'Viktor Pracht', '2006-05-04 11:03:44', 'Europe/Berlin', 5, 5, 'de');

INSERT INTO groups_members (cid, id, member) VALUES
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'users'), (SELECT id FROM users WHERE cid = 1 AND uid = 'marcus')),
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'users'), (SELECT id FROM users WHERE cid = 1 AND uid = 'bishoph')),
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'users'), (SELECT id FROM users WHERE cid = 1 AND uid = 'mailadmin')),
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'kleinegruppe'), (SELECT id FROM users WHERE cid = 1 AND uid = 'offspring')),
(1, (SELECT id FROM groups WHERE cid = 1 AND identifier LIKE 'kleinegruppe'), (SELECT id FROM users WHERE cid = 1 AND uid = 'viktor'));

INSERT INTO resource_groups (cid, id, identifier, displayName, available) VALUES
(1, select_serial_id(), 'Autos', 'Autos', true),
(1, select_serial_id(), 'Notebooks', 'Notebooks', true);

INSERT INTO resources (cid, id, identifier, displayName, available, description) VALUES
(1, select_serial_id(), 'Twingo', 'Twingo', true, null),
(1, select_serial_id(), 'Ford Focus', 'Ford Focus', true, null),
(1, select_serial_id(), 'mama', 'mama', true, null),
(1, select_serial_id(), 'testmoped', 'testmoped', true, null),
(1, select_serial_id(), 'resource2', 'resource2', true, null);

INSERT INTO resource_groups_members (cid, id, member) VALUES
(1, (SELECT id FROM resource_groups WHERE cid = 1 AND identifier = 'Autos'), (SELECT id FROM resources WHERE cid = 1 AND identifier = 'Twingo'));
