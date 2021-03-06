---
title: forceupdatetask
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema, Update Task
package: open-xchange-core
---

# NAME

forceupdatetask

# SYNOPSIS

**forceupdatetask** -t *taskName* [-c *contextId* | -n *schemaName*] -A *masterAdmin* -P *masterAdminPassword* [-p
                       *RMI-Port*] [-s *RMI-Server*] [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

Force (re-)run of update task denoted by given class name on a specific schema or on all schemata or on a specific context.

# OPTIONS

**-t**,**--task** *taskName* 
: The update task's class name

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

