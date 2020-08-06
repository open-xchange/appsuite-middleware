---
title: deletereason
icon: far fa-circle
tags: Administration, Command Line tools, Maintenance
package: open-xchange-admin
---

# NAME

deletereason - deletes a maintenance reason

# SYNOPSIS

**deletereason** [-h|--help]

**deletereason** --environment

**deletereason** -A *masterAdminUser* -P *masterAdminPassword* -i *reasonId* [--nonl] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool deletes a maintenance reason.

# OPTIONS

**-i**, **--reasonid** *reasonId*
: The identifier of the reason to be deleted.

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

**deletereason -A masteradmin -P secret -i 1138**

Deletes the reason with the specified identifier.

# SEE ALSO

[createreason(1)](createreason), [listreason(1)](listreason)
