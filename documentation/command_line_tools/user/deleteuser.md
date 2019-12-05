---
title: deleteuser
icon: far fa-circle
tags: Administration, Command Line tools, User
---

# NAME

deleteuser - deletes users in a given context.

# SYNOPSIS

**deleteuser** [OPTION]...

# DESCRIPTION

This command line tool deletes users in a given context. If you delete a user the public folder entries of this user are transferred to the admin user. All other data are deleted. 

# OPTIONS

**-c**, **--contextid** *contextid*
: The context identifier. Mandatory.

**-i**, *--userid* *userId*
: Id of the user.

**-u**, *--username* *username*
: Username of the user.

**-A**, **--adminuser** *contextAdmin*
: Context admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *contextAdminPassword*
: Context admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**deleteuser -A contextAdmin -P secret -c 1138 -i 137**

Deletes the user with the specified identifier in the specified context.

# SEE ALSO

[createuser(1)](createuser), [listuser(1)](listuser), [changeuser(1)](changeuser), [getusercapabilities(1)](getusercapabilities)