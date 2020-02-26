---
title: listserver
icon: far fa-circle
tags: Administration, Command Line tools, Server
package: open-xchange-admin
---

# NAME

listserver - lists all registered servers

# SYNOPSIS

**listserver** [-h|--help]

**listserver** --environment

**listserver** -A *masterAdminUser* -P *masterAdminPassword* -m *schemaName* -s *serverId* [--nonl] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool lists all registered servers. Optionally, with the use of '-s' you can search for a server name that matches the specified search pattern.

# OPTIONS

**-s**, **--searchpattern** *searchPattern*
: The optional search pattern.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**--csv**
: Format output to csv

**-h**, **--help**
: Prints a help text

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**listserver -A masteradmin -P secret**

Lists all registered servers.

**listserver -A masteradmin -P secret -s foobar**

Lists all registered servers that match the specified pattern '`foobar`'.

# SEE ALSO

[changeserver(1)](changeserver), [registerserver(1)](registerserver), [unregisterserver(1)](unregisterserver)