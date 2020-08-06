---
title: deleteExternalAccounts
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-admin
---

# NAME

listExternalAccounts - deletes external accounts

# SYNOPSIS

**deleteExternalAccounts** [-h|--help]

**deleteExternalAccounts** [-c *contextId*] [-u *userId*] [-m *module*] [-i *accountId*] -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*] [--responsetimeout *responseTimeout*]

# DESCRIPTION

This command line tool deletes external accounts. If an OAuth account is selected for deletion (`-m OAUTH`), then ALL external accounts tied to that OAuth account will also be deleted.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier. Mandatory.

**-u**, **--user** *userId*
: The user identifier. Mandatory.

**-m**, **--module** *module*
: The module identifier. Valid modules are: OAUTH, CONTACTS, CALENDAR, DRIVE, MAIL. Mandatory.

**-i**, **--accountId** *accountId*
: The account identifier. Mandatory.

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

**deleteExternalAccounts -A masterAdmin -P secret -c 1138 -u 314 -i 5 -m OAUTH**

Deletes for the specified user in the specified context the specified OAUTH account and all external accounts that are tied to it.

**deleteExternalAccounts -A masterAdmin -P secret -c 1138 -u 314 -i 16 -m DRIVE**

Deletes the specified DRIVE account of the specified user.