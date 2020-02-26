---
title: socketLogging
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

socketLogging - Socket Logger Management Tool

# SYNOPSIS

**socketLogging** [-h|--help]

**socketLogging** [OPTIONS]

# DESCRIPTION

This command line tool helps to manage the socket logger

# OPTIONS

**-h**,**--help**
: Prints the help text 

**-r**, **--register** 
: Registers a logger for socket logging

**-u**, **--unregister**
: Unregisters a logger from socket logging 

**-n**, **--name** *loggerName*
: The logger name to register/unregister for/from socket logging

**-lr**, **--list-registered**
: Lists all registered socket loggers

**-lb**, **--list-blacklisted**
: Lists all blacklisted socket loggers

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