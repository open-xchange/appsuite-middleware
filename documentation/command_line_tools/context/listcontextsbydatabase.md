---
title: listcontextsbydatabase
icon: far fa-circle
tags: Administration, Command Line tools, Context, Database
package: open-xchange-admin
---

# NAME

listcontextsbydatabase - lists all contexts that are assigned to a specific database.

# SYNOPSIS

**listcontextsbydatabase** [OPTION]...

# DESCRIPTION

This command line tool lists all contexts that are assigned to a specific database. Optionally, with the use of '-s' you can search for a context name that matches the specified search pattern.

# OPTIONS

**-d**, **--database** *databaseId*
: The database identifier. Mandatory and mutually exclusive with `-n`.

**-n**, **--name** *databaseName*
: The database name. Mandatory and mutually exclusive with `-d`.

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**--csv**
: Format output to csv.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**listcontextsbydatabase -A masterAdmin -P secret -d 1138**

Lists all contexts in the specified database.

# SEE ALSO

[createcontext(1)](createcontext), [enablecontext(1)](enablecontext), [changecontext(1)](changecontext), [enableallcontexts(1)](enableallcontexts), [disableallcontexts(1)](disableallcontexts), [deletecontext(1)](deletecontext), [disablecontext(1)](disablecontext), [getcontextcapabilities(1)](getcontextcapabilities)
