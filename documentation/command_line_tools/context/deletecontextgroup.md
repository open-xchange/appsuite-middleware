---
title: deletecontextgroup
icon: far fa-circle
tags: Administration, Command Line tools, Context
package: open-xchange-admin
---

# NAME

deletecontextgroup - deletes a context group.

# SYNOPSIS

**deletecontextgroup** [OPTION]...

# DESCRIPTION

Command line tool for deleting context groups and all data associated to them. 

# OPTIONS

**-g**, **--context-group-id** *contextGroupId*
: The context group identifier.

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**-p**. **--port** *rmiPort*
:  The optional RMI port (default:1099).

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

**-s**. **--server** *rmiHost*
:  The optional RMI server (default: localhost).

# EXAMPLES

**deletecontextgroup -A masterAdmin -P secret -c 1138**

Deletes the context with the specified identifier.

# SEE ALSO

[createcontext(1)](createcontext), [listcontext(1)](listcontext), [changecontext(1)](changecontext), [enablecontext(1)](enablecontext), [disablecontext(1)](disablecontext), [disableallcontexts(1)](disableallcontexts), [enableallcontexts(1)](enableallcontexts), [getcontextcapabilities(1)](getcontextcapabilities)