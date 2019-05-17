---
title: changefilestore
icon: far fa-circle
tags: Administration, Command Line tools, Filestore
---

# NAME

changefilestore - registers a filestore.

# SYNOPSIS

**changefilestore** [-h|--help]

**changefilestore** -A *masterAdminUser* -P *masterAdminPassword* -t *storePath* [-s *storeSize* -x *maxContexts* --responsetimeout *seconds* --nonl]

# DESCRIPTION

This command line tool changes the store size (`-s`) and the maximum supported contexts (`x`) for a filestore.

# OPTIONS

**-i**, **--id** *id*
: The id of the filestore which should be changed. Mandatory.

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

**changefilestore -A masteradmin -P secret -i 3 -x 1000 -s 5000**

Changes the filestore with the specified identifier and the maximum supported contexts and maximum size.

# SEE ALSO

[unregisterfilestore(1)](unregisterfilestore), [registerfilestore(1)](registerfilestore),[listfilestore(1)](listfilestore), [recalculatefilestoreusage(1)](recalculatefilestoreusage)