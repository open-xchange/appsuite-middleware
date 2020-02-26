---
title: clearpreviewcache
icon: far fa-circle
tags: Administration, Command Line tools, Caching
package: open-xchange-core
---

# NAME

clearpreviewcache - clears the preview cache.

# SYNOPSIS

**clearpreviewcache** [-h|--help]

**clearpreviewcache** [[[-c *contextId*] | [-a]] -A *masterAdmin* | *contextAdmin* -P *masterAdminPassword* | *contextAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*]]

# DESCRIPTION

This command line tool clears the preview cache.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier. Mandatory and mutually exclusive with `-a`.

**-a**, **--all**
: The flag to signal that contexts shall be processed. Mandatory and mutually exclusive with `-c`.

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

**clearpreviewcache -A oxadminmaster -P secret -c 1337**

Clears the preview cache for the specified context.
