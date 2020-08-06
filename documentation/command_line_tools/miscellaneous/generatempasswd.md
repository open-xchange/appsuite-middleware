---
title: generatempasswd
icon: far fa-circle
tags: Administration, Command Line tools, Security
package: open-xchange-admin
---

# NAME

generatempasswd - generates the master password file.

# SYNOPSIS

**generatempasswd** [OPTION]...

# DESCRIPTION

This command line tool generates the master password file. 

# OPTIONS

**-A**, **--adminuser** *username*
: Master Admin user name (Default: oxadminmaster)

**-P**, **--adminpass** *password*
: Master Admin password

**-e**, **--encryption** *encryption*
: Encryption algorithm to use for the password (Default: bcrypt)

**-f**, **--mpasswdfile** *path*
: Path to mpasswd (Default: /opt/open-xchange/etc/mpasswd)

**-h**, **--help**
: Prints this help text

# SUPPORTED ENCRYPTION ALGORITHMS

 - sha1: deprecated
 - sha256
 - sha512: Might not run on all JVMs. More information here: https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html
 - bcrypt: default
 - crypt

# EXAMPLES

**generatempasswd -A admindude -P secret -e sha256 -f /opt/openexchange/etc/masterpasswd**

Generates the master password for the specified admin and with the specified encryption algorithm.
