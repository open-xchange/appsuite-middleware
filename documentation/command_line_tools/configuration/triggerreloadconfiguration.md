---
title: triggerreloadconfiguration
icon: far fa-circle
tags: Administration, Command Line tools, Configuration
---

# NAME

triggerreloadconfiguration - Triggers the reload

# SYNOPSIS

**reloadconfiguration** OPTIONS

# DESCRIPTION

This command line tool triggers the reload with optional an other port to use.

# OPTIONS

**-h**, **--help**
: Prints a help text

**-d**
: Uses the RMI port from rmi.properties

**-f** *file*
: The configuration file containing the possibly overridden RMI port to use for the service to reload. Falls back to rmi.properties if file or property are missing.

# SEE ALSO

[reloadconfiguration(1)](reloadconfiguration)