---
title: getadmin
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin-reseller
---

# NAME

getadmin - get admin and sub-admin data.

# SYNOPSIS

**getadmin** [OPTION]...

# DESCRIPTION

This tool can be used to get data of an admin or a sub-admin. A reseller admin can fetch his own data or data from his sub-resellers. The master admin can fetch data for every one.

# OPTIONS

**-i**, **--adminid** *id*
: Numerical identifier of admin account.

**-u**, **--adminname** *adminname*
: Loginname of the admin account.

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Can also be an admin login.

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

**getadmin -A masterAdmin -P masterPassword -u sub-admin-jdoe**

Gets admin data of sub-admin-joe using the master credentials.

**getadmin -A admin-joe -P admin-joe-password -u sub-admin-jdoe**

Gets admin data of sub-admin-joe using admin joe's credentials.

# SEE ALSO

[listadmin(1)](listadmin)
