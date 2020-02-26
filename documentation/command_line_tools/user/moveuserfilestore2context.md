---
title: moveuserfilestore2context
icon: far fa-circle
tags: Administration, Command Line tools, User, Context, Filestore
package: open-xchange-admin
---

# NAME

moveuserfilestore2context - moves a user's files from his own to a context storage.

# SYNOPSIS

**moveuserfilestore2context** [OPTION]...

# DESCRIPTION

This command line tool moves a user's files from his own to a context storage.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory and mutually exclusive with `-N`.

**-N**, **--contextname** *contextName*
: The context name. Mandatory and mutually exclusive with `-c`.

**-i**, *--userid* *userId*
: Id of the user.

**-u**, *--username* *username*
: Username ofthe user.

**-A**, **--adminuser** *contextAdmin*
: Context admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *contextAdminPassword*
: Context admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**moveuserfilestore2context -A contextAdmin -P secret -c 1138 -i 137**

Moves the filestore of the specified user from the his own to a context one.
