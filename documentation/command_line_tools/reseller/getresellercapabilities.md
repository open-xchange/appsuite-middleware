---
title: getresellercapabilities
icon: far fa-circle
tags: Administration, Command Line tools, Reseller, Capabilities
package: open-xchange-admin
---

# NAME

getresellercapabilities - lists available capabilities for a certain reseller. 

# SYNOPSIS

**getresellercapabilities** [OPTION]...

# DESCRIPTION

This command line tool lists available capabilities for a certain reseller admin. 

# OPTIONS

**-i**, *--adminid* *adminId*
: Id of the admin or rather reseller admin.

**-u**, *--adminname* *adminname*
: Name of the admin.

**-A**, **--adminuser** *contextAdmin*
: The admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *contextAdminPassword*
: The admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**getresellercapabilities -A oxAdmin -P secret -c 1138**

Retrieves the capabilities of the reseller admin with the specified identifier.

# SEE ALSO

[createadmin(1)](createadmin)[changeadmin(1)](changeadmin) [deleteadmin(1)](deleteadmin) [listadmin(1)](listadmin)
