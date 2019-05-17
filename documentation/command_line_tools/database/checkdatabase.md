---
title: checkdatabase
icon: far fa-circle
tags: Administration, Command Line tools, Database
---

# NAME

checkdatabase - checks the consistency of the database schemata.

# SYNOPSIS

**checkdatabase** [OPTION]...

# DESCRIPTION

This command line tool checks whether the database schemata are up-to-date, whether there any databases need updating, currently updating or outdated updating.

# OPTIONS

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

**checkdatabase -A oxadminmaster -P admin_master_password**

Checks all registered schemata.

# SEE ALSO

[unregisterdatabase(1)](unregisterdatabase), [registerdatabase(1)](registerdatabase), [listdatabase(1)](listdatabase)

