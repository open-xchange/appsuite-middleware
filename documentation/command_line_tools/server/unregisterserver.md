---
title: unregisterserver
icon: far fa-circle
tags: Administration, Command Line tools, Server
---

# NAME

unregisterserver - lists all registered servers

# SYNOPSIS

**unregisterserver** [-h|--help]

**unregisterserver** --environment

**unregisterserver** -A *masterAdminUser* -P *masterAdminPassword* (-i *serverId* | -n *serverName*) [--nonl] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool unregisters servers by their id or name.

# OPTIONS

**-i**, **--id** *serverId*
: The server identifier. Mutually exclusive with '`-n`'.

**-n**, **--name** *serverName*
: The server name. Mutually exclusive with '`-i`'.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**unregisterserver -A masteradmin -P secret -i 13**

Unregisters the server with the identifier '`13`'.

**unregisterserver -A masteradmin -P secret -n foobar**

Unregisters the server with the name '`foobar`'.

# SEE ALSO

[listserver(1)](listserver), [changeserver(1)](changeserver), [registerserver(1)](registerserver)