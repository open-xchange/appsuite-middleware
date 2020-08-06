---
title: listdriveclient
icon: far fa-circle
tags: Administration, Command Line tools, Drive
package: open-xchange-drive-client-windows
---

# NAME

listdriveclient - list windows drive clients

# SYNOPSIS

**listdriveclient** [OPTIONS]

# DESCRIPTION

The command-line tool to list the available windows drive clients

# OPTIONS

**-o**, **-invalid_only**
:  Retrieves only invalid brandings.

**-s**, **--server** *rmiHost*
: The optional RMI server (default: localhost)

**-v**, **--validate**
: If defined, the brandings will be validated and only valid brandings will be returned. This validation verifies if all required files are present.

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

[reloaddriveclients(1)](reloaddriveclients)