---
title: listExecutedUpdateTasks
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

listExecutedUpdateTasks - lists all executed UpdateTasks.

# SYNOPSIS

**listExecutedUpdateTasks** [OPTIONS] | [-h]

# DESCRIPTION

This command line tool lists executed update tasks of a schema. This command line tool is due to deprecation with the next release. Use the
'listUpdateTasks' instead.

# OPTIONS

**-A**, **--adminuser** *adminUser*
: The admin username
 
**-h**, **--help**
: Prints a help text

**-n**, **--name** *schemaName*
: A valid schema name.

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**-P**, **--adminpass** *adminPassword* 
: The admin password

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)


# SEE ALSO

[listUpdateTasks(1)](listUpdateTasks)