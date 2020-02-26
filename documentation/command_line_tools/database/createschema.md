---
title: createschema
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema
package: open-xchange-admin
---

# NAME

createschema - creates additional database schemata which can be used for the creation of contexts.


# SYNOPSIS

**createschema** [-h|--help]

**createschema** -A *masterAdminUser* -P *masterAdminPassword* [-i *database_id*][--responsetimeout *seconds*][--csv]


# DESCRIPTION

This command line tool allows to create additional database schemata which can be used
during the creation of contexts.

It's either possible to determine the database where the schema is going to be created or to let the middleware automatically decide where to create it.

Please note that the created schema(ta) will **not** be available for the automatic schema
select strategy. For that purpose use the '--schema' parameter for context creation. See [createcontext(1)](createcontext(1).

# OPTIONS

**-i**, **--id** *id*
: An optional database id.

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

**--csv**
: Format output to csv.


# EXAMPLES

**createschema -A masteradmin -P secret --csv**

Creates a new schema in the best suited database and outputs the database id and the schema name in csv format.


**createschema -A masteradmin -P secret -i 3**

Creates a new schema in the database with id 3.

# SEE ALSO

[createcontext(1)](createcontext), [createschemas(1)](createschemas), [deleteemptyschema(1)](deleteemptyschema), [checkdatabase(1)](checkdatabase)
