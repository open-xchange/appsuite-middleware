---
title: updaterestrictions
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin-reseller
---

# NAME

updaterestrictions - update available restrictions within the database.

# SYNOPSIS

**updaterestrictions** [OPTION]...

# DESCRIPTION

Use this tool to update available database restrictions, e.g. if a new release contains new types of restrictions.

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

**updaterestrictions -A masterAdmin -P masterPassword**

Update database restrictions.

# SEE ALSO

[listrestrictions(1)](listrestrictions) [initrestrictions(1)](initrestrictions) [removerestrictions(1)](removerestrictions) [updatemoduleaccessrestrictions(1)](updatemoduleaccessrestrictions)
