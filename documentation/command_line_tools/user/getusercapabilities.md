---
title: getusercapabilities
icon: far fa-circle
tags: Administration, Command Line tools, User, Capabilities
package: open-xchange-admin
---

# NAME

getusercapabilities - lists available capabilities for a certain user. 

# SYNOPSIS

**getusercapabilities** [OPTION]...

# DESCRIPTION

This command line tool lists available capabilities for a certain user. 

# OPTIONS

**-c**, **--contextid** *contextId*
: The user identifier. Mandatory.

**-i**, *--userid* *userId*
: Id of the user.

**-u**, *--username* *username*
: Username ofthe user.

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

**getusercapabilities -A contextAdmin -P secret -c 1138**

Retrieves the capabilities of the user with the specified identifier.

# SEE ALSO

[createuser(1)](createuser), [deleteuser(1)](deleteuser), [changeuser(1)](changeuser), [listuser(1)](listuser)
