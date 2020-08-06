---
title: deduplicatecontacts
icon: far fa-circle
tags: Administration, Command Line tools, Contacts
package: open-xchange-core
---

# NAME

deduplicatecontacts - removes duplicate contact entries.

# SYNOPSIS

**deduplicatecontacts** -c *contextId* -f *folderId* [-m *maxNumber*] [-e] -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*] [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

This command line tool removes duplicate contact entries. Handle with care and review the found duplicates before executing the de-duplication. Detected duplicates are deleted permanently, with no recovery option.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier.

**-e**, **--execute**
: Actually performs the de-duplication of contacts - by default, identifiers of duplicated contacts are printed out only.

**-f**, **--folder** *folderId*
: A valid contact folder identifier

**-m**, **--max** *maxNumber*
: The maximum number of contacts to process, or 0 for no limit, defaults to 1000000

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

