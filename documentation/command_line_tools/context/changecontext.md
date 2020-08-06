---
title: changecontext
icon: far fa-circle
tags: Administration, Command Line tools, Context
package: open-xchange-admin
---

# NAME

changecontext - creates a context.

# SYNOPSIS

**changecontext** [OPTION]...

# DESCRIPTION

This command line tool makes context-wide changes.

If you specify module access options; e.g. "--access-edit-password on"; then please be aware that basic module access set is the one from context's administrator. Meaning any option not explicitly specified as CLI argument will fall-back to context administrator setting for _every_ user in associated context.

You can use changecontext to change the current quota for a given context. When the context has more changecontext space in use than the new quota allows, the customer is only able to delete files until the usage is below quota. Module access (calendar,tasks,email) can be set via predefined "access combination names". These names can be configured on the server side. All users which are created during later use of the "createuser" tool will inherit the module access rights from the context. If you do not specify any access rights on createcontext minimal access rights will be granted. Currently, these are Webmail and Contacts access rights.

There are some default combinations in the ModuleAccessDefinitions.properties file on the admin server, like: 

webmail=webmail, contacts, globaladdressbookdisabled, collectemailaddresses, editpassword
pim=webmail, calendar, contacts, tasks, globaladdressbookdisabled, collectemailaddresses, multiplemailaccounts, subscription, publication, editpassword
pim_infostore=webmail, calendar, contacts, tasks, infostore, webdav, globaladdressbookdisabled, collectemailaddresses, multiplemailaccounts, subscription, publication
pim_mobility=webmail, calendar, contacts, tasks, syncml, usm, activesync, globaladdressbookdisabled, collectemailaddresses, multiplemailaccounts, subscription, publication, editpassword
groupware_standard=webmail, calendar, contacts, infostore, tasks, webdav, ical, vcard, readcreatesharedfolders, delegatetask, editpublicfolders, editgroup, editresource, editpassword, collectemailaddresses, multiplemailaccounts, subscription, publication (Groupware Standard always gets new features except mobility and OXtender. )
groupware_premium=webmail, calendar, contacts, infostore, tasks, webdav, ical, vcard, syncml, usm, olox20, activesync, readcreatesharedfolders, delegatetask, editpublicfolders, editgroup, editresource, editpassword, collectemailaddresses, multiplemailaccounts, subscription, publication
all=webmail, calendar, contacts, infostore, tasks, webdav, ical, vcard, syncml, usm, olox20, activesync, readcreatesharedfolders, delegatetask, editpublicfolders, editgroup, editresource, editpassword, publicfoldereditable, collectemailaddresses, multiplemailaccounts, subscription, publication (This is a right tailored to a context administrator) 

Note: Italics denote additional rights in comparison to the previous set where applicable.

When having changed the access rights of the context and its users with "changecontext" the "downgrade" command should be called on the admin server. All unnecessary data are removed from the data base via "groupware api". If e. g. the context 1 is changed from "pim_infostore" to "webmail", the "downgrade" command has to be called for this context then. Then, all unnecessary folders for this context are removed from the data base. 

# OPTIONS

**-c**, **--contextid** *contextId*
: The id of the context, when starting with 0, 0 is deleted.

**-N**, **--contextname** *contextName*
: Context name.

**-L**, **--addmapping** *mapping*
: Add login mappings separated by ",".

**-R**, **--removemapping** *mapping*
: Remove login mappings. Separated by ",".

**-q**, **--quota** *quota*
: Quota for the context filestore in MB . 

**-N**, **--contextname** *contextName*
: The context name. Mandatory and mutually exclusive with `-c`.

**--access-combination-name** *access-combination-name*
: Access combination name.

**--capabilities-to-add** *capabilities-to-add*
: The capabilities to add as a comma-separated string (from 7.2.0 on).

**--capabilities-to-remove** *capabilities-to-remove*
: The capabilities to remove as a comma-separated string (from 7.2.0 on).

**--capabilities-to-drop** *capabilities-to-drop*
: The capabilities to drop; e.g. cleanse from storage; as a comma-separated string (from 7.6.0 on)

**--quota-module** *quota-module*
: The identifier of the module to which to apply the quota value (from 7.2.0 on)

**--quota-value** *quota-value*
: From v7.2.0 on: The quota value; zero is unlimited. From v7.6.0 on: The numeric quota value specifying the max. number of items allowed for context. Zero is unlimited. A value less than zero deletes the quota entry (and falls back to configured behavior) 

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

# EXAMPLES

**changecontext -A masterAdmin -P masterPassword -c 123 -q 5000**

Changes the quota of the specified context

# SEE ALSO

[deletecontext(1)](deletecontext), [listcontext(1)](listcontext), [createcontext(1)](createcontext), [enablecontext(1)](enablecontext), [disablecontext(1)](disablecontext), [disableallcontexts(1)](disableallcontexts), [enableallcontexts(1)](enableallcontexts), [getcontextcapabilities(1)](getcontextcapabilities)