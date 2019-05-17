---
title: replayschema
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema
---

# NAME

replayschema - restores a dumped database schema.

# SYNOPSIS

**replayschema** [-h|--help]

**replayschema** -A *masterAdminUser* -P *masterAdminPassword* -m *schemaName* [-r *rmiHosts*] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool restores a database schema that was dumped with the `dumpschema(1)` command line tool.

# OPTIONS

**-i**, **--in** *dumpFile*
: The dump file previously created with `dumpschema(1)`. Mandatory.

**-m**, **--source-schema** *schemaName*
: he source schema name (i.e. the schema that was dumped before). Mandatory.

**-t**, *-target-cluster-id* *targetClusterId*
: The target database cluster identifier. Mandatory.

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

**replayschema -A masteradmin -P secret -m foobar -i /tmp/database-dump.sql -t 12**

Restores the database schema with the specified name from the specified dump.

# SEE ALSO

[disableschema(1)](disableschema), [dumpschema(1)](dumpschema), [enableschema(1)](enableschema)
