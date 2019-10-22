---
title: deleteadmin
icon: far fa-circle
tags: Administration, Command Line tools
---

# NAME

deleteadmin - removes a sub-admin.

# SYNOPSIS

**deleteadmin** [OPTION]...

# DESCRIPTION

Remove sub-admin accounts.

# OPTIONS

**-i**, **--adminid** *id*
: Numerical identifier of sub-admin account.

**-u**, **--adminname** *adminname*
: Loginname of the sub-admin account.

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Can also be a sub-admin login in case it has the Subadmin.CanCreateSubadmin restriction enabled.

**-P**, **--adminpass** *adminPassword*
: Admin password for authentication.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**deleteadmin -A masterAdmin -P masterPassword -u sub-admin-jdoe**

Deletes sub-admin account with name sub-admin-jdoe.

# SEE ALSO

[createadmin(1)](createadmin) [changeadmin(1)](changeadmin) [listadmin(1)](listadmin)
