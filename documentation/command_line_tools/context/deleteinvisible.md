---
title: deleteinvisible
icon: far fa-circle
tags: Administration, Command Line tools, Context
package: open-xchange-admin
---

# NAME

deleteinvisible - Deletes unreachable data of a contexts.

# SYNOPSIS

**deleteinvisible** [OPTION]...

# DESCRIPTION

This command line tool flush data which is no longer needed due to access permission changes of a context.
E.g. you call this CLT after you downgraded a context by removing the calendar access for all its user. The
calendar data is now no longer reachable for and thus does not make sense to persist. This tool will clean
up the data.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory and mutually exclusive with `-N`.

**-N**, **--contextname** *contextName*
: The context name. Mandatory and mutually exclusive with `-c`.

**-A**, **--adminuser** *masterAdmin*
: Context admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Context admin password for authentication. Optional, depending on your configuration.

**--csv**
: Format output to csv.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**deleteinvisible -A masterAdmin -P secret -c 1138**

Deletes all invisible or rather superfluous data of the context with the specified identifier.

# SEE ALSO

[createcontext(1)](createcontext), [listcontext(1)](listcontext), [changecontext(1)](changecontext), [enablecontext(1)](enablecontext), [disableallcontexts(1)](disableallcontexts), [deletecontext(1)](deletecontext), [enableallcontexts(1)](enableallcontexts), [getcontextcapabilities(1)](getcontextcapabilities)