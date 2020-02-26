---
title: closesessions
icon: far fa-circle
tags: Administration, Command Line tools, Session
package: open-xchange-core
---

# NAME

closesessions

# SYNOPSIS

**closesessions** -c *contextId* [-u *userId*] [-g] -A *masterAdmin | contextAdmin* -P *masterAdminPassword |
                     contextAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*] | [-h]                          

# DESCRIPTION

Clears all sessions belonging to a given context and/or user.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier.

**-u**,**--user** *userId*
: A valid user identifier

 **-g**,**--global**
: Switch instructing the tool to perform a global session clean-up

**-A**, **--adminuser** *admin*
: The master or context admin user name for authentication.

**-P**, **--adminpass** *adminPassword*
: The master admin or context admin password for authentication.

**-h**, **--help**
: Prints a help text

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

