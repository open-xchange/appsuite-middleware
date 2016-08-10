# Filestore Documentation

This document provides an overview about filestores and how they can be configured. It also answers a majority of frequently asked questions.
First, there is a brief overview about how to use filestore. Then mentionable properties and command line tools will be listed and described.
Finally some remaining open questions will be answered.

## How to use a filestore

In order to use a filestore it must first be registered at the server. Filestores are registered via the registerfilestore CLT. E.g.:

    registerfilestore -A masteradmin -P masterpassword -t file:/tmp/filestore -s 1024 -x 1024


Beside local- or NFS-filesystem-based filestores, the OX also supports other filestores like S3 based cloud storage filestores.
Each filestore can be assigned to multiple contexts and/or users. For each assignment an individual quota can be configured.
These assignment are done via the create and update CLT's of the context and user. In addition there are CLT's which moves the data from one filestore to another.
See [Command Line Tools](# Command Line Tools) for more informations.

If a filestore is assigned to a context, all user within this context which doesn't own an individual filestore share this filestore and its quota.
If a filestore is assigned to a user, the user has an own filestore (aka user filestore) with an own quota. It is also possible, that a user shares his filestore with
other users. In this case the first user is the master user of the filestore and other users are slave users. They share the quota of the master user.

## Properties

In this section all relevant properties or property files are listed which affect filestores.

Property files:

* filestore-s3.properties - Properties concerning s3 filestorage
* filestore-sproxyd.properties - Properties concerning sproxyd filestorage
* filestore-swift.properties - Properties concerning swift filestorage
* caching.ccf - Contains some properties concerning the caching for filestores


Properties:

* com.openexchange.capability.filestore - Signal if file store is available or not
* ALLOW_CHANGING_QUOTA_IF_NO_FILESTORE_SET - Allows to change the quota of filestorages in case no own filestore is set


## Command Line Tools

This section defines a list of CLT's and their descriptions which are useful in context of filestores.
To get further informations about a specific CLT run it with the --help option.

|Name | Description|
| --|--|
| registerfilestore | Allows to register a filestore. |
| unregisterfilestore | Allows to remove a filestore. |
| movecontextfilestore | Moves all data of a context to another filestore and assign this new filestore to the context. |
| movecontextfilestore2user | Moves the data of a single user which does not have an own filestore to another filestore and assign this filestore to the user. |
| moveuserfilestore | Moves the data of a user from his filestore to another. |
| moveuserfilestore2Master |  Moves the data of a user from his filestore to the filestore of another user. |
| moveuserfilestore2Context |  Moves the data of a user from his filestore to the context filestore. |
| listfilestores | Lists all registered filestores |
| listuserfilestores | Lists user filestores for all users in a context or list all users for a single filestore |


## FAQ


* When is a user filestore enabled or when did a user has a own filestore?

  A user filestore has an own filestore if a filestore is assigned to this user.
  This can either be done via the create and update clt's or via the movecontextfilestore2user clt.


* When is which quota affected?

  In case the user has no own filestore the quota of the context filestore is affected.
  In case the user has an own filestore or in case the user is a slave user of another users filestore the quota of this user filestore is affected.

* How can a user check his quota usage?

   The 'Quota' portal widget shows quota and usage.

* How a user can find out what or who waste his quota?

  Currently it is not possible to identify what wastes the quota.

* How to change the filestore quota shown in the portal widget?

  To change the quota one just has to use the changeuser or changecontext CLT with the --quota (-q) argument. E.g.:

      changecontext -A adminmaster -P masterpassword -c <cid> -q 1024


* How does quota work for public files?

   The quota of the folder creator/owner is in charge for files other users put inside. This
   affects the quota for users having an own filestore


* What happens with ownership of public files and folders when the creator/owner gets deleted?

   This depends which argument is used during the delete operation. In the default case, the context admin is assigned as the owner.
   But it is also possible that the data is assigned to a different user or that the data is completed removed.

* What happens in over quota scenarios by deleting users?

  If the data is assigned to a different user the quota limit is ignored.

* How to deal with quota issues for the context admin caused by public files?

  If the quota is too limited for the context admin the data should be completely removed or assigned to a different user. E.g.:

      deleteuser -A ctxadmin -P password -c <cid> -i <uid> --no-reassign
