---
title: jobcontrol
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin
---

# NAME

jobcontrol - OX task management utility.

# SYNOPSIS

**allpluginsloaded** [-h|--help]

**allpluginsloaded** [-f] | [[-c *contextId* [-t *id*|-d *id*|-l]] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool provides functionality for managing the background jobs inside an OX node.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier.

**-f**, **--flush**
: Flushes all finished jobs from the queue.

**-t**, **--details** *id*
: Shows details for the job with the specified identifier.

**-d**, **--delete** *id*
: Deletes the job with the specified identifier.

**-l**, **--list**
: Lists all background jobs.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

