---
title: listuser
icon: far fa-circle
tags: Administration, Command Line tools, User
package: open-xchange-admin
---

# NAME

listuser - lists all users within a context.

# SYNOPSIS

**listuser** [OPTION]...

# DESCRIPTION

This command line tool lists all users within a context. Optionally, with the use of '-s' you can search for a user name that matches the specified search pattern.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-s**, **--searchpattern** *searchPattern*
: The optional search pattern.

**-i**, **--ignorecase**
: Do a case-insensitive search with the given search pattern

**--includeguests**
: Add guest users to listing. Available with v7.8.0

**--excludeusers**
: Exclude usual users from listing. Available with v7.8.0

**-A**, **--adminuser** *contextAdmin*
: Context admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *contextAdminPassword*
: Context admin password for authentication. Optional, depending on your configuration.

**--csv**
: Format output to csv

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**listuser -A contextAdmin -P secret -c 1138**

Lists all users in the specified context

**listuser -A contextAdmin -P secret -c 1138 -s foobar**

Lists all users that match the search pattern

# SEE ALSO

[createuser(1)](createuser), [deleteuser(1)](deleteuser), [changeuser(1)](changeuser), [getusercapabilities(1)](getusercapabilities)
