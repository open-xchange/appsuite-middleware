---
title: registerserver
icon: far fa-circle
tags: Administration, Command Line tools, Server
---

# NAME

registerserver - registers a server

# SYNOPSIS

**registerserver** [-h|--help]

**registerserver** --environment

**registerserver** -A *masterAdminUser* -P *masterAdminPassword* -n *name* [--nonl] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool registers a server.

# OPTIONS

**-n**, **--name** *name*
: The identifier of the new server. This is mandatory.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**registerserver -A masteradmin -P secret -n foobar**

Registers the server with name '`foobar`'.

# SEE ALSO

[listserver(1)](listserver), [changeserver(1)](changeserver), [unregisterserver(1)](unregisterserver)