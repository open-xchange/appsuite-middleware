---
title: deleteemptyschema
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema
---

# NAME

deleteemptyschema - deletes empty database schemata

# SYNOPSIS

**deleteemptyschema** [-h|--help]

**deleteemptyschema** -A *masterAdminUser* -P *masterAdminPassword* [-i *database_id*][--responsetimeout *seconds*]

# DESCRIPTION

This command line tool allows to delete (and retain) empty database schemata.

# OPTIONS

**-i**, **--id** *id*
: The optional identifier of a certain database host. If omitted all database hosts are considered.

**-n**, **-name** *name*
: The optional name of a certain database host (as alternative for "id" option). If omitted all database hosts are considered.

**--schema** *schema*
: The optional schema name of the database.

**-schemas-to-keep** *schemataToKeep*
: (Optionally) Specifies the number of empty schemas that are supposed to be kept (per database host). If omitted, all empty schemata are attempted to be deleted. Ineffective if `--schema` option is specified.

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

**deleteemptyschema -A masteradmin -P secret -i 3 --schemas-to-keep foobar,oxio**

Deletes all empty schemata and keeps the specified ones.

# SEE ALSO

[createschema(1)](createschema), [createschemas(1)](createschema), [listdatabaseschema(1)](listdatabaseschema)
