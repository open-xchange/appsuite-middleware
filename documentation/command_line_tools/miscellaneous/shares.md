---
title: shares
icon: far fa-circle
tags: Administration, Command Line tools
---

# NAME

shares - delete shares

# SYNOPSIS

**shares** [OPTIONS] | [-h]

# DESCRIPTION

This command line tool lists and deletes shares

# OPTIONS

**-h**, **--help**
: Prints a help text

**-c**, **--context** *contextId*
: The context id.

**-i**, **--userid** *userId*
: The guest user id.

**-T**, **--token** *token*
: Token or URL.

**-r**, **--remove** 
: Remove the token.

**-f**, **--force**
: Force removal of token.

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