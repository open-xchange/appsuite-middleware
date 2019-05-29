---
title: DB-User Privileges
icon: fa-lock
tags: Administration, Database, Security
---

This article describes how to reduce the database user privileges in existing Open-Xchange installations to the least required ones. Changing the existing ``ALL PRIVILEGES`` to the provided minimum set will have no implications for running the server. OX versions prior v7.8.0 may have used stored procedures; therefore they need to be removed beforehand, too.

# Remove obsolete Stored Procedures

## Why to manually remove the existing Stored Procedures?

The Stored Procedures mentioned within the next paragraph aren't needed any more by the Open-Xchange groupware. Because of the reduced privileges Open Xchange recommends, the  'openexchange' database user isn't able to detect the existing stored procedures.

## Which Stored Procedures are affected?

The following Stored Procedures have to be removed from your configured config database:

```
get_context_id, get_configdb_id
```

The following Stored Procedures have to be removed from the context schematas:

```
get_attachment_id, get_calendar_id, get_contact_id, get_folder_id, get_forum_id, get_gid_number_id, get_gui_setting_id, get_ical_id, get_infostore_id, get_mail_service_id, get_pinboard_id, get_principal_id, get_project_id, get_resource_group_id, get_resource_id, get_task_id, get_uid_number_id, get_unique_id, get_webdav_id
```

## How to remove affected Stored Procedures?

Connect to mysql as a privileged user and run the following commands:

* List all procedures owned/created by *openexchange* (assuming you did not change the default name of that account):

   ```
   mysql> SELECT name,definer FROM mysql.proc WHERE definer LIKE "openexchange@%";
   +-----------------------+------------------------+
   | name                  | definer                |
   +-----------------------+------------------------+
   | get_context_id        | openexchange@localhost |
   | get_configdb_id       | openexchange@localhost |
   | get_gid_number_id     | openexchange@localhost |
   | get_mail_service_id   | openexchange@localhost |
   | get_infostore_id      | openexchange@localhost |
   | get_forum_id          | openexchange@localhost |
   | get_pinboard_id       | openexchange@localhost |
   | get_gui_setting_id    | openexchange@localhost |
   | get_ical_id           | openexchange@localhost |
   | get_attachment_id     | openexchange@localhost |
   | get_webdav_id         | openexchange@localhost |
   | get_uid_number_id     | openexchange@localhost |
   | get_unique_id         | openexchange@localhost |
   | get_resource_id       | openexchange@localhost |
   | get_resource_group_id | openexchange@localhost |
   | get_principal_id      | openexchange@localhost |
   | get_folder_id         | openexchange@localhost |
   | get_calendar_id       | openexchange@localhost |
   | get_contact_id        | openexchange@localhost |
   | get_task_id           | openexchange@localhost |
   | get_project_id        | openexchange@localhost |
   +-----------------------+------------------------+
   21 rows in set (0.00 sec)
   ```

* If this list seems okay, delete these procedures using the command below:

   ```
   mysql> DELETE FROM mysql.proc WHERE definer LIKE "openexchange@%";
   Query OK, 21 rows affected (0.00 sec)
   ```


# Restrict Privileges 

The following privileges are required for the Open-Xchange user: ``CREATE``, ``LOCK TABLES``, ``REFERENCES``, ``INDEX``, ``DROP``, ``DELETE``, ``ALTER``, ``SELECT``, ``UPDATE``, ``INSERT``, ``CREATE TEMPORARY TABLES``, ``SHOW VIEW`` and ``SHOW DATABASES``. The following steps can be used to change the existing database privileges:

1. Login to the master MySQL database using the root user.

2. Detect the existing Open-Xchange users: 

   ``SELECT user,host FROM mysql.user;``

   The output will look like outlined in the following table:
   
   ```
   +------------------+-----------+
   | user             | host      |
   +------------------+-----------+
   | openexchange     | %         |
   | root             | 127.0.0.1 |
   ```
   
   In this case, the user for all additional processings is ``'openexchange'@'%'`` and will be used for the description below.

3. Detect all existing privileges for the Open-Xchange user above: 

   ``SHOW GRANTS FOR 'openexchange'@'%';``

   The output will look like outlined in the following table. If the output is extremly different the user might already has got limited privileges.
   
   ```
   +---------------------------------------------------------------------------------------------------+
   | Grants for openexchange@%                                                                         |
   +---------------------------------------------------------------------------------------------------+
   | GRANT ALL PRIVILEGES ON *.* TO 'openexchange'@'%' IDENTIFIED BY PASSWORD '*d884b784ee1ad34d0c394' |
   +---------------------------------------------------------------------------------------------------+
   1 row in set (0,00 sec)
   ```

4. Revoke all existing privileges for the Open-Xchange user above. Be careful to use the ``database@host`` pattern provided by the output from step 3 (in this case ``*.*``):

   ``REVOKE ALL PRIVILEGES ON *.* FROM 'openexchange'@'%';``

   Hint: This must be executed for each ``database@hostname`` combination displayed in step 3 (normally just ``*.*``). Without revoking privileges you will have duplicates.

5. Create new privileges: 
   
   ``GRANT CREATE, LOCK TABLES, REFERENCES, INDEX, DROP, DELETE, ALTER, SELECT, UPDATE, INSERT, CREATE TEMPORARY TABLES, SHOW VIEW, SHOW DATABASES ON *.* TO 'openexchange'@'%' IDENTIFIED BY '<YOUR_DB_PASS>' WITH GRANT OPTION;`` 

6. Write the privileges: 
   
   ``FLUSH PRIVILEGES;``

7. The changes of the privileges are noticed by the server so that the grant tables are loaded into memory again immediately after the change. You do not have to restart mysql. The grants outlined by ``SHOW GRANTS FOR 'openexchange'@'%';`` should now look like (grants for configdb, schema, global db, guard db and guard shard available): 
   
   ```
   +-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
   | Grants for openexchange@%                                                                                                                                                                           |
   +-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
   | GRANT USAGE ON *.* TO 'openexchange'@'%' IDENTIFIED BY PASSWORD '*d884b784ee1ad34d0c394'                                                                                                            |
   | GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES, SHOW VIEW ON `configdb`.* TO 'openexchange'@'%' WITH GRANT OPTION               |
   | GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES, SHOW VIEW ON `oxdatabase_6`.* TO 'openexchange'@'%' WITH GRANT OPTION           |
   | GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES, SHOW VIEW ON `oxdatabase_global`.* TO 'openexchange'@'%' WITH GRANT OPTION      |
   | GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES, SHOW VIEW ON `oxguard1`.* TO 'openexchange'@'%' WITH GRANT OPTION               |
   | GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES, SHOW VIEW ON `oxguard`.* TO 'openexchange'@'%' WITH GRANT OPTION                |
   +-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
   ```
