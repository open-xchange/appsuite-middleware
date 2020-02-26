---
title: changeserver
icon: far fa-circle
tags: Administration, Command Line tools, Server, Schema
package: open-xchange-admin
---

# NAME

changeserver - changes the server of the specified database schema

# SYNOPSIS

**changeserver** [-h|--help]

**changeserver** --environment

**changeserver** -A *masterAdminUser* -P *masterAdminPassword* -m *schemaName* -s *serverId* [--nonl] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool changes the server of the specified database schema.

# OPTIONS

**-m**, **--schema-name** *schemaName*
: The name of the schema for which to change the server id. This is mandatory.

**-s**, **--server-id** *serverId*
: The identifier of the new server. This is mandatory.

**-A**, **--adminuser** *masterAdminUser*
:   Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
:   Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**--environment**
:   Show info about commandline environment.

**--nonl**
:   Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**changeserver -A masteradmin -P secret -m foobar -s 13**

Changes changes the server of schema '`foobar`' to '`13`'.

# SEE ALSO

[listserver(1)](listserver), [registerserver(1)](registerserver), [unregisterserver(1)](unregisterserver)