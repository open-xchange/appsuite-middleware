---
title: removeoauthclient
icon: far fa-circle
tags: Administration, Command Line tools, OAuth
---

# NAME

removeoauthclient - Removes OAuth clients

# SYNOPSIS

**removeoauthclient** [OPTIONS]

# DESCRIPTION

This command line tool removes an specific OAuth client

# OPTIONS

**--id** *id*
: The id of the OAuth client

**-A**, **--adminuser** *masterAdminUser*
:   Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
:   Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**--environment**
:   Show info about commandline environment.

**--nonl**
:   Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**removeoauthclient -A masteradmin -P secret -i 5**

Removes the OAuth clients

# SEE ALSO

[createoauthclient(1)](createoauthclient), [disableoauthclient(1)](disableoauthclient), [enableoauthclient(1)](enableoauthclient), [getoauthclient(1)](getoauthclient), [listoauthclient(1)](listoauthclient), [revokeoauthclient(1)](revokeoauthclient), [updateoauthclient(1)](updateoauthclient)