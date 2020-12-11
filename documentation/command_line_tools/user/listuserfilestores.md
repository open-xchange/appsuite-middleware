---
title: listuserfilestores
icon: far fa-circle
tags: Administration, Command Line tools, User, Filestore
package: open-xchange-admin
---

# NAME

listuserfilestores - lists all user objects with given filestore for a given context.

# SYNOPSIS

**listuserfilestores** [OPTION]...

# DESCRIPTION

This command line tool lists all user objects with given filestore for a given context. If the filestore id is not specified then all user objects with an individual filestore for a given context are retrieved instead.

# OPTIONS

**-c**, **--contextid** *contextId*
: The context identifier. Mandatory.

**-m**, **--master_only**
: Prints only the master user .

**-f**, **--filestore_id** *filestoreId*
: The id of the filestore. Lists users for this filestore.

**-A**, **--adminuser** *masterAdmin*
: user admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *userAdminPassword*
: user admin password for authentication. Optional, depending on your configuration.

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

**listuserfilestores -A masterAdmin -P secret -c 1138 -f 137**

Lists all user objects within the specified .

