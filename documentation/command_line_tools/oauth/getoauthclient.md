---
title: getoauthclient
icon: far fa-circle
tags: Administration, Command Line tools, OAuth
package: open-xchange-admin-oauth-provider
---

# NAME

getoauthclient - Get a specific OAuth client

# SYNOPSIS

**getoauthclient** [OPTIONS]

# DESCRIPTION

This command line tool gets a specific OAuth client

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

**getoauthclient -A masteradmin -P secret --id 5**

Get an OAuthClient

# SEE ALSO

[createoauthclient(1)](createoauthclient), [disableoauthclient(1)](disableoauthclient), [enableoauthclient(1)](enableoauthclient), [listoauthclient(1)](listoauthclient), [removeoauthclient(1)](removeoauthclient), [revokeoauthclient(1)](revokeoauthclient), [updateoauthclient(1)](updateoauthclient)