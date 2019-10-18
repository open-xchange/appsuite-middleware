---
title: updatemoduleaccessrestrictions
icon: far fa-circle
tags: Administration, Command Line tools
---

# NAME

updatemoduleaccessrestrictions - update available module access based restrictions within the database.

# SYNOPSIS

**updatemoduleaccessrestrictions** [OPTION]...

# DESCRIPTION

Use this tool to update available database restrictions, e.g. if module access combinations have been added or removed.

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

**updatemoduleaccessrestrictions -A masterAdmin -P masterPassword**

Update database restrictions.

# SEE ALSO

[listrestrictions(1)](listrestrictions) [initrestrictions(1)](initrestrictions) [removerestrictions(1)](removerestrictions) [updaterestrictions(1)](updaterestrictions)
