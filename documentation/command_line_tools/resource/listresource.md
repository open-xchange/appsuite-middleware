---
title: listresource
icon: far fa-circle
tags: Administration, Command Line tools, Resource, Context
package: open-xchange-admin
---

# NAME

listresource - lists all resources within a context.

# SYNOPSIS

**listresource** [OPTION]...

# DESCRIPTION

This command line tool lists all resources within a context. Optionally, with the use of '-s' you can search for a resource name that matches the specified search pattern.

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

**listresource -A contextAdmin -P secret -c 1138**

Lists all resources in the specified context

**listresource -A contextAdmin -P secret -c 1138 -s foobar**

Lists all resources that match the search pattern

# SEE ALSO

[deleteresource(1)](deleteresource), [changeresource(1)](changeresource), [createresource(1)](createresource)
