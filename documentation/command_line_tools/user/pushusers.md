---
title: pushusers
icon: far fa-circle
tags: Administration, Command Line tools, User
package: open-xchange-core
---

# NAME

pushusers

# SYNOPSIS

**pushusers** [-l | [ -r -c *contextId* -u *userId* -i *client*] ] -A *masterAdmin* -P *masterAdminPassword* [-p
                 <RMI-Port>] [-s *RMI-Server*] [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

Command-line tool for unregistering and listing push users and client registrations

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier.

**-u**,**--user** *userId*
: A valid user identifier

**-r**,**--unregister**
: Flag to unregister a push user

**-l**,**--list-client-registrations**
: Flag to list client registrations

**-i**,**--client** *clientId*
: The client identifier

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

