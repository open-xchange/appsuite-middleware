---
title: shutdown
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

shutdown - shutdown the server

# SYNOPSIS

**shutdown** [OPTIONS]


# DESCRIPTION

Shuts down the OSGi framework through invoking closure of top-level system bundle. If the parameter -w is defined the tools awaits the completion of the shutdown process and returns if it was successful.

# OPTIONS

**-h** *jmxHost*
: The JMX host

**-p** *jmxPort*
: The port

**-l** *jmxLogin*
: The optional login
 
**-pw** *jmxPassword*
: The optional password

**-w**
: Optional parameter to await complete shutdown before returning to the console

# EXAMPLES

**shutdown**