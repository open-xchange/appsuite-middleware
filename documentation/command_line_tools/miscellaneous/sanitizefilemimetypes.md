---
title: sanitizefilemimetypes(1)
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

sanitizefilemimetypes - sanitises broken/corrupt MIME types currently held in database.

# SYNOPSIS

**sanitizefilemimetypes** [-h|--help]

**sanitizefilemimetypes** [[[-c *contextId*] | [-a]] [-i *mimetype_1,mimetype_2,...,mimetype_n*] -A *masterAdmin* | *contextAdmin* -P *masterAdminPassword* | *contextAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*]]

# DESCRIPTION

This command line tool sanitises broken/corrupt MIME types currently held in database for a specific context.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier. Mandatory and mutually exclusive with `-a`.

**-a**, **--all**
: The flag to signal that contexts shall be processed. Mandatory and mutually exclusive with `-c`.

**-i**, **--invalids** *mimetype_1,mimetype_2,...,mimetype_n*
: An optional comma-separated list of those MIME types that should be considered as broken/corrupt. Default are "application/force-download, application/x-download, application/$suffix".

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

**sanitizefilemimetypes -A oxadminmaster -P secret -c 1337**

Sanitises all broken/corrupt MIME types in the database for the specified context.
