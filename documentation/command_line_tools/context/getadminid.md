---
title: getadminid
icon: far fa-circle
tags: Administration, Command Line tools, Context
package: open-xchange-admin
---

# NAME

getadminid - returns the administrator's identifier for a context.

# SYNOPSIS

**getadminid** [OPTION]...

# DESCRIPTION

This command line tool returns the administrator's identifier for a context.

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

**getadminid -A masterAdmin -P secret -c 1138**

Returns the administrator's identifier for the specified context.
