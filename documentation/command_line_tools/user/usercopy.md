---
title: usercopy
icon: far fa-circle
tags: Administration, Command Line tools, User
---

# NAME

usercopy - copies a user to another context

# SYNOPSIS

**usercopy** [OPTION]...

# DESCRIPTION

This command line tool copies a user from one context to another.

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

# OPTIONS

**-h**, **--help**
: Prints a help text.

**-c**, **--srccontextid** *contextId*
: The context identifier of the source context. Mandatory.

**-d**, **--destcontextid** *contextId*
: The context identifier of the destination context. Mandatory.

**-i**, **--userid** *id*
: The id of the user which should be copied

**-u**, **--username** *name*
: The name of the user which should be copied

**-A**, **--adminuser** *contextAdmin*
: Context admin user name for authentication.

**-P**, **--adminpass** *contextAdminPassword*
: Context admin password for authentication.

# EXAMPLES

**usercopy -A contextAdmin -P secret -c 1138 -d 1139 -i 3**

Copies the user with identifier 3 from context 1138 to the context 1139


# SEE ALSO

[createuser(1)](createuser), [deleteuser(1)](deleteuser), [changeuser(1)](changeuser), [getusercapabilities(1)](getusercapabilities)
