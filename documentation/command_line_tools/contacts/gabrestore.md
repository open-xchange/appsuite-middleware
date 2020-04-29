---
title: restoregabdefaults
icon: far fa-circle
tags: Administration, Command Line tools, Contacts, Global Address Book
package: open-xchange-core
---

# NAME

restoregabdefaults

# SYNOPSIS

**restoregabdefaults** -c *contextId* -g *gabMode* -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*]
                          [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

Restores the default permissions for the global address book (GAB).

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier.

**-g**, **--gabMode** *gabMode*
: The optional modus the global address book shall operate on. Currently 'global' and 'individual' are known values. If the mode 'global' is chosen, the special "all users and groups" permission will grant access to the global address book for users. If the mode 'individual' is chosen, each user will have a dedicated permission for the global address book. 'individual' is the default. Please keep in mind that the modus will affect the response for folder requests regarding the global address book.

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

