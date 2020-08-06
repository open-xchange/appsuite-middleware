---
title: getuserconfigurationsource
icon: fa-book
tags: Administration, Command Line tools, User, Capabilities
package: open-xchange-admin
---

# NAME

getuserconfigurationsource - lists available capabilities for a certain user. 

# SYNOPSIS

**getuserconfigurationsource** [OPTION]...

# DESCRIPTION

This command line tool the configuration source for a certain user. 

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

**-a**,**--user-capabilities** *userCapabilities*
: Outputs the capabilities associated with the given user.

**-i**,**--userid** *userId*
: A valid user identifier.

**-o**,**--user-configuration** *userConfiguration*
: Outputs the configuration associated with the given user. Filter by providing a pattern of the property.

# EXAMPLES

**getuserconfigurationsource -A contextAdmin -P secret -c 1138 -i 42**

Retrieves the configuration source of the user with the specified identifier.

# SEE ALSO

[createuser(1)](createuser), [deleteuser(1)](deleteuser), [changeuser(1)](changeuser), [listuser(1)](listuser)
