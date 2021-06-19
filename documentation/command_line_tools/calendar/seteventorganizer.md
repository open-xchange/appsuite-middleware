---
title: seteventorganizer
icon: far fa-circle
tags: Administration, Command Line tools, Calendar
package: open-xchange-core
---

# NAME

seteventorganizer - sets a new organizer for the given event in the given context.

# SYNOPSIS

**seteventorganizer** -c *contextId* -e *eventId* -A *masterAdmin* -P *masterAdminPassword* [-p *RMI-Port*] [-s *RMI-Server*] [--responsetimeout *responseTimeout*] | [-h]

# DESCRIPTION

This command line tool sets a new organizer for the given event in the given context. 
If this is performed on a recurring event (master or exception), all exceptions and the master are changed. The new organizer must be an internal user and the old organizer must not be an external user.
If the organizer is no attendee, the organizer will automatically be added as attendee.
If the organizer is already set but not yet an attendee, the organizer will be added as attendee as well.
If the organizer is already set and also an attendee this is a no-op.
Bear in mind, that external users/clients may not support organizer changes, thus this operation is not propagated to
external attendees.

# OPTIONS

**-c**, **--context** *contextId*
: The context identifier.

**-e**, **--event** *eventId*
: Required. The event identifier

**-u**, **--userId** *userId*
: Required. The user identifier to set as replacement

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text

**-s**,**--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**,**--port** *rmiPort*
: The optional RMI port (default:1099)

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

