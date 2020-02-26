---
title: unblockdatabase
icon: far fa-circle
tags: Administration, Command Line tools, Database
package: open-xchange-admin
---

# NAME

unblockdatabase - unblocks a database schema

# SYNOPSIS

**unblockdatabase** [-h|--help]

**unblockdatabase** -A *masterAdminUser* -P *masterAdminPassword* [-i *database_id* | -n *name*][--responsetimeout *seconds*]

# DESCRIPTION

This command line tool unblocks specified database schema (in case marked as being updated for too long).

# OPTIONS

**-i**, **--id** *id*
: The identifier of a certain database host. Mutually exclusive with `-n`.

**-n**, **-name** *name*
: The name of a certain database host (as alternative for "id" option). Mutually exclusive with `-n`.

**--schema** *schema*
: The optional schema name of the database.

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

**unblockdatabase -A masteradmin -P secret -i 3 --schema foobar**

Unblocks the schema with the specified name from the database with the specified identifier.

# SEE ALSO

[createschema(1)](createschema), [createschemas(1)](createschema), [listdatabaseschema(1)](listdatabaseschema)
