---
title: calculatefilechecksums
icon: far fa-circle
tags: Administration, Command Line tools, Filestore
package: open-xchange-core
---

# NAME

calculatefilechecksums - calculates missing file checksums.

# SYNOPSIS

**calculatefilechecksums**  [-d *databaseId* | -c *contextId*] [-C] -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*] [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

This command line tool calculates missing file checksums.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier. Mandatory and mutually exclusive with `-a`.

**-C**, **--calculate**
: Calculate and store missing checksums (if not specified, files with missing checksums are printed out only).

**-d**, **--database** *databaseId*
: The database pool identifier to determine/calculate missing checksums in

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-s**, **--server** *rmiHost*
: The optional RMI server (default: localhost).

**-p**, **--port** *rmiPort*
: The optional RMI port (default:1099).

**-h**, **--help**
: Prints a help text.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**calculatefilechecksums -A oxadminmaster -P secret -d 1337 -c 1138 -C**

Calculates the checksums for all files in the specified context.
