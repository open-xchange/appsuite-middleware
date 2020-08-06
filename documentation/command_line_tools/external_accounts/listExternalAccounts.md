---
title: listExternalAccounts
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin
---

# NAME

listExternalAccounts - lists all external accounts context-wide

# SYNOPSIS

**listExternalAccounts** [-h|--help]

**listExternalAccounts** [-c *contextId*] [-u *userId*] [-m *module*] [-r *providerId*] -A *masterAdminUser* -P *masterAdminPassword*  [-p *RMI-Port*] [-s *RMI-Server*] [--responsetimeout *responseTimeout*]

# DESCRIPTION

This command line tool lists all external accounts for a specific context (`-c`) and/or a specific user (`-u`). The accounts can be filtered with the `-m` and/or `-r` options by module and provider respectively. All four options can be used in combination to narrow down the list to a specific user in a specific context and list all accounts of a specific module and provider.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier. Mandatory

**-u**, **--user** *userId*
: The user identifier.

**-m**, **--module** *module*
: The module identifier. Valid modules are: OAUTH, CONTACTS, CALENDAR, DRIVE, MAIL

**-r**, **--provider** *provider*
: The provider identifier.

**-h**, **--help**
: Prints a help text

**-A**, **--adminuser** *admin*
: The master or context admin user name for authentication.

**-P**, **--adminpass** *adminPassword*
: The master admin or context admin password for authentication.

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**listExternalAccounts -A masterAdmin -P secret -c 1138**

Listing all external accounts for a specific context.

**listExternalAccounts -A masterAdmin -P secret -c 1138 -u 314**

Listing all external accounts for a specific user in a specific context.

**listExternalAccounts -A masterAdmin -P secret -c 1138 -u 314 -r com.openexchange.oauth.google**

Listing all external accounts for a specific user in a specific context for the specified provider.

**listExternalAccounts -A masterAdmin -P secret -c 1138 -u 314 -r com.openexchange.oauth.google -m MAIL**

Listing all external accounts for a specific user in a specific context for the specified provider and a specific module.