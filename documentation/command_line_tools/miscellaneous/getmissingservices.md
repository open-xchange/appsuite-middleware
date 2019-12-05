---
title: getmissingservices
icon: far fa-circle
tags: Administration, Command Line tools
---

# NAME

getmissingservices

# SYNOPSIS

**getmissingservices** [-n *bundleName*] -A *masterAdmin | contextAdmin* -P *masterAdminPassword |
                          contextAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*] [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

Gets the missing services

# OPTIONS

**-n**,**--name** *bundleName*
: The optional bundle's symbolic name

**-A**, **--adminuser** *masterAdmin*
: The master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: The master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

