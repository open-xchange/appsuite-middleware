---
title: listUpdateTasks
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema, Update Task
package: open-xchange-core
---

# NAME

listUpdateTasks

# SYNOPSIS

**listUpdateTasks** [[-a | -g | -e | -x | -xf | -xn] -n *schemaName* -A *masterAdminUser* -P *masterAdminPassword*
                       [-p *port* -s *server* --responsetime *responseTime*]] | -h

# DESCRIPTION

Lists executed, pending and excluded update tasks of a schema specified by the '-n' switch (mandatory). The switches
'-a', '-e', '-g', '-x', '-xf' and '-xn' are mutually exclusive AND mandatory.

 An overall database status of all schemata can be retrieved via the 'checkdatabase' command line tool.

An update task may be in 'pending' state for three reasons:
  a) It was never executed before and is due for execution
  b) It is excluded via a namespace
  c) It is excluded via an entry in the 'excludeupdatetasks.properties'

# OPTIONS

**-a**,**--all**
: Lists all pending and excluded update tasks (both via excludeupdatetasks.properties' file and namespace)

**-e**,**--executed**
: Lists all executed (ran at least once) update tasks of a schema

**-g**,**--pending**
: Lists only the pending update tasks, i.e. those that were never executed but are due for execution.

**-x**,**--excluded**
: Lists only the update tasks excluded both via excludedupdate.properties' file and namespace

**-xf**,**--excluded-via-file**
: Lists only the update tasks excluded via 'excludeupdatetasks.properties' file
 
**-xn**,**--excluded-via-namespace**
: Lists only the update tasks excluded via namespace

**-n**,**--name** *arg*
: A valid schema name. Can be retrieved via `listdatabaseschema` CLT

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# SEE ALSO

[listdatabaseschema(1)](listdatabaseschema), [checkdatabase(1)](checkdatabase)