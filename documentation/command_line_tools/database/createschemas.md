---
title: createschemas
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema
---

# NAME

createschemas - creates a number database schemata which can be used for the creation of contexts.


# SYNOPSIS

**createschemas** [-h|--help]

**createschemas** -A *masterAdminUser* -P *masterAdminPassword* [-i *database_id*][--responsetimeout *seconds*]

# DESCRIPTION

This command line tool allows to create a number database schemata which can be used during the creation of contexts.

# OPTIONS

**-i**, **--id** *id*
: The database id. Mutually exclusive with `-n`.

**-n**, **-name** *name*
: The database name. Mutually exclusive with `-i`.

**--userdb-schema-count** *userdb-schema-count*
: (Optionally) Specifies the number of userdb schemas that are supposed to be pre-created. If missing, number of schemas is calculated by "maxunit" divided by CONTEXTS_PER_SCHEMA config option from '`hosting.properties`'.

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

**createschemas -A masteradmin -P secret -i 3 --userdb-schema-count 10**

Creates 10 new schemata in database with the specified identifier.

# SEE ALSO

[createcontext(1)](createcontext), [createschema(1)](createschema), [deleteemptyschema(1)](deleteemptyschema), [listdatabaseschema(1)](listdatabaseschema), [checkdatabase(1)](checkdatabase), [countdatabaseschema(1)](countdatabaseschema)
