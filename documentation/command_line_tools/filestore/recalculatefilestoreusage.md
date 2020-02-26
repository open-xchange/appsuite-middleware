---
title: recalculatefilestoreusage
icon: far fa-circle
tags: Administration, Command Line tools, Filestore
package: open-xchange-admin
---

# NAME

recalculatefilestoreusage - recalculates the usage of file storages.

# SYNOPSIS

**recalculatefilestoreusage** [-h|--help]

**recalculatefilestoreusage** -A *masterAdminUser* -P *masterAdminPassword* [[--scope *scope*]|[-c *contextId* -u *userId*]][--responsetimeout *seconds*]

# DESCRIPTION

This command line tool lists recalculates the usage of file storages.

# OPTIONS

**-c**, **--context** *contextId*
: The identifier of the context for which the file storage usage shall be recalculated. If a user identifier is also specified, only the user-associated file storage is considered.

**-u**, **--user** *userId*
: The identifier of the user for which the file storage usage shall be recalculated or 'all' to recalculates the usages for all user-associated file storage in the given context.

**--scope** *scope*
: Scope can be either set to either 'all', 'context' or 'user'. If set to 'all', all usages of all context and user file stores are recalculated. Otherwise the usages of either context- or user-associated file storages are recalculated. Cannot be used in conjunction with the '`--context`' or '`--user`'.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication.

**-s**, **--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**, **--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

**-h**, **--help**
: Prints a help text.

# EXAMPLES

**recalculatefilestoreusage -A masteradmin -P secret -c 1138**

Re-calculates the filestore usage for the specified context.

**recalculatefilestoreusage -A masteradmin -P secret -c 1138 -u 1337**

Re-calculates the filestore usage for the specified user in the specified context.

**recalculatefilestoreusage -A masteradmin -P secret --scope all**

Re-calculates the filestore usage of all context and user filestores.

# SEE ALSO

[registerfilestore(1)](registerfilestore), [unregisterfilestore(1)](unregisterfilestore), [changefilestore(1)](changefilestore), [listfilestore(1)](listfilestore)