---
title: listusersbyaliasdomain
icon: far fa-circle
tags: Administration, Command Line tools, User, Alias
package: open-xchange-admin
---

# NAME

listusersbyaliasdomain - returns all users with an alias within the given domain.

# SYNOPSIS

**listusersbyaliasdomain** [OPTION]...

# DESCRIPTION

This command line tool lists all users with an alias within the given domain.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-d**, **--alias-domain** *alias-domain*
:  The domain of the user aliases. Mandatory.

**-A**, **--adminuser** *contextAdmin*
: Context admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *contextAdminPassword*
: Context admin password for authentication. Optional, depending on your configuration.

**--csv**
: Format output to csv

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

**--length**
: Limits the result size.

**--offset**
: The beginning offset of the result list.

# EXAMPLES

**listusersbyaliasdomain -A contextAdmin -P secret -c 1138 -d foobar.io**

Lists all users that have the specified alias within the specified context.
