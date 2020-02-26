---
title: extractJULModifications
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

extractJULModifications - modifies logger levels

# SYNOPSIS

**extractJULModifications** [OPTIONS] | [-h]

# DESCRIPTION

This command line tool extracts modified logger levels from file-logging.properties.

# OPTIONS

**-h**, **--help**
: Prints a help text

**-i**, **--in** *input*
: Java Util logging properties configuration file to read. If omitted this will be read vom STDIN.

**-o**, **--out** *output*
: Added JUL logger will be written as properties configuration to this file. If this option is omitted the output will be written to STDOUT.