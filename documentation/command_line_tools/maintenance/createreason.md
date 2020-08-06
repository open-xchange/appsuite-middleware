---
title: createreason
icon: far fa-circle
tags: Administration, Command Line tools, Maintenance
package: open-xchange-admin
---

# NAME

createreason - creates a maintenance reason

# SYNOPSIS

**createreason** [-h|--help]

**createreason** --environment

**createreason** -A *masterAdminUser* -P *masterAdminPassword* -r *reasonText* [--nonl] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool creates a maintenance reason.

# OPTIONS

**-r**, **--reasontext** *reasonText*
: The text for the reason.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**createreason -A masteradmin -P secret -r "Upgrading to version x.y.z"**

Creates the reason with the specified text.

# SEE ALSO

[deletereason(1)](deletereason), [listreason(1)](listreason)
