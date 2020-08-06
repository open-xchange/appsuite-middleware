---
title: xmlModifier
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

xmlModifier - modifies XML files

# SYNOPSIS

**xmlModifier** [-h|--help]

**sanitizefilemimetypes** [OPTIONS]

# DESCRIPTION

This command line tool can modify XML configuration files. Currently only allows to add XML fragments.

# OPTIONS

**-h**,**--help**
: Prints the help text 

**i**, **in**, *input*,
: XML document is read from this file.

**o**, **out**, *output*
: Modified XML document is written to this file.

**x**, **xpath** *xpath*
: XPath to the elements that should be modified.

**a**, **add** *filename*
: XML file that should add the elements denoted by the XPath. - can be used to read from STDIN.

**r**, **replace** *filename*
: XML file that should replace the elements denoted by the XPath. - can be used to read from STDIN.

**m**, **remove** *filename*
: XML file that should remove the elements denoted by the XPath. - can be used to read from STDIN.

**d**, **id** *attribute*
: Defines the identifying attribute as XPath (relative to \**-x\**) to determine if an element should be replaced (-r). If omitted all matches will be replaced.

**z**, **zap**
: Defines if duplicate matching elements should be removed(zapped) instead of only being replaced (-r).

**s**, **scr**, *scr*
: Specifies which scr should be executed on the xml document selected via -i