---
title: restoregabdefaults
icon: far fa-circle
tags: Administration, Command Line tools, Contacts, Global Address Book
---

# NAME

restoregabdefaults

# SYNOPSIS

**restoregabdefaults** -c *contextId* -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*]
                          [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

Restores the default permissions for the global address book (GAB).

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

