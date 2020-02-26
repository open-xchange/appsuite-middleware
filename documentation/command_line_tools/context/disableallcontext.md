---
title: disableallcontexts
icon: far fa-circle
tags: Administration, Command Line tools, Context
package: open-xchange-admin
---

# NAME

disableallcontexts - disables all contexts.

# SYNOPSIS

**disableallcontexts** [OPTION]...

# DESCRIPTION

This command line tool lists disables all contexts. Whenever a customer tries to log in to a disabled context, the login is denied. 

# OPTIONS

**-A**, **--adminuser** *masterAdmin*
: Context admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

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

**disableallcontexts -A masterAdmin -P secret**

Disables all contexts.

# SEE ALSO

[createcontext(1)](createcontext), [listcontext(1)](listcontext), [changecontext(1)](changecontext), [enablecontext(1)](enablecontext), [disablecontext(1)](disablecontext), [deletecontext(1)](deletecontext), [enableallcontexts(1)](enableallcontexts), [getcontextcapabilities(1)](getcontextcapabilities)