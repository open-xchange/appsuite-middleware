---
title: includestacktrace
icon: far fa-circle
tags: Administration, Command Line tools
---

# NAME

includestacktrace - enables/disables the ability to include the server stacktraces in the HTTP API JSON responses.

# SYNOPSIS

**changeaccessglobal** [OPTION]...

# DESCRIPTION

This command line tool enables/disables the ability to include the server stacktraces in the HTTP API JSON responses.

# OPTIONS

**-e**, **--enable**
: Flag to enable to include stack traces in HTTP-API JSON responses.

**-d**, **--disable**
: Flag to disable to include stack traces in HTTP-API JSON responses.

**-c**, **--context** *contextId*
: The context id for which to enable logging.

**-u**, **--user** *userId*
: The user id for which to enable logging.

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

**includestacktrace -A oxadminMaster -P secret -e -c 1138**

Enables the ability to include server stacktraces in the HTTP API JSON response for the specified context.

**includestacktrace -A oxadminMaster -P secret -d -c 1138 -u 137**

Disables the ability to include server stacktraces in the HTTP API JSON response for the specified user in the specified context.

# SEE ALSO

[logconf(1)](logconf)