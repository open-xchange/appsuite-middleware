---
title: listdatabase
icon: far fa-circle
tags: Administration, Command Line tools, Database
package: open-xchange-admin
---

# NAME

listdatabase - lists all registered databases.

# SYNOPSIS

**listdatabase** [-h|--help]

**listdatabase** [--environment]

**listdatabase** -A *masterAdminUser* -P *masterAdminPassword* [-s *searchPattern*][--responsetimeout *seconds* --nonl --csv]

# DESCRIPTION

This command line tool lists all registered databases.

# OPTIONS

**-s**, **--searchpattern** *searchPattern*
: The pattern to search for when listing databases.

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

**listdatabase -A masteradmin -P secret -s foobar**

Lists all databases that their names match the specified search pattern

# SEE ALSO

[registerdatabase(1)](registerdatabase), [unregisterdatabase(1)](unregisterdatabase), [changedatabase(1)](changedatabase)
