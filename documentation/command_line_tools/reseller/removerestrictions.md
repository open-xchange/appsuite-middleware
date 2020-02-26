---
title: removerestrictions
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin-reseller
---

# NAME

removerestrictions - removes all available restrictions from the database.

# SYNOPSIS

**removerestrictions** [OPTION]...

# DESCRIPTION

Use this tool to remove all restrictions from the database.

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

**removerestrictions -A masterAdmin -P masterPassword**

Remova all database restrictions.

# SEE ALSO

[listrestrictions(1)](listrestrictions) [initrestrictions(1)](initrestrictions) [updaterestrictions(1)](updaterestrictions) [updatemoduleaccessrestrictions(1)](updatemoduleaccessrestrictions)
