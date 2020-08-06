---
title: listUpdateTaskNamespaces
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema, Update Task
package: open-xchange-core
---

# NAME

listUpdateTaskNamespaces

# SYNOPSIS

**listUpdateTaskNamespaces** -n -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*]
                                [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

This tools lists all namespaces for any update tasks and/or update task sets. The outcome of this tool can be used to
populate the property 'com.openexchange.groupware.update.excludedUpdateTasks'. Entries in that property will result in
excluding all update tasks that are part  of that particular namespace.

# OPTIONS

**-n**,**--namespaces-only**
: Prints only the available namespaces without their update tasks

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

