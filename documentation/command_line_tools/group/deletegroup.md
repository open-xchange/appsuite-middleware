---
title: deletegroup
icon: far fa-circle
tags: Administration, Command Line tools, Group
package: open-xchange-admin
---

# NAME

deletegroup - deletes a group from a context.

# SYNOPSIS

**deletegroup** [OPTION]...

# DESCRIPTION

This command line tool deletes a group in a given context. There is no need to remove the group members before.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-n**, **--name** *groupName*
: The group name. Mandatory and mutually exclusive with `-i`.

**-i**, **--groupid** *groupId*
: The resource identifier. Mandatory and mutually exclusive with `-n`.

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

**deletegroup -A contextAdmin -P secret -c 1138 -i 31137**

Deletes the group with the specified identifier from the specified context.

# SEE ALSO

[deletegroup(1)](deletegroup), [listgroup(1)](listgroup), [changegroup(1)](changegroup)
