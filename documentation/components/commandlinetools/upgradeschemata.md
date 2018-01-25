---
title: upgradeschemata
---

# Performs a database upgrade from 7.6.3 to 7.10.0

The `upgradeschemata` command line tool performs a database upgrade from 7.6.3 to 7.10.0 for all (or defined) schemata automatically. In other words it sums up all relevant steps for the upgrade process as described in the migration guide (link TBD), i.e. registers the new 7.10.0 server, and then iterates through all schemata in the installation, disables each one, runs the database updates, changes the server references and re-enables it. 

## Parameters

 - `` -n,--server-name <server-name>``<br>
 The mandatory name of the server to register and point all upgraded schemata to
 - `` -H,--host <host>``<br>
 The optional JMX host (default:localhost)
 - ``-p,--port <port>``<br>
 The optional JMX port (default:9999)
 - ``-l,--login <login>``<br>
 The optional JMX login (if JMX has authentication enabled)
 - ``-s,--password <password>``<br>
 The optional JMX password (if JMX has authentication enabled)
 - ``-m,--schema-name <schema-name>``<br>
 The optional schema name to continue from
 - ``-f,--force``<br>
 Forces the upgrade even if the updates fail in some schemata
 - ``-k,--skip-schemata <skip-schemata>``<br>
 Defines the names of the schemata as a comma separated list that should be skipped during the upgrade phase
 - ``-A,--adminuser <adminuser>``<br>
 Admin username
 - ``-P,--adminpass <adminpass>``<br>
 Admin password

### Examples

The trivial case, registers a server with the name 'oxserver-710' and runs all database updates.

```bash
upgradeschemata -A oxadminmaster -P secret -n oxserver-710
```
If a server with that name is already registered, then a warning will be displayed indicating that and prompting the administrator to either `abort` or `continue` with the upgrade operation.

```bash
WARNING: The specified server is already registered with id '6'.
         If that shouldn't be the case, type 'abort' to abort the upgrade process, otherwise, type 'continue' to proceed.
         If you continue the already existing server will be used to point the updated schemata after the update tasks complete.
```

The command line tool also provides the `-f` flag to force the continuation of upgrading all schemata (or as many as possible) even if one or more update tasks in one or more schemata fail. If the flag is absent, then as soon as one schema upgrade fails, the command line tool aborts the operation.

```bash
upgradeschemata -A oxadminmaster -P secret -n oxserver-710 -f
```

To continue the operation and skip the failed schema, the `-m` flag can be used. If present, then the upgrade continues from where it left of and by skipping the specified schema.

```bash
upgradeschemata -A oxadminmaster -P secret -n oxserver-710 -f -m oxdatabase_81
```
If certain schemata need to be skipped, then the `-k` flag can be used. It defines a comma separated list of the names of the schemata that are to be skipped from the upgrades.

```bash
upgradeschemata -A oxadminmaster -P secret -n oxserver-710 -k oxdatabase_81,oxdatabase_44,oxdatabase_51
```

The flags `-m`, `-k` and `-f` can be combined. In that case it would mean that the upgrade phase will continue even if the updates fail in some schemata, it will start from the specified schema and skip all schemata that are in the list.

```bash
upgradeschemata -A oxadminmaster -P secret -n oxserver-710 -f -m oxdatabase_3 -k oxdatabase_81,oxdatabase_44,oxdatabase_51
```