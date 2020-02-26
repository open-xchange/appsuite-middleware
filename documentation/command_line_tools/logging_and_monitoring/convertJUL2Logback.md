---
title: convertJUL2Logback
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

convertJUL2Logback - converts log configuration

# SYNOPSIS

**convertJUL2Logback** [OPTIONS] | [-h]

# DESCRIPTION

This command line tool reads Java Util logging properties configuration files and converts that to a LogBack XML configuration.

# OPTIONS

**-h**, **--help**
: Prints a help text

**-i**, **--in** *input*
: Java Util logging properties configuration file to read. If omitted this will be read vom STDIN.

**-o**, **--out** *output*
: Added JUL logger will be written as properties configuration to this file. If this option is omitted the output will be written to STDOUT.