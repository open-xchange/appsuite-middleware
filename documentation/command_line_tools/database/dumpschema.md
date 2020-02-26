---
title: dumpschema
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema
package: open-xchange-admin
---

# NAME

dumpschema - dumps a database schema to a file.

# SYNOPSIS

**dumpschema** [-h|--help]

**dumpschema** -A *masterAdminUser* -P *masterAdminPassword* -m *schemaName* -o *dumpFile* [-r *rmiHosts*] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool dumps a database schema to a file.

# OPTIONS

**-o**, **--out** *dumpFile*
: The name of the dump file. Mandatory.

**-m**, **--target-schema** *schemaName*
: The name of the schema to enable. Mandatory.

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

**dumpschema -A masteradmin -P secret -m foobar -t /tmp/foobar-dump.sql**

Dumps the schema with the specified name to the specified file.

# SEE ALSO

[disableschema(1)](disableschema), [enableschema(1)](enableschema), [replayschema(1)](replayschema)
