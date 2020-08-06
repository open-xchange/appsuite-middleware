---
title: allpluginsloaded
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin
---

# NAME

allpluginsloaded - checks whether all bundles are loaded and active.

# SYNOPSIS

**allpluginsloaded** [-h|--help]

**allpluginsloaded** [-l *jmxLogin*] [-s *jmxPassword*] [-H *jmxHost*] [-p *jmxPort*] [--responsetimeout *seconds*]

# DESCRIPTION

This command line tool checks whether all bundles of the OX middleware node are loaded and active.

# OPTIONS

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

