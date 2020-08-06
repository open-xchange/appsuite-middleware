---
title: runallupdate
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema, Update Task
package: open-xchange-core
---

# NAME

runallupdate

# SYNOPSIS

**runallupdate** [-e] -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*] [--responsetimeout
                    *responseTimeout*] | [-h]

# DESCRIPTION

Runs the update on all schemas.

# OPTIONS

**-e**,**--error**
: The flag indicating whether process is supposed to be stopped if an error occurs when trying to update a schema.

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

