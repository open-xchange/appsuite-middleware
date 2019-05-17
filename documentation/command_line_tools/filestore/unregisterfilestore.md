---
title: unregisterfilestore
icon: far fa-circle
tags: Administration, Command Line tools, Filestore
---

# NAME

unregisterfilestore - registers a filestore.
# SYNOPSIS

**unregisterfilestore** [-h|--help]

**unregisterfilestore** -A *masterAdminUser* -P *masterAdminPassword* -i *id* [--responsetimeout *seconds* --nonl]

# DESCRIPTION

This command line tool registers a filestore.

# OPTIONS

**-i**, **--id** *id*
: The id of the filestore which should be unregistered. Mandatory.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**unregisterfilestore -A masteradmin -P secret -i 1138**

Unregisters the filestore with the specified identifier.

# SEE ALSO

[registerfilestore(1)](registerfilestore), [listfilestore(1)](listfilestore), [changefilestore(1)](changefilestore), [recalculatefilestoreusage(1)](recalculatefilestoreusage)