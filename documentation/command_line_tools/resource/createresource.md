---
title: createresource
icon: far fa-circle
tags: Administration, Command Line tools, Resource, Context
---

# NAME

createresource - creates a resource within a context.

# SYNOPSIS

**createresource** [OPTION]...

# DESCRIPTION

This command line tool allows to create a new resource within a given context. 

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

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

**createresource -A contextAdmin -P secret -c 1138 -n foobar -d "The FooBar" -D "This is the foobar resource." -e foobar@ox.io**

Creates the resource with the specified name that is enabled by default.

**createresource -A contextAdmin -P secret -c 1138 -n foobar -d "The FooBar" -D "This is the foobar resource." -e foobar@ox.io -a false**

Creates the resource with the specified name that is disabled by default.

# SEE ALSO

[deleteresource(1)](deleteresource), [listresource(1)](listresource), [changeresource(1)](changeresource)
