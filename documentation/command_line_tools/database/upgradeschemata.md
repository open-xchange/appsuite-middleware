---
title: upgradeschemata
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema
---

# NAME

upgradeschemata - runs the update process on all available schemata.

# SYNOPSIS

**upgradeschemata** [OPTION...]

# DESCRIPTION

This command line tool performs a database upgrade from 7.6.3 to 7.10.x for all (or defined) schemata automatically. In other words it sums up all relevant steps for the upgrade process as described in the migration guide (link TBD), i.e. registers the new 7.10.0 server, and then iterates through all schemata in the installation, disables each one, runs the database updates, changes the server references and re-enables it. 

# OPTIONS

**-f**, **--force**
: Flag to force the upgrade even if the updates fail in some schemata.

**-m**, **--schema-name** *schemaName*
: The optional schema name to continue from.

**-n**, **-server-name** *serverName*
: The name of the server to register and point all upgraded schemata to.

**-k**, **--skip-schemata** *schemataToSkip*
: Defines the names of the schemata as a comma separated list that should be skipped during the upgrade phase.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication.

**-H**, **--host** *jmxHost*
: The optional JMX host (default:localhost)

**-l**, **--login** *jmxLogin*
: The optional JMX login (if JMX authentication is enabled)

**-p**, **--port** *jmxPort*
: The optional JMX port (default:9999)

**-s**, **--password** *jmxPassword*
: The optional JMX password (if JMX authentication is enabled)

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

The trivial case, registers a server with the name 'oxserver-710' and runs all database updates.

**upgradeschemata -A oxadminmaster -P secret -n oxserver-710**

If a server with that name is already registered, then a warning will be displayed indicating that and prompting the administrator to either "`abort`" or "`continue`" with the upgrade operation.

```
WARNING: The specified server is already registered with id '6'.
         If that shouldn't be the case, type 'abort' to abort the upgrade process, otherwise, type 'continue' to proceed.
         If you continue the already existing server will be used to point the updated schemata after the update tasks complete.
```

The command line tool also provides the -f flag to force the continuation of upgrading all schemata (or as many as possible) even if one or more update tasks in one or more schemata fail. If the flag is absent, then as soon as one schema upgrade fails, the command line tool aborts the operation.

**upgradeschemata -A oxadminmaster -P secret -n oxserver-710 -f**

To continue the operation and skip the failed schema, the -m flag can be used. If present, then the upgrade continues from where it left of and by skipping the specified schema.

**upgradeschemata -A oxadminmaster -P secret -n oxserver-710 -f -m oxdatabase_81**

If certain schemata need to be skipped, then the -k flag can be used. It defines a comma separated list of the names of the schemata that are to be skipped from the upgrades.

**upgradeschemata -A oxadminmaster -P secret -n oxserver-710 -k oxdatabase_81,oxdatabase_44,oxdatabase_51**

The flags -m, -k and -f can be combined. In that case it would mean that the upgrade phase will continue even if the updates fail in some schemata, it will start from the specified schema and skip all schemata that are in the list.

**upgradeschemata -A oxadminmaster -P secret -n oxserver-710 -f -m oxdatabase_3 -k oxdatabase_81,oxdatabase_44,oxdatabase_51**


# SEE ALSO

[createschema(1)](createschema), [createschemas(1)](createschema), [listdatabaseschema(1)](listdatabaseschema)
