---
title: deletemultifactordevice
icon: far fa-circle
tags: Administration, Command Line tools, Security
package: open-xchange-multifactor
---

# NAME

deletemultifactordevice - deletes multifactor authentication devices.


# SYNOPSIS

**deletemultifactordevice** [OPTION]...

# DESCRIPTION

Deletes multifactor authentication devices for a given user.

# OPTIONS

**-c**, **--contextid** *contextid*
: The context identifier. Mandatory.

**-i**, *--userid* *userId*
: Id of the user. Mandatory.

**-A**, **--adminuser** *contextAdmin*
: Context admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *contextAdminPassword*
: Context admin password for authentication. Optional, depending on your configuration.

**-r**, **--provider** *provider*
: The multifactor provider name. Mandatory.

**-d**, **--device** *deviceId*
: The multifactor device id. Mandatory.

**-h**, **--help**
: Prints a help text.

# EXAMPLES

**deletemultifactordevice -A contextAdmin -P secret -c 1138 -i 137 -r U2F -d 8c6723dac0cf405e9fc424e8f2f788ee**

Deletes the specified U2F device of the user.

# SEE ALSO

[listmultifactordevice(1)](listmultifactordevice)
