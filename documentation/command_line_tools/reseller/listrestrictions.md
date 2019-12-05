---
title: listrestrictions
icon: far fa-circle
tags: Administration, Command Line tools
---

# NAME

listrestrictions - lists all available restrictions stored in the database.

# SYNOPSIS

**listrestrictions** [OPTION]...

# DESCRIPTION

Use this tool to list restrictions stored in the database.

# OPTIONS

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**--csv**
: Format output to csv.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**listrestrictions -A masterAdmin -P masterPassword**

List database restrictions.

# SEE ALSO

[initrestrictions(1)](initrestrictions) [removerestrictions(1)](removerestrictions) [updaterestrictions(1)](updaterestrictions) [updatemoduleaccessrestrictions(1)](updatemoduleaccessrestrictions)
