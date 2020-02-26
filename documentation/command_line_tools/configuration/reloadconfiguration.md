---
title: reloadconfiguration
icon: far fa-circle
tags: Administration, Command Line tools, Configuration
package: open-xchange-core
---

# NAME

reloadconfiguration - reloads all changed configuration properties that are 'reloadable'.

# SYNOPSIS

**reloadconfiguration** [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

This command line tool reloads all changed configuration properties that are 'reloadable'.

# OPTIONS

**-h**, **--help**
: Prints a help text

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

