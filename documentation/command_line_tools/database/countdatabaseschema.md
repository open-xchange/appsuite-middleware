---
title: countdatabaseschema
icon: far fa-circle
tags: Administration, Command Line tools, Database
---

# NAME

countdatabaseschema - counts the schemata per database host.

# SYNOPSIS

**countdatabaseschema** [OPTION]...

# DESCRIPTION

This command line tool counts the database schemata per database host. An optional search pattern can be defined (`-s`) and count only schemata that match that pattern. Optionally, only the empty schemata can be counted (`--only-empty-schemas`).

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

**countdatabaseschema -A oxadminmaster -P admin_master_password -s foobar**

Counts all database schmeata that match the specified pattern.

# SEE ALSO

[createschema(1)](createschema), [createschemas(1)](createschemas),[deleteemptyschema(1)](deleteemptyschema), [listdatabaseschema(1)](listdatabaseschema)

