---
title: usercopy
---

# Copies a user

This command-line-tool allows to copy a user from one filestore to another.

The following informations will be copied:

* Contacts
* Calender entries
* Folders
* Infostore documents
* Mail accounts
* Messaging accounts
* OAuth accounts
* Reminder
* Subscriptions
* Tasks
* UserCounts
* UserSettings


## Parameters

 - ``-A,--adminuser <arg>``<br>
 Master admin user name for authentication
 - ``-P,--adminpass <arg>``<br>
 Master admin password for authentication
 - ``--responsetimeout <arg>``<br>
 The optional response timeout in seconds when reading data from server (default: 0s; infinite)
  - ``-c,--srccontextid <arg>``<br>
 The id of the source context
 - ``-d,--destcontextid <arg>``<br>
 The id of the destination context
 - ``-i,--userid <arg>``<br>
 The id of the user which should be copied
 - ``-u,--username <arg>``<br>
 The name of the user which should be copied

### Examples

```
./usercopy -A masteradmin -P secret -c 1 -d 2 -i 3
```
Copies the user 3 from context 1 to context 2.

```
./usercopy -A masteradmin -P secret -c 1 -d 2 -u Max
```
Copies the user with the name Max from context 1 to context 2.

 
