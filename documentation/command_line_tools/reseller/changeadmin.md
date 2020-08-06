---
title: changeadmin
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin-reseller
---

# NAME

changeadmin - change sub-admin parameters.

# SYNOPSIS

**changeadmin** [OPTION]...

# DESCRIPTION

This tool can be used to apply changes to sub-admin accounts.

# OPTIONS

**-i**, **--adminid** *id*
: Numerical identifier of sub-admin account.

**-u**, **--adminname** *adminname*
: Loginname of the sub-admin account.

**-d**, **--displayname** *displayName*
: Displayname for the new sub-admin account.

**-p**, **--password** *password*
: Password for the new sub-admin account.

**-m**, **--passwordmech** *(CRYPT/SHA/BCRYPT)*
: Password mechanism to use, one of CRYPT, SHA or BCRYPT.

**-a**, **--addrestriction** *restrictionname*
: Restriction to add (can be specified multiple times).

See [createadmin(1)](createadmin#options) for available default restrictions.

**-e**, **--editrestriction** *restrictionname*
: Restriction to edit (can be specified multiple times).

See [createadmin(1)](createadmin#options) for available default restrictions.

**-r**, **--removerestriction** *restrictionname*
: Restriction to remove (can be specified multiple times).

**-n**, **--parentid** *id*
: Identifier of parent sub-admin.

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

**changeadmin -A masterAdmin -P masterPassword -u sub-admin-jdoe -d "Sub-admin for John Doe"**

Changes displayname of sub-admin with login sub-admin-jdoe.

**changeadmin -A masterAdmin -P masterPassword -u sub-admin-jdoe -a Subadmin.CanCreateSubadmin=true**

Enables restriction Subadmin.CanCreateSubadmin for sub-admin with login sub-admin-jdoe.

# SEE ALSO

[createadmin(1)](createadmin) [deleteadmin(1)](deleteadmin) [listadmin(1)](listadmin)
