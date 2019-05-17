---
title: unregisterdatabase
icon: far fa-circle
tags: Administration, Command Line tools, Database
---

# NAME

unregisterdatabase - unregisters a database.

# SYNOPSIS

**unregisterdatabase** [-h|--help]

**unregisterdatabase** [--environment]

**unregisterdatabase** -A *masterAdminUser* -P *masterAdminPassword* [-i *database_id* | -n *name*][--responsetimeout *seconds* --nonl]

# DESCRIPTION

This command line tool unregisters a database.

# OPTIONS

**-i**, **--id** *id*
: The database identifier. Mutually exclusive with `-n`.

**-n**, **--name** *name*
: The database name. Mutially exclusive with `-i`.

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

**unregisterdatabase -A masteradmin -P secret -i 4**

Unregisters the database with the specified identifier.

# SEE ALSO

[registerdatabase(1)](registerdatabase), [listdatabase(1)](listdatabase), [changedatabase(1)](changedatabase)
