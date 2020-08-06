---
title: registerfilestore
icon: far fa-circle
tags: Administration, Command Line tools, Filestore
package: open-xchange-admin
---

# NAME

registerfilestore - registers a filestore.
# SYNOPSIS

**registerfilestore** [-h|--help]

**registerfilestore** -A *masterAdminUser* -P *masterAdminPassword* -t *storePath* [-s *storeSize* -x *maxContexts* --responsetimeout *seconds* --nonl]

# DESCRIPTION

This command line tool registers a filestore.

# OPTIONS

**-t**, **--storepath** *filestorePath*
: Path to store filestore contents in URI format e.g. file:/tmp/filestore. Mandatory.

**-s**, **--storesize** *storeSize*
: The maximum size of the filestore in MB. Default: 1000

**-x**, **--maxcontexts** *maxContexts*
: The maximum number of contexts. Default: 5000

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**registerfilestore -A masteradmin -P secret -t file:/var/lib/open-xchange/filestore -x 1000 -s 5000**

Registers a filestore under the specified location which supports 1.000 contexts and has a maximum size of 5GB.

# SEE ALSO

[unregisterfilestore(1)](unregisterfilestore), [listfilestore(1)](listfilestore), [changefilestore(1)](changefilestore), [recalculatefilestoreusage(1)](recalculatefilestoreusage)