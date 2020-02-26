---
title: listadmin
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin-reseller
---

# NAME

listadmin - lists sub-admins.

# SYNOPSIS

**listadmin** [OPTION]...

# DESCRIPTION

Lists sub-admin accounts.

# OPTIONS

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Can also be a sub-admin login in case it has the Subadmin.CanCreateSubadmin restriction enabled.

**-P**, **--adminpass** *adminPassword*
: Admin password for authentication.

**--csv**
: Format output to csv.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**listadmin -A masterAdmin -P masterPassword --csv**

Lists all sub-admin in csv format.

# SEE ALSO

[createadmin(1)](createadmin) [changeadmin(1)](changeadmin) [deleteadmin(1)](deleteadmin)
