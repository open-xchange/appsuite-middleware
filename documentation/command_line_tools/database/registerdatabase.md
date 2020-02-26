---
title: registerdatabase
icon: far fa-circle
tags: Administration, Command Line tools, Database
package: open-xchange-admin
---

# NAME

registerdatabase - registers a database.

# SYNOPSIS

**registerdatabase** [OPTION]...

# DESCRIPTION

This command line tool registers a database. Both master (`-m`) and slave (`-m false -M <masterId>`) databases can be registered. Optionally user database schemata can pre-provisioned (`--create-userdb-schemas --userdb-schema-count <count>`) and the default pooling options can be overriden (`-l -o 10 -a 200`). Furthermore, the maximum number of contexts that the registered database can hold could also be set (`-x 100`).

# OPTIONS

**-n**, **--name** *name*
: Name of the database. This option is mandatory.

**-H**, **--hostname** *hostname*
: Hostname of the server.

**-u**, **--dbuser** *dbUser*
: The name of the database user, i.e. the user on behalf of which the middleware will perform all SQL queries.

**-p**, **--dbpasswd** *dbPassword*
: The password of the database user. This option is mandatory.

**-d**, **--dbdriver** *dbDriver*
: The driver to be used for the database. Default: com.mysql.jdbc.Driver.

**-m**, **--master** *true/false*
: Set this if the registered database is the master. This option is mandatory.

**-M**, **--masterid** *masterId*
: If this database isn't the master, then this option provides the id of the master database.

**-x**, **--maxunit** *maxunit*
: The maximum number of contexts in this database. If this option is omitted then the default of 1000 is applied.

**-l**, **--poolhardlimit** *true/false*
: Database pool hard limit. Default: true.

**-o**, **--poolinitial** *poolInitial*
: Database pool initial. Default: 0.

**-a**, **--poolmax** *poolMax*
: Database pool max. Default: 100.

**--create-userdb-schemas**
: A flag that signals whether userdb schemas are supposed to be pre-created.

**--userdb-schema-count** *userdb-schema-count*
: (Optionally) Specifies the number of userdb schemas that are supposed to be pre-created. If missing, number of schemas is calculated by "maxunit" divided by CONTEXTS_PER_SCHEMA config option from '`hosting.properties`'.

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

**registerdatabase -A oxadminmaster -P admin_master_password -n oxdatabase -p db_password -m true**

Registers a new master database with the specified database user.

**registerdatabase -A oxadminmaster -P secret --name oxdatabase_slave --hostname 1.2.3.4 --dbuser openexchange --dbpasswd secret --master false --masterid=4**

Registers a new slave database.

# SEE ALSO

[unregisterdatabase(1)](unregisterdatabase), [listdatabase(1)](listdatabase), [changedatabase(1)](changedatabase)

