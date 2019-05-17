---
title: changegroup
icon: far fa-circle
tags: Administration, Command Line tools, Group
---

# NAME

changegroup - changes a group within a context.

# SYNOPSIS

**changegroup** [OPTION]...

# DESCRIPTION

This command line tool changes a group within a context.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-i**, **--groupid** *groupId*
: The group identifier. Mandatory.

**-n**, **--name** *groupName*
: The group name. Mandatory.

**-d**, **--displayname** *displayName*
: The group display name. Mandatory.

**-a**, **--addmembers** *userId(s)*
: Comma separated identifiers of members to add to group (see `createuser(1)`).

**-r**, **--removemembers** *userId(s)*
: Comma separated identifiers of members to remove from group (see `createuser(1)`).

**-D**, **--description** *description*
: The group's description.

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

**changegroup -A contextAdmin -P secret -c 1138 -i 31137 -a 3,4,5 -r 7,8,9**

Adds and removes users to and from the group with the specified identifier

**changegroup -A contextAdmin -P secret -c 1138 -i 31137 -n foobear -d "The Foo Bear"**

Renames and changes the name and display name of the group with the specified identifier.

# SEE ALSO

[deletegroup(1)](deletegroup), [listgroup(1)](listgroup), [creategroup(1)](creategroup)
