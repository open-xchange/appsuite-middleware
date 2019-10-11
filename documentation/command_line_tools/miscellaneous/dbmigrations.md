---
title: deleteorphanedattachments
icon: far fa-circle
tags: Administration, Command Line tools
---

# NAME

deleteorphanedattachments - delete orphaned references

# SYNOPSIS

**deleteorphanedattachments** [OPTIONS] | [-h]

# DESCRIPTION

The command-line tool for deleting orphaned references from mail compose for specified file storage identifiers.

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