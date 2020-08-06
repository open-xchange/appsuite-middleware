---
title: runupdate
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema, Update Task
package: open-xchange-core
---

# NAME

runupdate

# SYNOPSIS

**runupdate** [-c *contextId* | -n *schemaName*] -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s
                 *RMI-Server*] [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

Runs the schema's update.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier.

**-n**,**--name** *arg*
: A valid schema name. This option is a substitute for '-c/--context' option. If both are present '-c/--context' is preferred.

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

