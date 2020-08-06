---
title: enableallcontexts
icon: far fa-circle
tags: Administration, Command Line tools, Context
package: open-xchange-admin
---

# NAME

enableallcontexts - enables all contexts.

# SYNOPSIS

**enableallcontexts** [OPTION]...

# DESCRIPTION

This command line tool lists enables all disabled contexts.

# OPTIONS

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

**enableallcontexts -A masterAdmin -P secret**

Enables all contexts.

# SEE ALSO

[createcontext(1)](createcontext), [listcontext(1)](listcontext), [changecontext(1)](changecontext), [enablecontext(1)](enablecontext), [disableallcontexts(1)](disableallcontexts), [deletecontext(1)](deletecontext), [disablecontext(1)](disablecontext), [getcontextcapabilities(1)](getcontextcapabilities)