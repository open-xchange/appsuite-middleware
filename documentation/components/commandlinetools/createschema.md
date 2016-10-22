---
title: createschema
---

# Creates a new database schema

This command-line-tool allows to create additional database schemata which can be used during the creation of contexts.

It's either possible to determine the database where the schema is created yourself or to let the middleware automatically decide where to create the newly schema.

Please notice that the created schemata will **not** be available for the automatic schema select strategy. Instead use the "--schema-name" parameter for context creation. 

## Parameters

 - ``-A,--adminuser <arg>``<br>
 Master admin user name for authentication
 - ``-P,--adminpass <arg>``<br>
 Master admin password for authentication
 - ``--responsetimeout <arg>``<br>
 The optional response timeout in seconds when reading data from server (default: 0s; infinite)
 - ``-i,--id <arg>``<br>
 An optional database id
 - ``--csv``<br>
 Format output to csv

### Examples

```
./createSchema -A masteradmin -P secret --csv
```
Creates a new schema in the best suited database and outputs the database id and the schema name in csv format.

```
./createSchema -A masteradmin -P secret -i 3
```
Creates a new schema in the database with id 3.  
