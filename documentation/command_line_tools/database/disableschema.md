---
title: disableschema
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema
---

# NAME

disableschema - disables a database schema

# SYNOPSIS

**disableschema** [-h|--help]

**disableschema** -A *masterAdminUser* -P *masterAdminPassword* -m *schemaName* [-r *rmiHosts*] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool disables a database schema

# OPTIONS

**-m**, **--target-schema** *schemaName*
: The name of the schema to disable. Mandatory.

**-r**, **--rmi-hosts** *rmiHosts*
: A list of RMI hosts e.g. 192.168.1.25:1099,192.168.1.26. If no port is given the default RMI port 1099 is used. Default: rmi://localhost:1099/

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**disableschema -A masteradmin -P secret -m foobar**

Disables the schema with the specified name.

# SEE ALSO

[enableschema(1)](enableschema), [dumpschema(1)](dumpschema), [replayschema(1)](replayschema)
