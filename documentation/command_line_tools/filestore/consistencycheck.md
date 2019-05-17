---
title: checkconsistency
icon: far fa-circle
tags: Administration, Command Line tools, Filestore
---

# NAME

checkconsistency - checks the filestore consistency.

# SYNOPSIS

**checkconsistency** [OPTION...]

# DESCRIPTION

This command line tool performs a consistency check for file entries on the filestore.

# OPTIONS

**-a**, **--action** *action*
: Defines the action. Accepted values are:

      - "list_unassigned":  Lists names of orphaned files held in file storage
      - "list_missing": Lists names of files that are still referenced, but do no more exist in actual file storage
      - "repair": Repairs either orphaned files or references to non-existing files according to specified "--policy" and associated "--policy-action"
      - "repair_configdb": Deletes artefacts of non-existing contexts from config database. Requires no further options.
      - "check_configdb": Checks for artefacts of non-existing contexts in config database. Requires no further options.

**-o**, **--source** *source*
: Defines the source that is going to be used. Only considered if "--action" option specifies either "list_missing", "list_unassigned" or "repair". Accepted values are: 

      - "database": Considers all files of all contexts that belong to a certain database's schema.
      - "context": Considers all files of a certain context.
      - "filestore": Considers all files of a certain file store.
      - "all": Considers all files; no matter to what context and/or file store a file belongs (the "--source-id" option is ignored).
  
    All values (except `all`) are used in conjunction with the `-i` option which defines the database, context and filestore identifiers respectively. See the EXAMPLES section for more detailed examples.

**-i**, **--source-id** *sourceId*
: Defines the source identifier. Only considered if "--source" option is specified. If "--source" is set to "all" then this option is simply ignored.

**-r**, **--policy** *policy*
: Defines the 'repair' policy. Only considered if "--action" option specifies "repair". Available repair policies are: "missing_entry_for_file", "missing_file_for_attachment", "missing_file_for_infoitem", "missing_file_for_snippet","missing_file_for_vcard", and "missing_attachment_file_for_mail_compose". Those prepair policies define on *how* an inconcistency shall be resolved. The action regarding *what* to perform with the chosen policy is defined with the `-y` option.

**-y**, **--policy-action** *policyAction* 
: Defines an action for the desired repair policy. Only considered if "--policy" option is specified. Available actions are: "delete", "create_dummy", "create_admin_infoitem", which deletes, creates a dummy entry and creates an admin info item for the missing items respectively.

**-A**, **--adminuser** *masterAdminUser*
: Master admin user name for authentication.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication.

**-s**, **--server** *rmiHost*
: The optional RMI server (default: localhost)

**-p**, **--port** *rmiPort*
: The optional RMI port (default:1099)

**-h**, **--help**
: Prints a help text.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

# EXAMPLES

**checkconsistency -A masteradmin -P secret -a list_unassigned**

Lists names of orphaned files held in file storage

**checkconsistency -A masteradmin -P secret -a list_missing**

Lists names of files that are still referenced, but do no more exist in actual file storage

**checkconsistency -A masteradmin -P secret -a repair**

Repairs either orphaned files or references to non-existing files according to specified "--policy" and associated "--policy-action"

**checkconsistency -A masteradmin -P secret -a repair_configdb**

Deletes artefacts of non-existing contexts from config database. Requires no further options.

**checkconsistency -A masteradmin -P secret -a check_configdb"**

Checks for artefacts of non-existing contexts in config database. Requires no further options.

The following examples are only considered for actions "list_missing", "list_unassigned" or "repair".

**checkconsistency -A masteradmin -P secret -a list_missing -o context -i 1138**

Lists all missing files in a certain context.

**checkconsistency -A masteradmin -P secret -a list_unassigned -o filestore -i 1337**

Lists all unassigned files in a certain filestore.

**checkconsistency -A masteradmin -P secret -a list_missing -o database -i 1618**

Lists all missing files in a certain database.

**checkconsistency -A masteradmin -P secret -a list_unassigned -o all"**

Lists all unassigned files no matter to what context and/or file store a file belongs (the "--source-id" option is ignored).

If the "repair" action is used then a "policy" MUST also be specified.

**checkconsistency -A masteradmin -P secret -a repair -o context -i 1138 -r missing_entry_for_file -y create_admin_infoitem"**

Creates a dummy Drive entry named "Restoredfile" and associates it with each missing file.

**checkconsistency -A masteradmin -P secret -a repair -o filestore -i 1337 -r missing_entry_for_file -y delete**

Simply deletes the orphanded file from storage.

**checkconsistency -A masteradmin -P secret -a repair -o database -i 1618 -r missing_file_for_infoitem -y create_dummy**

Creates a dummy file in storage and associates it with the Drive item

**checkconsistency -A masteradmin -P secret -a repair -o context -i 1138 -r missing_file_for_infoitem -y delete**

Simply deletes the Drive item pointing to a non-existing file

**checkconsistency -A masteradmin -P secret -a repair -o filestore -i 1337 -r missing_file_for_attachment -y create_dummy**

Creates a dummy file in storage and associates it with the attachment item

**checkconsistency -A masteradmin -P secret -a repair -o database -i 1618 -r missing_file_for_attachment -y delete**

Simply deletes the attachment item pointing to a non-existing file

**checkconsistency -A masteradmin -P secret -a repair -o context -i 1138 -r missing_file_for_snippet -y create_dummy**

Creates a dummy file in storage and associates it with the snippet item

**checkconsistency -A masteradmin -P secret -a repair -o filestore -i 1337 -r missing_file_for_snippet -y delete**

Simply deletes the snippet item pointing to a non-existing file

**checkconsistency -A masteradmin -P secret -a repair -o database -i 1618 -r missing_attachment_file_for_mail_compose -y create_dummy**

Creates a dummy file in storage and associates it with the mail compose attachment item

**checkconsistency -A masteradmin -P secret -a repair -o context -i 1138 -r missing_attachment_file_for_mail_compose -y delete**

Simply deletes the mail compose attachment item pointing to a non-existing file

**checkconsistency -A masteradmin -P secret -a repair -o database -i 1618 -r missing_file_for_vcard -y create_dummy**

Creates a dummy file in storage and associates it with the vcard item

**checkconsistency -A masteradmin -P secret -a repair -o filestore -i 1337 -r missing_file_for_vcard -y delete**

Simply deletes the vcard item pointing to a non-existing file

# SEE ALSO

[registerfilestore(1)](registerfilestore), [unregisterfilestore(1)](unregisterfilestore), [changefilestore(1)](changefilestore), [recalculatefilestoreusage(1)](recalculatefilestoreusage)