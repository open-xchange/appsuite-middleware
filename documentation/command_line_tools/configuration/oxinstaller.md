---
title: oxinstaller
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin
---

# NAME

oxinstaller - setup the configuration.

# SYNOPSIS

**oxinstaller** [OPTION]...

# DESCRIPTION

This command line tool setup the he Open-Xchange Server configuration.

# OPTIONS

**--servername** *serverName*
: The name of the server

**--imapserver** *address*
: The address of the IMAP server

**--smtpserver** *address*
: The address of the SMTP server

**--mail-login-src** *source*
: The login source for primary mail/transport account

**--mail-server-src** *source*
: The mail server source for primary mail account

**--transport-server-src** *source*
: The primary transport server

**--servermemory** *memory*
: The server memory

**--clt-memory** *memory*
: The CLT memory

**--tmpdir-path** *path*
: The path of the tmp directory
 
**--jkroute** *route*
: The route name of this node

**--maxSession** *number*
: The number of maximum sessions

**--sessionDefLifeTime** *time*
: The time of sessions life time

**--add-license** *key*
: The license key

**--no-license**
: Signals that no special license is used.
 
**--configdb-user** *user*
: The configDB user

**--configdb-pass** *password*
: The configDB password

**--configdb-readhost** *address*
: The address of the configDB read access

**--configdb-writehost** *address*
: The address of the configDB write access

**--configdb-readport** *port*
: The port of the configDB read access

**--configdb-writeport** *port*
: The port of the configDB write access
 
**--configdb-dbname** *name*
: The name of the configDB

**--master-pass** *password*
: The master admin password

**--master-user** *user*
: The name of the admin master user

<!---
**--disableauth** 
: 
**--extras-link**
: 
**--object-link-hostname**
: 
-->

**--name-of-oxcluster** *name*
: The name this node shall have in the cluster

**--network-listener-host** *host*
: The host for the connector's (ajp, http) network listener

**--ajp-bind-port** *port*
: The AJP bind port  

**-h**, **--help**
: Prints the help text

**-D**
: Activates debug mode


# EXAMPLE

**oxinstaller --servername=oxserver --configdb-readhost=10.20.30.219 --configdb-writehost=10.20.30.217 --configdb-user=openexchange --master-pass=secret --configdb-pass=secret --jkroute=OX1 --ajp-bind-port=* **
