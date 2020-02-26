---
title: listreason
icon: far fa-circle
tags: Administration, Command Line tools, Maintenance
package: open-xchange-admin
---

# NAME

listreason - deletes a maintenance reason

# SYNOPSIS

**listreason** [-h|--help]

**listreason** --environment

**listreason** -A *masterAdminUser* -P *masterAdminPassword* -s *searchPattern* [--nonl] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool deletes a maintenance reason.

# OPTIONS

**-s**, **--searchpattern** *searchPattern*
: The pattern to search for when listing the maintenance reasons.

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

**listreason -A masteradmin -P secret**

Lists all registered maintenance reasons.

**listreason -A masteradmin -P secret -s "upgrade"**

Lists all registered maintenance that match the specified search pattern.

# SEE ALSO

[createreason(1)](createreason), [deletereason(1)](deletereason)
