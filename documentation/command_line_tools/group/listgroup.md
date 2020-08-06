---
title: listgroup
icon: far fa-circle
tags: Administration, Command Line tools, Group
package: open-xchange-admin
---

# NAME

listgroup - lists all groups within a context.

# SYNOPSIS

**listgroup** [OPTION]...

# DESCRIPTION

This command line tool lists all groups within a context. Optionally, with the use of '-s' you can search for a group name that matches the specified search pattern.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-s**, **--searchpattern** *searchPattern*
: The optional search pattern.

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

**listgroup -A contextAdmin -P secret -c 1138**

Lists all groups in the specified context

**listgroup -A contextAdmin -P secret -c 1138 -s foobar**

Lists all groups that match the search pattern

# SEE ALSO

[deletegroup(1)](deletegroup), [changegroup(1)](changegroup), [creategroup(1)](creategroup)
