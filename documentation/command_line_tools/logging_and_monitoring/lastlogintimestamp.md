---
title: lastlogintimestamp
icon: far fa-circle
tags: Administration, Command Line tools, Monitoring
---

# NAME

lastlogintimestamp - prints the timestamp of last login for specified user for given client.

# SYNOPSIS

**lastlogintimestamp** [OPTION]...

# DESCRIPTION

This command line tool prints the timestamp of last login for specified user for given client.

# OPTIONS
 
**-c**, **--context** *contextId*
: A valid (numeric) context identifier.

**-u**, **--user** *userId*
: A valid (numeric) user identifier.

**-i** *userId*
: A valid (numeric) user identifier. As alternative for the `-u, --user` option.

**-d**, **--datepattern** *datePattern*
: The optional date pattern used for formatting retrieved timestamp; e.g "EEE, d MMM yyyy HH:mm:ss Z" would yield "Wed, 4 Jul 2001 12:08:56 -0700".

**-l**, **--list-clients**
: Outputs a table of known client identifiers

**-t**, **--client** *clientId*
: A client identifier; e.g "open-xchange-appsuite" for App Suite UI. Execute `lastlogintimestamp --listclients` to get a listing of known identifiers.

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-s**, **--server** *rmiHost*
: The optional RMI server (default: localhost).

**-p**, **--port** *rmiPort*
: The optional RMI port (default:1099).

**-h**, **--help**
: Prints a help text.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**lastlogintimestamp -c 1 -u 6 -t open-xchange-appsuite**

Prints the last login timestamp for the specified user that logged from the specified client.

**lastlogintimestamp -c 1 -u 6 -t open-xchange-appsuite -d "yyyy.MM.dd G 'at' HH:mm:ss z"**

Prints the last login timestamp for the specified user that logged from the specified client and format the timestamp with the specified pattern.

# SEE ALSO

[logconf(1)](logconf), [includestacktrace(1)](includestacktrace), [logincounter(1)](logincounter)