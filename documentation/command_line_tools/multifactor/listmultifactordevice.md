---
title: listmultifactordevice
icon: far fa-circle
tags: Administration, Command Line tools, Security
---

# NAME

listmultifactordevice - lists multifactor authentication devices for a given user.

# SYNOPSIS

**listmultifactordevice** [OPTION]...

# DESCRIPTION

Lists multifactor authentication devices for a given user.

# OPTIONS

**-c**, **--contextid** *contextid*
: The context identifier. Mandatory.

**-i**, *--userid* *userId*
: Id of the user. Mandatory.

**-A**, **--adminuser** *contextAdmin*
: Context admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *contextAdminPassword*
: Context admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

# EXAMPLES

**listmultifactordevice -A contextAdmin -P secret -c 1138 -i 137**

Lists all multifactor authentication devices for the specified user.

# SEE ALSO

[deletemultifactordevice(1)](deletemultifactordevice)
