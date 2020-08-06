---
title: listbundles
icon: far fa-circle
tags: Administration, Command Line tools, Bundle
package: open-xchange-core
---

# NAME

listbundles - starts a bundle

# SYNOPSIS

**listbundles** [OPTIONS] bundlename


# DESCRIPTION

This command line tool starts a specific bundle.

# OPTIONS

**-h** *jmxHost*
: The JMX host

**-p** *jmxPort*
: The port

**-l** *jmxLogin*
: The optional login
 
**-pw** *jmxPassword*
: The optional password

# EXAMPLES

**listbundles open-xchange-server**


# SEE ALSO

[installbundle(1)](installbundle), [startbundle(1)](startbundle), [refreshbundles(1)](refreshbundles), [stopbundle(1)](stopbundle), [uninstallbundle(1)](uninstallbundle), [updatebundle(1)](updatebundle)