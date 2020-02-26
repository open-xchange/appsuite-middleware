---
title: getUpdateTaskStatus
icon: far fa-circle
tags: Administration, Command Line tools, Database, Schema, Update Task
package: open-xchange-core
---

# NAME

getUpdateTaskStatus - retrieves the status of a running or completed update task job.

# SYNOPSIS

**getUpdateTaskStatus** -j *job-id* -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*] [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

This command line tool retrieves the status of a running or completed update task job.  If the job already finished and its status has not yet retrieved, then its status will be returned and it will be removed from the pool. A further invocation of this tool will yield no results for the same job identifier. If the job is still running then its invocation will return its current status.

# OPTIONS

**-j**, **--job-id** *job-id*
: The job identifier for which to retrieve its status.

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

**getUpdateTaskStatus -A oxadminmaster -P secret -j 1138**

Returns the update task status for the job with the specified identifier.
