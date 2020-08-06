---
title: restoregabdefaults
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

restoregabdefaults - restores GAB defaults

# SYNOPSIS

**restoregabdefaults** [OPTIONS] | [-h]

# DESCRIPTION

This command line tool restores the default permissions for the global address book (GAB).

# OPTIONS

**-h**, **--help**
: Prints a help text

**-c**, **--context** *contextId*
: The context id.

**-A**, **--adminuser** *admin*
: The master or context admin user name for authentication.

**-P**, **--adminpass** *adminPassword*
: The master admin or context admin password for authentication.

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).