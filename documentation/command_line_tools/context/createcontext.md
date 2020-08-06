---
title: createcontext
icon: far fa-circle
tags: Administration, Command Line tools, Context
package: open-xchange-admin
---

# NAME

createcontext - creates a context.

# SYNOPSIS

**createcontext** [OPTION]...

# DESCRIPTION

This command line tool creates create new contexts. A context is an independent instance within the Open-Xchange system and holds users, groups and resources and all their objects. Data from one context is not visible to other contexts. Module access (calendar, tasks, email) can be set via predefined "access combination names". These names can be configured on the server side. All users which are created during later use of the "`createuser(1)`" tool will inherit the module access rights from the context. If you do not specify any access rights on `createcontext(1)` minimal access rights will be granted. Currently, these are Webmail and Contacts access rights.

# OPTIONS

**-c**, **--contextid** *contextId*
: The id of the context, when starting with 0, 0 is deleted.

**-q**, **--quota** *quota*
: Context wide filestore quota in MB. -1 = unlimited. Note: The context-associated filestore is not only used by Infostore/Drive module, but also for other features like snippets/signatures, thumbnail cache, PIM (contacts, calendar & tasks) attachments, etc. Thus even if you don't use the Infostore/Drive, you should always set an appropriate amount so users can e.g. store signatures or attach files to PIM items. 

**-u**, **--username** *username*
: Username for the new context admin user.

**-d**, **--displayname** *displayName*
: Displayname for the new context admin user.

**-g**, **--givenname** *givenName*
: Given name for the new context admin user.

**-s**, **--surname** *surname*
: Surname/last name for the new context Admin user.

**-p**, **--password** *password*
: Password for the new context admin user.

**-e**, **--email** *email*
: Primary E-Mail address for the new context Admin user.

**-l**, **--lang** *language*
: Language for the new context Admin user.

**-t**, **--timezone** *timezone*
: Timezone for the new context Amin user.

**-N**, **--contextname** *contextName*
: Context name.

**-L**, **--addmapping** *mapping*
: Add login mappings separated by ",".

**-F**, **--destination-store-id** *storeId*
: Specifies the optional file store identifier to which the context gets assigned; if missing the file store gets auto-detected.

**-D**, **--destination-database-id** *databaseId*
: Specifies the optional database identifier to which the context gets assigned; if missing the database gets auto-detected

**--access-combination-name** *access-combination-name*
: Access combination name.

**--access-denied-portal** *on/off*
: Denies portal access (Default is off)

**--csv-import** *CSV file*
: Full path to CSV file with user data to import. This option makes mandatory options obsolete, except credential options (if needed). 

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

**--gabMode** *gabMode*
: The optional modus the global address book shall operate on. Currently 'global' and 'individual' are known values. If the mode 'global' is chosen, the special "all users and groups" permission will grant access to the global address book for users. If the mode 'individual' is chosen, each user will have a dedicated permission for the global address book. 'individual' is the default. Please keep in mind that the modus will affect the response for folder requests regarding the global address book.

# Importing CSV Files
With the `--csv-import <CSV file>` option a full path to a CSV file with user data to import can be specified. This option makes mandatory command line options obsolete, except credential options (if needed). But they have to be set in the CSV file.

With this option you can specify a csv file (a full pathname must be given) with the data which should be imported. The columnnames in the CSV file must be the same as the long-options of the command line tools, without the prefix "--".

This option will normally be used to fill new large installations with the new data. So instead of calling the command line tools in a shell script every time, just a csv file needs to be created, containing the whole data.

Note that the credentials of the masteradmin in the createcontext call must be given on the command line with the -A and -P options nevertheless - if authentication is enabled. If the createuser command line tool is used, the credentials are part of the csv file, and cannot be set as options on the command line itself. The reason for this different behavior is that different contexts have different credentials for the admin user, so they must be set in every line of the csv file. Opposed to this the credentials of the masteradmin are always the same. 

# EXAMPLES

**createcontext -A masterAdmin -P masterPassword -c 123 -q 1000 -N CompanyA -u "admin" -d "Admin of CompanyA" -g John -s Example -p newpw -e john@example.com**

Creates a context.

# SEE ALSO

[deletecontext(1)](deletecontext), [listcontext(1)](listcontext), [changecontext(1)](changecontext), [enablecontext(1)](enablecontext), [disablecontext(1)](disablecontext), [disableallcontexts(1)](disableallcontexts), [enableallcontexts(1)](enableallcontexts), [getcontextcapabilities(1)](getcontextcapabilities), [restoregabdefaults](1)