---
title: listdatabaseschema
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema
package: open-xchange-admin
---

# NAME

listdatabaseschema - lists all database schemata.

# SYNOPSIS

**listdatabaseschema** [-h|--help]

**listdatabaseschema** [--environment]

**listdatabaseschema** -A *masterAdminUser* -P *masterAdminPassword* [-s *searchPattern*][--responsetimeout *seconds* --nonl --csv]

# DESCRIPTION

This command line tool lists all database schemata with the option to only list empty schemata (`--only-empty-schemas`).

# OPTIONS

**-s**, **--searchpattern** *searchPattern*
: The pattern to search for when listing databases.

**--only-empty-schemas**
: (Optionally) Specifies to list only empty schemas (per database host). If omitted, all empty schemas are considered.

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

**listdatabaseschema -A masteradmin -P secret -s foobar**

Lists all database schemata that their names match the specified search pattern

# SEE ALSO

[createschema(1)](createschema), [createschemas(1)](createschemas), [countdatabaseschema(1)](countdatabaseschema)
