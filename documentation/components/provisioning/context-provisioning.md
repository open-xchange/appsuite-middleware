---
title: Context provisioning
---

This article outlines how context provisioning has been improved starting with v7.10. It describes the preconditions, implemented ideas and changes as well as modified and newly introduced command-line tools.

# Motivation
Considering our recent standard installations, the number of users/contexts held in our database leaves the dimension of several thousands and enters the range of several millions. 
The current context provisioning performance simply does not meet the requirements to:

* Quickly create, update and delete contexts in such large data sets
* Bulk create contexts on prepared (pre-created) database schemas

Those drawbacks are mainly driven by a strict automated schema management as well as auto-selection of suitable file storages. 
This forces usage of heavy locking to prevent concurrency issues and executing expensive COUNT queries, which slows down performance and stresses the CPU usage of the MySQL service drastically. 
Therefore, the context create and delete mechanism have been revised, trying to resolve the afore-mentioned drawbacks.

# Preconditions
* The property `"CREATE_CONTEXT_USE_UNIT"` is required to be set to `"context"`. Because of this, the property has been dropped from '/opt/open-xchange/etc/hosting.properties' file
* There is no more the possibility to specify a cluster weight for a registered database. Thus the assumed weight is always 100% for each database. Therefore, cluster weight has been completely removed from existing provisioning APIs
* The "inmemory" bulk creation mode for contexts is no more supported, since the reworked context provisioning renders that option obsolete

# Introduction of count tables
The biggest change is the usage of count-tables that were introduced by Carsten Höger and Dennis Sieben in a proof of concept implementation and their main goal is a quick detection of the next suitable file storage and 
database (schema) for a new context as a quick look-up of a file storage's or database's occupancy is provided. This avoids the need to execute slow/stressing `COUNT` queries. Those tables track:

* Number of contexts per file storage
* Number of contexts per database host
* Number of contexts per database schema

To ensure those count tables do reflect a consistent state, the `checkcountsconsistency` tool is available and once invoked takes care that entries and counters do hold the right values:

* Create missing entries
* Drop non-existent entries
* Update counter if inconsistent.

```
checkcountsconsistency 
 -h,--help                                         Prints a help text          
    --environment                                  Show info about commandline environment
    --nonl                                         Remove all newlines (\n) from output
    --responsetimeout <responsetimeout>            response timeout in seconds for reading response from the backend (default 0s; infinite)
 -A,--adminuser <adminuser>                      ? master Admin user name      
 -P,--adminpass <adminpass>                      ? master Admin password
```

# Less lock contention and lenient schema management
This point cares about trying to eliminate locking as well as big, long-lasting transactions where possible. For selecting the next suitable file storage and/or database (schema), the candidate's counter held in appropriate count table 
is atomically incremented acting as a "reservation" for the context, which is supposed to be created. That allows to remove locking for the task of file storage / database selection, but still guarantees concurrency. 
Moreover, those atomic compare-and-set operations do not need to be part of a transaction, lowering overall transaction scope and duration.

As next step, context provisioning gets rid off strict schema management. While during context creation, the auto-creation of a schema (if no suitable one available) is still in place, the context delete operation 
no more takes care of possibly dropping a schema once the last context in it has been removed. This allows to eliminate the database-backed locking through `SELECT...FOR UPDATE` statements.

Additionally, the task to auto-select next suitable database schema has been heavily optimized, rendering the `in-memory` schema select strategy obsolete. 
For that reason, the `in-memory` option has been removed as it no more provides significant performance gains and allows to get rid off the "please use only one provisioning node" burden.

# Pre-creation of schemas
As mentioned in previous section, the schema selection is strongly optimized as considering the "Number of contexts per database schema" count table allows a quick detection of a suitable database schema. 
Meaning, the more schemas are available, the faster (in terms of concurrent) contexts can be created. To further leverage from that optimization, tooling has been introduced to pre-create database schemas 
either for a newly registered database or for an existing one.

Thus, `registerdatabase` utility, is enhanced by `create-userdb-schemas` and `userdb-schema-count` options. The `create-userdb-schemas` option is a flag that signals whether database schemas are supposed to be pre-created. 
`userdb-schema-count` optionally specifies the number of database schemas that shall be pre-created. If missing, number of schemas is calculated by `maxunit` divided by `CONTEXTS_PER_SCHEMA` config option 
from '/opt/open-xchange/etc/hosting.properties' file.

```
registerdatabase 
 -h,--help                                         Prints a help text          
    --environment                                  Show info about commandline environment
    --nonl                                         Remove all newlines (\n) from output
    --responsetimeout <responsetimeout>            response timeout in seconds for reading response from the backend (default 0s; infinite)
 -A,--adminuser <adminuser>                      ? Admin username              
 -P,--adminpass <adminpass>                      ? Admin password              
 -n,--name <name>                                * Name of the database        
 -H,--hostname <hostname>                          Hostname of the server      
 -u,--dbuser <dbuser>                              Name of the user for the database
 -d,--dbdriver <dbdriver>                          The driver to be used for the database. Default: com.mysql.jdbc.Driver
 -p,--dbpasswd <dbpasswd>                        * Password for the database   
 -m,--master <true/false>                        * Set this if the registered database is the master
 -M,--masterid <masterid>                          If this database isn't the master give the id of the master here
 -x,--maxunit <maxunit>                            The maximum number of contexts in this database.. Default: 1000
 -l,--poolhardlimit <true/false>                   Db pool hardlimit. Default: true
 -o,--poolinitial <poolinitial>                    Db pool initial. Default: 0 
 -a,--poolmax <poolmax>                            Db pool max. Default: 100   
    --create-userdb-schemas                        A flag that signals whether userdb schemas are supposed to be pre-created. Accepts: true/false
    --userdb-schema-count <userdb-schema-count>    (Optionally) Specifies the number of userdb schemas that are supposed to be pre-created.
                                                   If missing, number of schemas is calculated by "maxunit" divided by CONTEXTS_PER_SCHEMA config option from hosting.properties
```

To also pre-create schemas for already registered database hosts, the `createschemas` utility is available:

```
createschemas 
 -h,--help                                         Prints a help text          
    --environment                                  Show info about commandline environment
    --nonl                                         Remove all newlines (\n) from output
    --responsetimeout <responsetimeout>            response timeout in seconds for reading response from the backend (default 0s; infinite)
 -A,--adminuser <adminuser>                      ? Admin username              
 -P,--adminpass <adminpass>                      ? Admin password              
 -i,--id <id>                                    | The id of the database.     
 -n,--name <name>                                | Name of the database        
    --userdb-schema-count <userdb-schema-count>    (Optionally) Specifies the number of userdb schemas that are supposed to be pre-created.
                                                   If missing, number of schemas is calculated by "maxunit" divided by CONTEXTS_PER_SCHEMA config option from hosting.properties
```
# Deletion of empty schemas and further schema tooling
As mentioned previously, no auto-deletion of empty schemas is performed and thus dropping empty schemas now becomes a manual task. To determine the schemas and actually deleting them, several tools are introduced.

`listdatabaseschema` outputs a listing of database schemas and optionally accepts a search pattern similar to the `listdatabase` command-line tool. 
Moreover, the `only-empty-schemas` argument allows to list only empty schemas, which might be candidates for deletion.

```
listdatabaseschema 
 -h,--help                                         Prints a help text          
    --environment                                  Show info about commandline environment
    --nonl                                         Remove all newlines (\n) from output
    --responsetimeout <responsetimeout>            response timeout in seconds for reading response from the backend (default 0s; infinite)
 -A,--adminuser <adminuser>                      ? Admin username              
 -P,--adminpass <adminpass>                      ? Admin password              
 -s,--searchpattern <searchpattern>                Search/List pattern!        
    --csv                                          Format output to csv        
    --only-empty-schemas                           (Optionally) Specifies to list only empty schemas (per database host). If missing, all empty schemas are considered
```

In addition to `listdatabaseschema` the `countdatabaseschema` tool outputs the number of schemas per database host, and also allows to only consider empty schemas through `only-empty-schemas` argument. 

```
countdatabaseschema 
 -h,--help                                         Prints a help text          
    --environment                                  Show info about commandline environment
    --nonl                                         Remove all newlines (\n) from output
    --responsetimeout <responsetimeout>            response timeout in seconds for reading response from the backend (default 0s; infinite)
 -A,--adminuser <adminuser>                      ? Admin username              
 -P,--adminpass <adminpass>                      ? Admin password              
 -s,--searchpattern <searchpattern>                Search/List pattern!        
    --csv                                          Format output to csv        
    --only-empty-schemas                           (Optionally) Specifies to list only empty schemas (per database host). If missing, all empty schemas are considered
```

Those tools are supposed to help detecting which (or how many) empty schemas should be deleted from what database host. To finally delete empty schemas, the `deleteemptyschema` tool should be used. That tool can be used to:

* Delete a certain empty schema from a given database host
* Delete empty schemas from a certain database host, while optionally maintaining a given number of empty schemas
* Delete empty schemas from all database hosts, while optionally maintaining a given number of empty schemas (per database host)

```
deleteemptyschema 
 -h,--help                                         Prints a help text          
    --environment                                  Show info about commandline environment
    --nonl                                         Remove all newlines (\n) from output
    --responsetimeout <responsetimeout>            response timeout in seconds for reading response from the backend (default 0s; infinite)
 -A,--adminuser <adminuser>                      ? Admin username              
 -P,--adminpass <adminpass>                      ? Admin password              
 -i,--id <id>                                      The optional ID of a certain database host. If missing all database hosts are considered
 -n,--name <name>                                  The optional name of a certain database host (as alternative for "id" option). If missing all database hosts are considered
    --schema <schema>                              The optional schema name of the database.
    --schemas-to-keep <schemas-to-keep>            (Optionally) Specifies the number of empty schemas that are supposed to be kept (per database host).
                                                   If missing, all empty schemas are attempted to be deleted. Ineffective if "schema" option is specified
```
