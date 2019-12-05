---
title: listquota
icon: far fa-circle
tags: Administration, Command Line tools, Context, Quota
---

# NAME

listquota - lists the quota limitations of a context 

# SYNOPSIS

**listquota** [OPTION]...

# DESCRIPTION

This command line tool lists the quota limitations of a context. 

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

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

**listquota -A masterAdmin -P secret -c 1138**

Lists the quota limitations of the context with the specified identifier.

# SEE ALSO

[createcontext(1)](createcontext), [enablecontext(1)](enablecontext), [changecontext(1)](changecontext), [enableallcontexts(1)](enableallcontexts), [disableallcontexts(1)](disableallcontexts), [deletecontext(1)](deletecontext), [disablecontext(1)](disablecontext), [listcontext(1)](listcontext)
