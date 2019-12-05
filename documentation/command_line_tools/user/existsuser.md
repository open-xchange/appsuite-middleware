---
title: existsuser
icon: fa fa-question-circle
tags: Administration, Command Line tools, User
---

# NAME

existsuser - Checks for user existence.

# SYNOPSIS

**existsuser** [OPTION]...

# DESCRIPTION

This command line tool allows to check whether a user exists in a given context. It uses either the id or the display name of the user
 
# OPTIONS

**-c**, **--contextid** *contextid*
: The context identifier. Mandatory.

**-i**, *--userid* *userId*
: Id of the user.

**-u**, *--username* *username*
: Username of the user.

**-d**, *--displayname* *displayname*
: Display name of the user.

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

**existsuser -A contextAdmin -P secret -c 1138 -i 137**

Deletes the user with the specified identifier in the specified context.

# SEE ALSO

[createuser(1)](createuser), [listuser(1)](listuser), [changeuser(1)](changeuser), [deleteuser(1)](deleteuser)