---
title: Update tasks
---

# Introduction

Updates for database are executed by so called update tasks. Each task applies one or more changes either to database schema (e.g. add another column to an existing table) or to data (e.g. insert a new default value for existing rows in a table).

# Triggers

Each new version of the Open-Xchange Middleware may ship with one or more such update tasks. The not yet applied update tasks are either triggered

 - Manually by invoking the ``runupdate`` or the ``runallupdate`` command-line interface
 - By the first login of any user from any context that is hosted on the affecetd database schema
 - By executing provisioning calls
   - On context deletion
   - On moving a context to another database
   - On context administrator login via RMI (accessing special ``com.openexchange.admin.rmi.OXLoginInterface`` RMI interface)
   - On every call to ``com.openexchange.admin.rmi.OXUserInterface``
   - On every call to ``com.openexchange.admin.rmi.OXGroupInterface``
   - On every call to ``com.openexchange.admin.rmi.OXResourceInterface``

Furthermore, an administrator may (re-)execute certain update tasks through using the ``forceupdatetask`` command-line interface.

**Note:** The update tasks that are actually scheduled and/or available for execution may vary based on the installed packages and enabled features on the system.  

# Excluding update tasks

In certain scenarios, specific update tasks may be forcibly excluded from being executed. In order to do so, the update task's name needs to be uncommented or entered in the configuration file ``excludedupdatetasks.properties``. Update tasks and their identifying names used to be announced in the release notes. Newly introduced update tasks will be added in the following overview instead.

# Core update tasks

The following list gives an overview about the known update tasks of the core Open-Xchange Middleware starting from v7.8.3.

- ``com.openexchange.publish.database.PublicationsAddEntityIndex``  
  Adds the index "entity (cid,module,entity)" to the table "publications".
- ``com.openexchange.tools.oxfolder.RemoveInconsistentLocksUpdateTasks``  
  Removes all file locks which may be hold by any user which doesn't have any permissions to do so anymore.
- ``com.openexchange.oauth.impl.internal.groupware.RenameMigrateLinkedInServiceIdUpdateTask``  
  Updates the service identifier and scope for LinkedIN accounts stored in the table "oauthAccounts".
- ``com.openexchange.groupware.update.tasks.AddOAuthColumnToMailAccountTableTask``  
  Adds "oauth" column to the "user_mail_account" and "user_transport_account" tables.
- ``com.openexchange.oauth.impl.internal.groupware.OAuthAddScopeColumnTask``  
  Adds the column "scope" to the "oauthAccounts" table.
- ``com.openexchange.oauth.impl.internal.groupware.OAuthCreateTableTask2``  
  Corrects the column definitions for the table "oauthAccounts" if needed.
- ``com.openexchange.oauth.impl.internal.groupware.OAuthCreateTableTask``  
  Creates the "oauthAccounts" table.
- ``com.openexchange.filestore.impl.groupware.MakeQuotaMaxConsistentInUserTable``  
  Ensures a NOT NULL value for "quota_max" column in "user" and "del_user" tables.
- ``com.openexchange.groupware.update.tasks.AllowNullValuesForStandardFolderNamesUpdateTask``  
  Updates the "user_mail_account" table to allow NULL values in folder names.
- ``com.openexchange.groupware.update.tasks.FolderDefaultValuesForDelTablesTasks``  
  Adds default values to the 'del_oxfolder_tree', and 'virtualBackupTree' tables.
- ``com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsMakeUuidPrimaryTask``  
  Changes the column defintion for "uuid" to "uuid BINARY(16) NOT NULL" in the "driveEventSubscriptions" table, fills it with random values, then changes the primary key to "(cid,uuid)". Also, an additional index for "(cid,service,token)" is added.
- ``com.openexchange.download.limit.rdb.FileAccessCreateTableTask``  
  Creates the "fileAccess" table.
- ``com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsAddUuidColumnTask``  
  Adds the column "uuid BINARY(16) DEFAULT NULL" to the table "driveEventSubscriptions".
- ``com.openexchange.groupware.update.tasks.UserSettingMediumTextTask``  
  Applies "MEDIUM TEXT" to "user_setting" table.
- ``com.openexchange.pns.subscription.storage.groupware.PnsCreateTableTask``  
  Creates tables for the "pns" bundle.
- ``com.openexchange.pns.subscription.storage.groupware.PnsSubscriptionsReindexTask``  
  Adds the 'client' column to the table's primary key.


# Error scenarios

## Unexpected termination

A typical error scenario is when the JVM or the executing update task is terminated unexpectedly; e.g. by killing the Java process. In that case, the update task mechanism is not able to remove special markers on affected database schemas, which leaves those schemas in an unaccessible state.

To detect or remedy from such a situation, an administrator can execute the ``checkdatabase`` command-line interface which lists all database schemas that either need an update, are currently blocked by a running update or are considered as stale (updating for more than 24 hours).

Example:

```
  id name      hostname            scheme       status                               
1996 database1 db.open-xchange.com database1_73 Needs update                         
1996 database1 db.open-xchange.com database1_75 Needs update                         
1996 database1 db.open-xchange.com database1_77 Needs update                         
1996 database1 db.open-xchange.com database1_79 Blocking updates running                         
1996 database1 db.open-xchange.com database1_81 Blocking updates running                         
1996 database1 db.open-xchange.com database1_83 Needs update                         
1996 database1 db.open-xchange.com database1_85 Needs update                         
1996 database1 db.open-xchange.com database1_91 Blocking updates running for too long
1996 database1 db.open-xchange.com database1_92 Blocking updates running for too long
```

To unblock such schemas that have their status set to ``Blocking updates running for too long`` and are known by administrator that no update should be running, the ``unblockdatabase`` command-line interface is available. That command-line interface may be used to either unblock a certain database schema or unblock all schemas of a certain database that are considered as stale (last-modified time stamp more than 24 hours in the past).

Taking the exemplary output an invocation of ``./unblockdatabase -i 1996 -A oxadminmaster -P secret`` yields:

```
unblocked the following schemas from database 1996:

  id name      hostname            scheme         
1996 database1 db.open-xchange.com database1_91
1996 database1 db.open-xchange.com database1_92
```

## Ordinary failure

Although an update task is supposed to never fail, it may occur due to untested environments and/or database versions that an update task may fail. In contrast to the previous section, any markers are removed; meaning the database schema is still accessible. However, needed modifications were not applied, which might lead to errors during runtime.

To check the update tasks' status for a certain database schema the ``listExecutedUpdateTasks`` command-line interface is supposed to be used, which lists all executed update task for denoted schema along-side with each task's status (successful vs. failed)

```
./listExecutedUpdateTasks -l admin -s secret -n openxchangedb
taskName                                successful lastModified
com.openexchange.tasks.SuccessfulTask01 true       2016-12-23 10:43:51 CET
com.openexchange.tasks.SuccessfulTask02 true       2016-12-23 10:43:51 CET
com.openexchange.tasks.SuccessfulTask03 true       2016-12-23 10:43:51 CET
com.openexchange.tasks.SuccessfulTask04 true       2016-12-23 10:43:51 CET
com.openexchange.tasks.SuccessfulTask05 true       2016-12-23 10:43:51 CET
com.openexchange.tasks.FailedTask01     false      2016-12-23 10:43:51 CET
com.openexchange.tasks.FailedTask02     false      2016-12-23 10:43:51 CET
com.openexchange.tasks.SuccessfulTask06 true       2016-12-23 10:43:51 CET
com.openexchange.tasks.SuccessfulTask07 true       2016-12-23 10:43:51 CET
...
com.openexchange.tasks.SuccessfulTaskN  true       2016-12-23 10:43:51 CET
```

A failed update task can be re-run using the ``forceupdatetask`` command-line tool

```
./forceupdatetask -l admin -s secret -n openxchangedb -t com.openexchange.tasks.FailedTask01
```

# Command-line interfaces

## (Re-)Run a certain update task

As mentioned above the ``forceupdatetask`` command-line interface is supposed to be executed in order to (re-)run a certain update task either for a specific database schema or for all available database schemas.

### Parameters

 - ``-c,--context <arg>``  
 A valid context identifier contained in target schema; if missing and '-n/--name' option is also absent all schemas are considered.
 - ``-H,--host <arg>``  
 The optional JMX host (default:localhost)
 - ``-h,--help``  
 Prints a help text
 - ``-l,--login <arg>``  
 The optional JMX login (if JMX has authentication enabled)
 - ``-n,--name <arg>``  
 A valid schema name. This option is a substitute for ``-c/--context`` option. If both are present ``-c/--context`` is preferred. If both absent all schemas are considered.
 - ``-p,--port <arg>``  
 The optional JMX port (default:9999)
 - ``--responsetimeout <arg>``  
 The optional response timeout in seconds when reading data from server (default: 0s; infinite)
 - ``-s,--password <arg>``  
 The optional JMX password (if JMX has authentication enabled)
 - ``-t,--task <arg>``  
 The update task's class name

### Examples

```
./forceupdatetask -l admin -s secret -n openxchangedb -t com.openexchange.tasks.FailedTask01
```

## List executed update tasks for a schema

The ``listExecutedTasks`` is supposed to be used in order to list all update tasks that have been executed on a specified database schema.

### Parameters

- ``-H,--host <arg>``  
The optional JMX host (default:localhost)
- ``-h,--help``  
Prints a help text.
- ``-l,--login <arg>``  
The optional JMX login (if JMX has authentication enabled)
- ``-n,--name <arg>``  
A valid schema name.
- ``-p,--port <arg>``  
The optional JMX port (default:9999)
- ``--responsetimeout <arg>``  
The optional response timeout in seconds when reading data from server (default: 0s; infinite)
- ``-s,--password <arg>``  
The optional JMX password (if JMX has authentication enabled)

### Examples

```
./listExecutedUpdateTasks -l admin -s secret -n openxchangedb
taskName                                                                               successful lastModified
com.openexchange.groupware.update.tasks.CalendarExtendDNColumnTask                     true       2009-12-23 10:43:51 CET
com.openexchange.groupware.update.tasks.MailAccountAddPersonalTask                     true       2009-12-23 10:43:51 CET
com.openexchange.groupware.update.tasks.ContactsAddIndex4AutoCompleteSearch            true       2009-12-23 10:43:51 CET
com.openexchange.groupware.update.tasks.DelFolderTreeTableUpdateTask                   true       2009-12-23 10:43:51 CET
com.openexchange.groupware.update.tasks.MALPollCreateTableTask                         true       2009-12-23 10:43:51 CET
com.openexchange.groupware.update.tasks.CreatePublicationTablesTask                    true       2009-12-23 10:43:51 CET
 ...
com.openexchange.drive.checksum.rdb.DirectoryChecksumsAddUsedColumnTask                true       2016-03-16 16:33:10 CET
com.openexchange.drive.checksum.rdb.DirectoryChecksumsReIndexTaskV2                    true       2016-03-16 16:33:10 CET
com.openexchange.usm.database.ox.update.USMDeleteStoredProceduresUpdateTaskV2          true       2016-04-11 14:37:30 CEST
com.openexchange.groupware.update.tasks.AddStartTLSColumnForMailAccountTablesTask      true       2016-04-11 14:37:30 CEST
com.openexchange.groupware.update.tasks.UserSettingMediumTextTask                      true       2016-04-29 07:57:44 CEST
com.openexchange.groupware.update.tasks.Release781UpdateTask                           true       2016-05-01 09:12:07 CEST
com.openexchange.share.limit.rdb.FileAccessCreateTableTask                             true       2016-05-12 10:04:18 CEST
com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsAddUuidColumnTask   true       2016-05-30 11:31:40 CEST
com.openexchange.download.limit.rdb.FileAccessCreateTableTask                          true       2016-06-08 16:51:59 CEST
com.openexchange.drive.events.subscribe.rdb.DriveEventSubscriptionsMakeUuidPrimaryTask true       2016-06-23 11:57:13 CEST
```

## Trigger all pending update tasks for a specific schema

To apply all pending update tasks to a certain schema, the ``runupdate`` command-line interface is available, provided that specified schema is not yet marked as being updated. In case the schema is already blocked, the invocation does nothing.

Once the schema is updated, the tool lists those update tasks, that could not be successfully executed on denoted database schema.

### Parameters

- ``-c,--context <arg>``  
A valid context identifier contained in target schema
- ``-H,--host <arg>``  
The optional JMX host (default:localhost)
- ``-h,--help``  
Prints a help text
- ``-l,--login <arg>``  
The optional JMX login (if JMX has authentication enabled)
- ``-n,--name <arg>``  
A valid schema name. This option is a substitute for ``-c/--context`` option. If both are present ``-c/--context`` is preferred.
- ``-p,--port <arg>``  
The optional JMX port (default:9999)
- ``--responsetimeout <arg>``  
The optional response timeout in seconds when reading data from server (default: 0s; infinite)
- ``-s,--password <arg>``  
The optional JMX password (if JMX has authentication enabled)

### Examples

```
./runupdate -l admin -s secret -n openxchangedb
The following update task(s) failed:
 com.openexchange.tasks.FailedTask01 (schema=openxchangedb)
 com.openexchange.tasks.FailedTask02 (schema=openxchangedb)
```

## Trigger all pending update tasks for all schemas

To apply all pending update tasks to all available schemas, the ``runallupdate`` command-line interface is available. That tool supports two modes. The first one is to ignore possible errors and/or conflicts on a certain schema and to continue with the next schema in line. The other does abort the invocation whenever an error and/or conflict occurs.

Moreover, that tool starts a task that performs the actual update asynchronously. Therefore, the current status/progress is periodically printed to standard out.

### Parameters

- ``-e,--error``  
The flag indicating whether process is supposed to be stopped if an error occurs when trying to update a schema.
- ``-H,--host <arg>``  
The optional JMX host (default:localhost)
- ``-h,--help``  
Prints a help text
- ``-l,--login <arg>``  
The optional JMX login (if JMX has authentication enabled)
- ``-p,--port <arg>``  
The optional JMX port (default:9999)
- ``--responsetimeout <arg>``  
The optional response timeout in seconds when reading data from server (default: 0s; infinite)
- ``-s,--password <arg>``  
The optional JMX password (if JMX has authentication enabled)

### Example

Invocation with ``error`` flag enabled; encountering an error:

```
./runallupdate  -l admin -s secret -e
Attempting to update 167 schemas in total...
Processed 27 of 167 schemas.
Processed 27 of 167 schemas.
Processed 27 of 167 schemas.
Processed 27 of 167 schemas.
Processed 29 of 167 schemas.
Processed 29 of 167 schemas.
Processed 29 of 167 schemas.
Processed 29 of 167 schemas.
Processed 29 of 167 schemas.
Processed 33 of 167 schemas.
Processed 33 of 167 schemas.
Processed 33 of 167 schemas.
Processed 33 of 167 schemas.
Processed 42 of 167 schemas.
Processed 42 of 167 schemas.
Processed 42 of 167 schemas.
Processed 42 of 167 schemas.
Update conflict detected. Another process is currently updating schema database1_77.
```

Invocation without ``error`` flag enabled:  
Possible errors/warnings are written to logging

```
./runallupdate  -l admin -s secret
Attempting to update 167 schemas in total...
Processed 44 of 167 schemas.
Processed 44 of 167 schemas.
Processed 44 of 167 schemas.
Processed 45 of 167 schemas.
Processed 45 of 167 schemas.
Processed 47 of 167 schemas.
Processed 47 of 167 schemas.
Processed 61 of 167 schemas.
Processed 88 of 167 schemas.
Processed 88 of 167 schemas.
Processed 167 of 167 schemas.
```

## Checking for pending, blocked or stale schemas

To check for pending, blocked or stale schemas, the ``checkdatabase`` command-line interface is supposed to be used. As mentioned in previous section, this tool lists all database schemas that either need an update, are currently blocked by a running update or are considered as stale (updating for more than 24 hours).

### Parameters

- ``-h,--help``  
Prints a help text                                                 
- ``--responsetimeout <arg>``  
The optional response timeout in seconds for reading response from the backend (default 0s; infinite)
- ``-A,--adminuser <arg>``  
Admin username              
- ``-P,--adminpass <arg>``  
Admin password

### Examples

Example:

```
  id name      hostname            scheme       status                               
1996 database1 db.open-xchange.com database1_73 Needs update                         
1996 database1 db.open-xchange.com database1_75 Needs update                         
1996 database1 db.open-xchange.com database1_77 Needs update                         
1996 database1 db.open-xchange.com database1_79 Blocking updates running                         
1996 database1 db.open-xchange.com database1_81 Blocking updates running                         
1996 database1 db.open-xchange.com database1_83 Needs update                         
1996 database1 db.open-xchange.com database1_85 Needs update                         
1996 database1 db.open-xchange.com database1_91 Blocking updates running for too long
1996 database1 db.open-xchange.com database1_92 Blocking updates running for too long
```

## Unblocking stale schemas

Such schemas considered as stale may be unblocked by an administrator in order to make that schema re-accessible. For that purpose the ``unblockdatabase`` command-line interface is supposed to be used. This command-line interface may be used to either unblock a certain database schema or unblock all schemas of a certain database that are considered as stale (last-modified time stamp more than 24 hours in the past).

### Parameters

- ``-h,--help``  
Prints a help text          
- ``--responsetimeout <arg>``  
The optional response timeout in seconds for reading response from the backend (default 0s; infinite)
- ``-A,--adminuser <arg>``  
Admin username              
- ``-P,--adminpass <arg>``  
Admin password              
- ``-i,--id <arg>``  
The id of the database.     
- ``-n,--name <arg>``  
Name of the database        
- ``--schema <arg>``  
The optional schema name of the database.

### Examples

```
./unblockdatabase -i 1996 -A oxadminmaster -P secret
unblocked the following schemas from database 1996:

  id name      hostname            scheme         
1996 database1 db.open-xchange.com database1_91
1996 database1 db.open-xchange.com database1_92
```
