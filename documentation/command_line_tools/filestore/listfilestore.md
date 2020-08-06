---
title: listfilestore
icon: far fa-circle
tags: Administration, Command Line tools, Filestore
package: open-xchange-admin
---

# NAME

listfilestore - lists all available filestores.
# SYNOPSIS

**listfilestore** [-h|--help]

**listfilestore** -A *masterAdminUser* -P *masterAdminPassword* [-s *searchPattern* -u --responsetimeout *seconds* --nonl --csv]

# DESCRIPTION

This command line tool lists all available filestores.

# OPTIONS

**-s**, **--searchpattern** *searchPattern*
: The pattern to search for when listing filestores.

**-u**, **--omitUsage**
: Do not load the usage of the file stores, which is expensive.

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

**--csv**
: Format output to csv.

# EXAMPLES

**listfilestore -A masteradmin -P secret -u -s foobar**

Lists all filestores that match the specified search pattern and omits calculating the usage of each found filestore.

# SEE ALSO

[registerfilestore(1)](registerfilestore), [unregisterfilestore(1)](unregisterfilestore), [changefilestore(1)](changefilestore), [recalculatefilestoreusage(1)](recalculatefilestoreusage)