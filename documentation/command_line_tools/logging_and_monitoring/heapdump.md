---
title: heapdump
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

heapdump - creates a heap dump of the OX middleware.

# SYNOPSIS

**heapdump** [-c][-v][-r][-a|-al]

# DESCRIPTION

This command line tool creates a heap dump of the OX middleware.

# OPTIONS

**-f**, **--file** *arg*
: The path name of the file in which to dump the heap snapshot; e.g. "/tmp/heap.bin"

**-H**, **--host** *jmxHost*
: The optional JMX host (default:localhost)

**-l**, **--login** *jmxLogin*
: The optional JMX login (if JMX authentication is enabled)

**-p**, **--port** *jmxPort*
: The optional JMX port (default:9999)

**-s**, **--password** *jmxPassword*
: The optional JMX password (if JMX authentication is enabled)

**-h**, **--help**
: Prints a help text

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite)
