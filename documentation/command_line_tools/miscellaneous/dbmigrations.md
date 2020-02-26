---
title: dbmigrations
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

dbmigrations

# SYNOPSIS

**dbmigrations** -n <schemaName> [[-f] | [-ll] [-u]] -A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s <RMI-Server>] [--responsetimeout <responseTimeout>] | [-h]

# DESCRIPTION

Command line tool to control database migrations.

# OPTIONS

**-h**, **--help**
: Prints a help text

**-n**, **--name** *schemaName*
: The database schema name to use

**-u**, **--force-unlock**
: Forces a release of all locks

**-ll**, **--list-locks**
: Lists all currently acquired locks

**-r**, **--run**
: Forces a run of the current core changelog

**-A**, **--adminuser** *admin*
: The master or context admin user name for authentication.

**-P**, **--adminpass** *adminPassword*
: The master admin or context admin password for authentication.

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).