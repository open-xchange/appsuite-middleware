---
title: creategroup
icon: far fa-circle
tags: Administration, Command Line tools, Group
---

# NAME

creategroup - creates a group within a context.

# SYNOPSIS

**creategroup** [OPTION]...

# DESCRIPTION

This command line tool creates a new group in a given context. Groups are created with no group members when no userid(s) are supplied with the parameter `-a`. 

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-n**, **--name** *groupName*
: The group name. Mandatory.

**-d**, **--displayname** *displayName*
: The group's display name. Mandatory.

**-a**, **--addmembers** *userId(s)*
: Comma separated identifiers of members to add to group (see `createuser(1)`).

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

**creategroup -A contextAdmin -P secret -c 1138 -n foobar -d "The FooBar" -a 3,4,5,6**

Creates a group with the specified name and adds the users with the specified identifiers as members.

# SEE ALSO

[deletegroup(1)](deletegroup), [listgroup(1)](listgroup), [changegroup(1)](changegroup)
