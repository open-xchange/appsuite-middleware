---
title: getcontextcapabilities
icon: far fa-circle
tags: Administration, Command Line tools, Context, Capabilities
---

# NAME

getcontextcapabilities - lists available capabilities for a certain context. 

# SYNOPSIS

**getcontextcapabilities** [OPTION]...

# DESCRIPTION

This command line tool lists available capabilities for a certain context. 

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory and mutually exclusive with `-N`.

**-N**, **--contextname** *contextName*
: The context name. Mandatory and mutually exclusive with `-c`.

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**getcontextcapabilities -A masterAdmin -P secret -c 1138**

Retrieves the capabilities of the context with the specified identifier.

# SEE ALSO

[createcontext(1)](createcontext), [enablecontext(1)](enablecontext), [changecontext(1)](changecontext), [enableallcontexts(1)](enableallcontexts), [disableallcontexts(1)](disableallcontexts), [deletecontext(1)](deletecontext), [disablecontext(1)](disablecontext), [listcontext(1)](listcontext)
