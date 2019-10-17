---
title: initconfigdb
icon: far fa-circle
tags: Administration, Command Line tools, Database
---

# NAME

initconfigdb - Initializes the configDB.

# SYNOPSIS

**initconfigdb** [OPTION]...

# DESCRIPTION

This command line tool initializes the configDB. 

# OPTIONS

**--configdb-user** *configDbUser*
: The user name of the configDB user for authentication.

**--configdb-pass** *configDbPassword*
: The configDB users password for authentication.

**--configdb-host** *host*
: The host the DB system runs on.

**-p**, **--configdb-port** *port*
: The port of the database.

**--configdb-dbname** *dbName*
: The name of the configDB

**--mysql-root-user** *rootUser*
: The SQL root user name for authentication.

**--mysql-root-passwd** *password*
: The password of the SQL root user for authentication.

**--addon-sql** *fileNames*
: Comma separated list of additional files that contains SQL statements. Files should be under /opt/open-xchange/sbin/mysql 

**-a**
: To create SQL admin user using GRANT command.

**-i**
: To automatically delete the configDB if exists

# EXAMPLES

**initconfigdb --configdb-user=openexchange --configdb-pass=secret --configdb-host=10.20.30.217**

# SEE ALSO

[unregisterdatabase(1)](unregisterdatabase), [registerdatabase(1)](registerdatabase), [listdatabase(1)](listdatabase)

