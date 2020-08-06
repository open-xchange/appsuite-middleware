---
title: checkcountsconsistency
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin
---

# NAME

checkcountsconsistency - checks the consistencies for the `count` table.

# SYNOPSIS

**checkcountsconsistency** [OPTION]...

# DESCRIPTION

This command line tool checks the consistencies for the `count` table.

# OPTIONS

**-A**, **--adminuser** *masterAdmin*
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

**checkcountsconsistency -A masterAdmin -P secret**

Performs a consistency check on the `count` database table.

