---
title: changedatabase
icon: far fa-circle
tags: Administration, Command Line tools, Database
package: open-xchange-admin
---

# NAME

changedatabase - changes the configuration of an already registered database.

# SYNOPSIS

**changedatabase** [OPTION]...

# DESCRIPTION

This command line tool changes the configuration of an already registered database such as the default pooling options can be overriden (`-l -o 10 -a 200`), the maximum number of contexts that the registered database can hold (`-x 100`), the database user and password as well as the driver.

# OPTIONS

**-i**, **--iu** *id*
: The id of the database. Mutually exclusive with '`-n`'.

**-n**, **--name** *name*
: Name of the database. Mutually exclusive with '`-i`'.

**-H**, **--hostname** *hostname*
: Hostname of the server.

**-u**, **--dbuser** *dbUser*
: The name of the database user, i.e. the user on behalf of which the middleware will perform all SQL queries.

**-p**, **--dbpasswd** *dbPassword*
: The password of the database user. This option is mandatory.

**-d**, **--dbdriver** *dbDriver*
: The driver to be used for the database. Default: com.mysql.jdbc.Driver.

**-x**, **--maxunit** *maxunit*
: The maximum number of contexts in this database. If this option is omitted then the default of 1000 is applied.

**-l**, **--poolhardlimit** *true/false*
: Database pool hard limit. Default: true.

**-o**, **--poolinitial** *poolInitial*
: Database pool initial. Default: 0.

**-a**, **--poolmax** *poolMax*
: Database pool max. Default: 100.

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

**changedatabase -A oxadminmaster -P admin_master_password -n oxdatabase -p db_password -x 1000 -i 4**

Changes maximum number of contexts in the database with the specified id.

**changedatabase -A oxadminmaster -P secret --name foobar --dbuser openexchange --dbpasswd secret**

Changes the username and password of the database with the specified name.

# SEE ALSO

[unregisterdatabase(1)](unregisterdatabase), [registerdatabase(1)](registerdatabase), [listdatabase(1)](listdatabase)

