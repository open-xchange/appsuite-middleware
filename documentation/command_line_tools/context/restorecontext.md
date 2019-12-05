---
title: restorecontext
icon: far fa-circle
tags: Administration, Command Line tools, User
---

# NAME

restorecontext - Restores a deleted context from a backup.

# SYNOPSIS

**restorecontext** [OPTION]...

# DESCRIPTION

This command line tool restores a deleted context from a backup. Note: restorecontext will only restore data from the database, not from the filestore. During the restore, the context and the filestore content will be deleted, so make a backup first or if you deleted an context by accident you have to restore the filestore content after restorecontext.

# OPTIONS

**-h**, **--help**
: Prints a help text

**-c**, **--contextid** *contextId*
: The id of the context

**-A**, **--adminuser** *adminuser*
: The name of the admin
              
 **-P**, **--adminpass** *adminpass*
: The admin password              
          
**--environment**
: Show info about commandline environment

**--nonl**
: Remove all newlines (\n) from output

**--responsetimeout** *responsetimeout*
: The response timeout in seconds for reading response from the backend (default 0s; infinite)

 **-f**, **--filename** *filename*
: Comma-separated list of filenames with full path
 
 **-n**, **--dry-run**
: Activate this option if do not want to apply the changes to the database

# OPTIONAL

**-d**, **--configdb** *configdb*
:  The name of the ConfigDB schema. If not set, ConfigDB name is determined by "writeUrl" property in file configdb.properties

# EXAMPLES

**restorecontext -A oxadminmaster -P secret -c 1 -f /path/to/configdb.sql,/path/to/userdb.sql,/path/to/userdb1.sql,...**


# SEE ALSO

[deletecontext(1)](deletecontext), [listcontext(1)](listcontext), [createcontext(1)](createcontext), [enablecontext(1)](enablecontext), [disablecontext(1)](disablecontext), [disableallcontexts(1)](disableallcontexts), [enableallcontexts(1)](enableallcontexts), [getcontextcapabilities(1)](getcontextcapabilities)