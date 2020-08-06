---
title: movecontextdatabase
icon: far fa-circle
tags: Administration, Command Line tools, Context, Database
package: open-xchange-admin
---

# NAME

movecontextdatabase - moves the context to a different database. 

# SYNOPSIS

**movecontextdatabase** [OPTION]...

# DESCRIPTION

This command line tool moves the context to a different database. 

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory and mutually exclusive with `-N`.

**-N**, **--contextname** *contextName*
: The context name. Mandatory and mutually exclusive with `-c`.

**-d**, **--database** *databaseId*
: The id of the target database. Mandatory and mutually exclusive with `-n`. The target database must be a master database.
 
**-n**, **--name** *databaseName*
: Name of the target database. Mandatory and mutually exclusive with `-d`. The target database must be a master database.

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

**movecontextdatabase -A masterAdmin -P secret -c 1138 -d 1337**

Moves the specified context to the specified database.

# SEE ALSO

[createcontext(1)](createcontext), [enablecontext(1)](enablecontext), [changecontext(1)](changecontext), [enableallcontexts(1)](enableallcontexts), [disableallcontexts(1)](disableallcontexts), [deletecontext(1)](deletecontext), [disablecontext(1)](disablecontext), [listcontext(1)](listcontext)
