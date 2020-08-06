---
title: initrestrictions
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin-reseller
---

# NAME

initrestrictions - initializes available restrictions within the database.

# SYNOPSIS

**initrestrictions** [OPTION]...

# DESCRIPTION

This tool can only be used once in order to initialize the restrictions that are
available within an installation. If restrictions should be updated, the tool
[updaterestrictions(1)](updaterestrictions) can be used.

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

**initrestrictions -A masterAdmin -P masterPassword**

Initialize database restrictions.

# SEE ALSO

[listrestrictions(1)](listrestrictions) [removerestrictions(1)](removerestrictions) [updaterestrictions(1)](updaterestrictions) [updatemoduleaccessrestrictions(1)](updatemoduleaccessrestrictions)
