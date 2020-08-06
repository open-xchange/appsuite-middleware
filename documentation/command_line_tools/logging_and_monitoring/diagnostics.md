---
title: diagnostics
icon: far fa-circle
tags: Administration, Command Line tools, Monitoring
package: open-xchange-core
---

# NAME

diagnostics - prints out diagnostic information about the JVM and the OX middleware.

# SYNOPSIS

**diagnostics** [-c][-v][-r][-a|-al]

# DESCRIPTION

This command line tool prints out diagnostic information about the JVM and the OX middleware. 

# OPTIONS

**-a**, **--charsets**
: A list with all supported charsets of this JVM. This switch is mutually-exclusive with it's counter-part '-al'

**-al**, **--charsets-long**
: A long list with all supported charsets of this JVM. Along the charsets their aliases will also be listed as a comma separated list. The name of each charset will always be first. This switch is mutually-exclusive with it's counter-part '-a'

**-c**, **--cipher-suites**
: A list with all supported cipher suites of this JVM.

**-r**, **--protocols**
: A list with all supported SSL protocols of this JVM.

**-v**, **--version**
: The server's version.

**-s**, **--server** *rmiHost*
: The optional RMI server (default: localhost).

**-p**, **--port** *rmiPort*
: The optional RMI port (default:1099).

**-h**, **--help**
: Prints a help text.
