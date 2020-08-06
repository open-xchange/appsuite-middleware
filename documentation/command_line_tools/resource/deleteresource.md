---
title: deleteresource
icon: far fa-circle
tags: Administration, Command Line tools, Resource, Context
package: open-xchange-admin
---

# NAME

deleteresource - deletes a resource from a context.

# SYNOPSIS

**deleteresource** [OPTION]...

# DESCRIPTION

This command line tool deletes a resource from a context.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-i**, **--resourceid** *resourceId*
: The resource identifier. Mandatory and mutually exclusive with `-n`.

**-n**, **--name** *resourceName*
: The resource name. Mandatory and mutually exclusive with `-i`.

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

**deleteresource -A contextAdmin -P secret -c 1138 -i 31137**

Deletes the specified resource from the specified context.

# SEE ALSO

[changeresource(1)](changeresource), [listresource(1)](listresource), [createresource(1)](createresource)
