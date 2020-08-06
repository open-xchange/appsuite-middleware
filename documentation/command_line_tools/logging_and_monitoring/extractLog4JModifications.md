---
title: extractLog4Modifications
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

extractLog4Modifications - modifies logger levels

# SYNOPSIS

**extractLog4Modifications** [OPTIONS] | [-h]

# DESCRIPTION

This command line tool reads a log4j.xml and outputs modified logger levels as JUL properties format.

# OPTIONS

**-h**, **--help**
: Prints a help text

**-i**, **--in** *input*
: XML document is read from this file. If omitted the input will be read from STDIN.

**-o**, **--out** *output*
: JUL properties configuration file is written to this file. If this option is omitted the output will be written to STDOUT.