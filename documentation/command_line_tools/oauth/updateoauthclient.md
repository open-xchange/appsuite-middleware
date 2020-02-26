---
title: updateoauthclient
icon: far fa-circle
tags: Administration, Command Line tools, OAuth
package: open-xchange-admin-oauth-provider
---

# NAME

updateoauthclient - updates an OAuth client

# SYNOPSIS

**updateoauthclient** [OPTIONS]

# DESCRIPTION

This command line tool updates an existing OAuth client

# OPTIONS

**-x*, **--context-group-id** *cgid*
: The id of the context group
 
**-n**, **--name** *name*
: Define the name of the oauth client

**-d**, **--description** *description*
: The description of the oauth client

**-w**, **--website** *website*
: The client website
          
**-o**, **--contact-address** *contactAddress**
: The contact adress of the oauth client

**-i**, **--icon-path** *iconPath*
: Path to a image file which acts as a icon for the oauth client

**-s**, **--default-scope** *defaultScope*
: The default scope of the oauth client

**--urls** *urls*
: The redirect urls of the oauth client as a comma separated list

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

**updateoauthclient -A masteradmin -P secret -x 10 --urls https://example.org, https://example.com**

Updates an OAuthClient

# SEE ALSO

[createoauthclient(1)](createoauthclient), [disableoauthclient(1)](disableoauthclient), [enableoauthclient(1)](enableoauthclient), [getoauthclient(1)](getoauthclient), [listoauthclient(1)](listoauthclient), [removeoauthclient(1)](removeoauthclient), [revokeoauthclient(1)](revokeoauthclient)