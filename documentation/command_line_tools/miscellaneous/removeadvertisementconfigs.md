---
title: removeadvertisementconfigs
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-advertisement
---

# NAME

removeadvertisementconfigs - removes advertisement configuration

# SYNOPSIS

**removeadvertisementconfigs** [OPTIONS]


# DESCRIPTION

This tool allows the master admin to remove the advertisement configurations. He can either remove all or a single reseller configurations. It is also possible to remove only the resellers, which are not active anymore (e.g in case they are deleted).

# OPTIONS

**-A**, **--adminuser** *adminUser*
: The Admin username

**-c**, **--clean**
: If set the clt only removes configurations of resellers which doesn't exist any more.

**-h**, **--help**
: Prints this help text

**-i**, **--includePreviews**
: If set the clt also removes preview configurations. This is only applicable in case the argument 'clean' is used.

**-p**, **--port** **rmiPort**
: The optional RMI port (default:1099)

**-P**, **--adminpass** *adminPassword*
: The admin password

**-r**, **--reseller** *resellerId*
: Defines the reseller for which the configurations should be deleted. Use 'default' for the default reseller or in case no reseller are defined. If missing all configurations are deleted instead.

**--responsetimeout** *timeout*
: The optional response timeout in seconds when reading data from server (default: 0s; infinite)

**-s**, **--server** **rmiHost**
: The optional RMI server (default: localhost) the argument 'clean' is used.