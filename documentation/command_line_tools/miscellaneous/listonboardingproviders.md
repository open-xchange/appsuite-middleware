---
title: listonboardingproviders
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-client-onboarding
---

# NAME

listonboardingproviders - delete orphaned references

# SYNOPSIS

**listonboardingproviders** [OPTIONS] | [-h]

# DESCRIPTION

The command-line tool for listing available on-boarding providers

# OPTIONS

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