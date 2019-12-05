---
title: changeresource
icon: far fa-circle
tags: Administration, Command Line tools, Resource, Context
---

# NAME

changeresource - changes a resource within a context.

# SYNOPSIS

**changeresource** [OPTION]...

# DESCRIPTION

This command line tool changes a resource within a context.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-i**, **--resourceid** *resourceId*
: The resource identifier. Mandatory.

**-n**, **--name** *resourceName*
: The resource name. Mandatory.

**-d**, **--displayname** *displayName*
: The resource display name. Mandatory.

**-e**, **--email** *email*
: The e-mail address of the resource. Mandatory.

**-a**, **-available** *true/false*
: Toggle the resource availability.

**-D**, **--description** *description*
: The resource's description.

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

**changeresource -A contextAdmin -P secret -c 1138 -i 31137 -a false**

Disables the resource with the specified identifier.

**changeresource -A contextAdmin -P secret -c 1138 -i 1138 -n foobear -D "This is the foobear resource."**

Renames and changes the description of the resource with the specified identifier.

# SEE ALSO

[deleteresource(1)](deleteresource), [listresource(1)](listresource), [createresource(1)](createresource)
