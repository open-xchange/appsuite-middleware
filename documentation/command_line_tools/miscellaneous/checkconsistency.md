---
title: checkconsistency
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

checkconsistency - checks database/filestore consistency. 

# SYNOPSIS

**checkconsistency** [OPTION]...

# DESCRIPTION

This command line tool checks for missing files in the filestore, which are referenced in the database and also for orphaned files in the filestore, which have no reference in the database.

There are also options to repair these entries by either creating dummies or removing the entries.

# OPTIONS

**-h**, **--help**
: Prints a help text.

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-a**, **--action**
: Defines the action. Accepted values are: 'list_unassigned', 'list_missing', 'repair', 'rapir_configdb', 'check_configdb'.

**-o**, **--source**
: Defines the source that is going to be used. Only considered if --action option specifies either 'list_missing', 'list_unassigned or 'repair'. Accepted values are: 'database', 'context', 'filestore', 'all'.

**-r**, **--policy**
: Defines the 'repair' policy. Only considered if --action option specifies 'repair'. Available repair policies are: 'missing_entry_for_file', 'missing_file_for_attachment', 'missing_file_for_infoitem', 'missing_file_for_snippet', 'missing_file_for_vcard', 'missing_attachment_file_for_mail_compose', 'missing_file_for_preview'.

**-y**, **--policy-action**
: Defines an action for the desired repair policy. Only considered if --policy option is specified.

**-i**, **--source-id**
: Defines the source identifier. Only considered if --source option is specified. If --source is set to 'all' then this option is simply ignored.

# EXAMPLES

1. -a,--action:
====================================

- "-a list_unassigned"
Lists names of orphaned files held in file storage
- "-a list_missing"
Lists names of files that are still referenced, but do no more exist in actual file storage
- "-a repair"
Repairs either orphaned files or references to non-existing files according to specified "--policy" and associated
"--policy-action"
- "-a repair_configdb"
Deletes artefacts of non-existing contexts from config database. Requires no further options.
- "-a check_configdb"
Checks for artefacts of non-existing contexts in config database. Requires no further options.

2. -o,--source / -i,--source-id:
====================================
Only considered for actions "list_missing", "list_unassigned" or "repair"
Possible combinations:
- "-o context -i <context-id>"
Considers all files of a certain context
- "-o filestore -i <filestore-id>"
Considers all files of a certain file store
- "-o database -i <database-id>"
Considers all files of all contexts that belong to a certain database's schema
- "-o all"
Considers all files; no matter to what context and/or file store a file belongs (the "--source-id" option is ignored)

3. -r,--policy / -y,--policy-action:
====================================
Only considered for action "repair"
Possible combinations:
- "-r missing_entry_for_file -y create_admin_infoitem"
Creates a dummy Drive entry named "Restoredfile" and associates it with the file
- "-r missing_entry_for_file -y delete"
Simply deletes the orphanded file from storage
- "-r missing_file_for_infoitem -y create_dummy"
Creates a dummy file in storage and associates it with the Drive item
- "-r missing_file_for_infoitem -y delete"
Simply deletes the Drive item pointing to a non-existing file
- "-r missing_file_for_attachment -y create_dummy"
Creates a dummy file in storage and associates it with the attachment item
- "-r missing_file_for_attachment -y delete"
Simply deletes the attachment item pointing to a non-existing file
- "-r missing_file_for_snippet -y create_dummy"
Creates a dummy file in storage and associates it with the snippet item
- "-r missing_file_for_snippet -y delete"
Simply deletes the snippet item pointing to a non-existing file
- "-r missing_attachment_file_for_mail_compose -y create_dummy"
Creates a dummy file in storage and associates it with the mail compose attachment item
- "-r missing_attachment_file_for_mail_compose -y delete"
Simply deletes the mail compose attachment item pointing to a non-existing file
- "-r missing_file_for_vcard -y create_dummy"
Creates a dummy file in storage and associates it with the vcard item
- "-r missing_file_for_vcard -y delete"
Simply deletes the vcard item pointing to a non-existing file
- "-r missing_file_for_preview -y delete"
Simply deletes the preview item pointing to a non-existing file