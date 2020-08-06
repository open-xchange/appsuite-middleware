---
title: createadmin
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin-reseller
---

# NAME

createadmin - creates a sub-admin.

# SYNOPSIS

**createadmin** [OPTION]...

# DESCRIPTION

This tool creates a sub-admin. sub-admins are oxadminmaster accounts with restricted rights:

* they can only manage contexts (no database, filestore, etc)
* they can only manage their own contexts (list/change/delete)
* they might be able to create further sub-admins (see below)

# OPTIONS

**-u**, **--adminname** *adminname*
: Loginname for the new sub-admin account.

**-d**, **--displayname** *displayName*
: Displayname for the new sub-admin account.

**-p**, **--password** *password*
: Password for the new sub-admin account.

**-m**, **--passwordmech** *(CRYPT/SHA/BCRYPT)*
: Password mechanism to use, one of CRYPT, SHA or BCRYPT.

**-a**, **--addrestriction** *restrictionname*
: Restriction to add (can be specified multiple times).

Available default restrictions:

```Text
Subadmin.MaxOverallUser
  - the maximum number of users a subadmin can create distributed over all it's
    contexts

Subadmin.MaxContext
  - the maximum number of contexts a subadmin can create

Context.MaxUser
  - the maximum number of users in a single context a contextadmin can create
    Note: this is a restriction, a subadmin can apply to each context

Subadmin.MaxOverallContextQuota
  - the maximum number of quota distributed over all contexts a subadmin
    can use

Subadmin.CanCreateSubadmin
  - Should this subadmin be able to create subsubadmins? This is NOT allowed by default.
    There's also only one additional level. Note: A subsubadmin cannot create any further
    subadmins.

Subadmin.MaxSubadmin
  - If a subadmin is allowed to create subsubadmins, should there be a maximum?
```

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

**createadmin -A masterAdmin -P masterPassword -u sub-admin-jdoe -d "Sub-admin for Jane Doe" -p secret**

Creates a new sub-admin.

**createadmin -A oxadminmaster -P secret -u sub-admin-jdoe -d "Sub-admin for Jane Doe" -p secret -a Subadmin.MaxContext=2000 -a Subadmin.MaxOverallUser=2100 -a Subadmin.MaxOverallUserByModuleaccess_webmail_plus=2010**

Creates a new sub-admin with various restrictions defined.

**createadmin -A masterAdmin -P masterPassword -u sub-admin-jdoe -d "Sub-admin for Jane Doe" -p secret -a Subadmin.CanCreateSubadmin=true**

Creates a new sub-admin with the ability to create sub-sub-admin accounts.

# SEE ALSO

[changeadmin(1)](changeadmin) [deleteadmin(1)](deleteadmin) [listadmin(1)](listadmin)
