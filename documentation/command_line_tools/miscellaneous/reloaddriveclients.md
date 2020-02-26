---
title: reloaddriveclients
icon: far fa-circle
tags: Administration, Command Line tools, Drive
package: open-xchange-drive-client-windows
---

# NAME

reloaddriveclients - reload windows drive clients

# SYNOPSIS

**reloaddriveclients** [OPTIONS]

# DESCRIPTION

The command-line tool to reload the available windows drive clients

# OPTIONS

**-s**, **--server** *rmiHost*
: The optional RMI server (default: localhost)

**--path** *arg*
: Defines the path to look for brandings. If set the configured path will be ignored.

**-A**, **--adminuser** *masterAdminUser*
:   Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
:   Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**--environment**
:   Show info about commandline environment.

**--nonl**
:   Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# SEE ALSO

[listdriveclient(1)](listdriveclient)