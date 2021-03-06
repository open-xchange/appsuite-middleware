---
title: moveuserfilestore
icon: far fa-circle
tags: Administration, Command Line tools, User, Filestore
package: open-xchange-admin
---

# NAME

moveuserfilestore - moves a user's files from one storage to another.

# SYNOPSIS

**moveuserfilestore** [OPTION]...

# DESCRIPTION

This command line tool moves a user's files from one storage to another.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory and mutually exclusive with `-N`.

**-N**, **--contextname** *contextName*
: The context name. Mandatory and mutually exclusive with `-c`.

**-i**, *--userid* *userId*
: Id of the user.

**-u**, *--username* *username*
: Username ofthe user.

**-f**, **--filestore** *filestore*
: The identifier for the file storage. Mandatory.

**-q**, **--quota** *quota*
: The file storage quota in MB for associated user. Mandatory.

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

**moveuserfilestore -A contextAdmin -P secret -c 1138 -i 137 -f 12 -m 152**

Moves the filestore of the specified user from the context one to an individual one and retains the already existing quota.
