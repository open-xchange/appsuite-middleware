---
title: getaccesscombinationnameforuser
icon: far fa-circle
tags: Administration, Command Line tools, User
package: open-xchange-admin
---

# NAME

getaccesscombinationnameforuser - returns the access combination name for a user.

# SYNOPSIS

**getaccesscombinationnameforuser** [OPTION]...

# DESCRIPTION

This command line tool returns the access combination name for a user.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-i**, **--userid** *userId*
: The user identifier. Mandatory and mutually exclusive with `-u`.

**-u**, **--username** *username*
: The user name. Mandatory and mutually exclusive with `-i`.

**-A**, **--adminuser** *masterAdmin*
: user admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *userAdminPassword*
: user admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**getaccesscombinationnameforuser -A masterAdmin -P secret -c 1138 -i 137**

Returns the access combination name for the specified user.

