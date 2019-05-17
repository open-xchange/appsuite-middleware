---
title: changeaccessglobal
icon: far fa-circle
tags: Administration, Command Line tools, User
---

# NAME

changeaccessglobal - changes module Permissions for all (!) users in all (!) contexts.

# SYNOPSIS

**changeaccessglobal** [OPTION]...

# DESCRIPTION

This command line tool changes module Permissions for all (!) users in all (!) contexts. This can be filtered by already existing access combinations. If no filter is given, all users are changed.

# OPTIONS

**-f**, *--filter* *filter*
: The call will only affect users with this access combination. Can be an Integer or a String, representing a module access definition. If left out, all users will be changed.

**-a**, **--access-combination-name** *access-combination-name*
: The optional access combination name as replacement for specifying single permissions to enable/disable. A value for "access-global-address-book-disabled" will be ignored.

**--extendedoptions**
: Set this if you want to see all options, use this instead of help option.

**-A**, **--adminuser** *contextAdmin*
: Context admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *contextAdminPassword*
: Context admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXTENDED OPTIONS

**--access-calendar** *<on/off>*
: Calendar module (Default is off)

**--access-contacts** *<on/off>*
: Contact module access (Default is on)

**--access-delegate-tasks** *<on/off>*
: Delegate tasks access (Default is off)

**--access-edit-public-folder** *<on/off>*
: Edit public folder access (Default is off)

**--access-ical** *<on/off>*
: Ical module access (Default is off)

**--access-infostore** *<on/off>*
: Infostore module access (Default is off)

**--access-read-create-shared-Folders** *<on/off>*
: Read create shared folder access (Default is off)

**--access-syncml** *<on/off>*
: Syncml access (Default is off)

**--access-tasks** *<on/off>*
: Tasks access (Default is off)

**--access-vcard** *<on/off>*
: Vcard access (Default is off)

**--access-webdav** *<on/off>*
: Webdav access (Default is off)

**--access-webdav-xml** *<on/off>*
: Webdav-Xml access (Default is off) [DEPRECATED]

**--access-webmail** *<on/off>*
: Webmail access (Default is on)

**--access-edit-group** *<on/off>*
: Edit Group access (Default is off)

**--access-edit-resource** *<on/off>*
: Edit Resource access (Default is off)

**--access-edit-password** *<on/off>*
: Edit Password access (Default is off)

**--access-collect-email-addresses** *<on/off>*
: Collect Email Addresses access (Default is off)

**--access-multiple-mail-accounts** *<on/off>*
: Multiple Mail Accounts access (Default is off)

**--access-subscription** *<on/off>*
: Subscription access (Default is off)

**--access-publication** *<on/off>*
: Publication access (Default is off) [DEPRECATED]

**--access-active-sync** *<on/off>*
: Exchange Active Sync access (Default is off)

**--access-usm** *<on/off>*
: Universal Sync access (Default is off)

**--access-olox20** *<on/off>*
: OLOX v2.0 access (Default is off) [DEPRECATED]

**--access-denied-portal** *<on/off>*
: Denies portal access (Default is off)

**--access-public-folder-editable** *<on/off>*
: Whether public folder(s) is/are editable (Default is off). Applies only to context admin user.

# EXAMPLES

**changeaccessglobal -A contextAdmin -P secret --access-calendar off**

Changes the global access of calendar for all users in all contexts.

# SEE ALSO

[createuser(1)](createuser), [listuser(1)](listuser), [changeuser(1)](changeuser), [getusercapabilities(1)](getusercapabilities)