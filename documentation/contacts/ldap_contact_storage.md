---
title: LDAP Contact Storage
icon: fas fa-users
tags: LDAP, Contacts, Administration
---

This article describes how an existing LDAP (Lightweight Directory Access Protocol) directory can be integrated as a contact storage backend for the Open-Xchange server.

# Overview

In enterprise installations, there's often a central directory service that hosts the user data or other addressbook related entries. The Open-Xchange Server can be configured to access such data available from LDAP directories and integrate it in terms of contact folders in the groupware. For end-users, there's no difference to other contact folders, so that they can access the contents from the LDAP directory in a transparent way, e.g. when looking up participants for an appointment or choosing recipients for an e-mail message. 

Internally, a LDAP directory is configured as a read-only subfolder in the public folders tree that is automatically kept in sync with the directory via the contact-storage-ldap bundle that registers as an additional contact storage backend in the Open-Xchange Server. Changes made in the directory are reflected automatically in the groupware folder, so in contrast to the [OX LDAP Sync script](https://www.oxpedia.org/wiki/index.php?title=OXLDAPSync_Guide), this is a contact storage plugin for the server that works along with other contact storage providers such as the default database storage.

# Requirements

* Open-Xchange Server v6.22.1 and above (``open-xchange-core``, ``open-xchange-contact-storage-ldap``)
* An LDAP compatible directory server (e.g. OpenLDAP, Microsoft Active Directory)

# Configuration

The following steps provide a walkthrough to configure access to the LDAP directory. The example describes the configuration of a folder that is backed by an Active Directory in a Microsoft Windows Server 2008 / Microsoft Exchange 2010 environment, but the steps should be similar for any other LDAP directory. 

## Install required Bundles

If not yet done, install the following additional bundle on the server: ``open-xchange-contact-storage-ldap``.

## Configure the LDAP Folder

The bundle ``open-xchange-contact-storage-ldap`` is responsible for publishing contact information from the LDAP directory in a contact folder of the server. For each folder that should be served with contents of the LDAP directory, a separate configuration is necessary in a separate configuration file. After installation, a template for a folder configuration file can be found at ``folder.properties.example``. To start the configuration, copy this file to a .properties file with a name of your choice, e.g. ``e2k10.properties``, open the file in a text editor, and adjust the settings as needed. Please refer to the included property comments for details. The following example shows an example configuration for the aforementioned Microsoft Windows Server 2008 / Exchange 2010 environment:

```properties
 # Properties file for a LDAP folder. 
 
 
 # == Folder Properties =======================================================
 
 # The context ID where the LDAP folder resides in. Required.
 com.openexchange.contact.storage.ldap.contextID=1
 
 # The ID of the folder. Either the 'foldername' or the 'folderID' property must 
 # be defined. If set, the folder ID must refer to an existing folder in the 
 # context, which is then used by the storage.   
 com.openexchange.contact.storage.ldap.folderID=
 
 # Defines the display name of the folder. Either the 'foldername' or the 
 # 'folderID' property must be defined. If set, the folder name is bound to 
 # a folder below the system folder "Public folders" (ID 2), and is implicitly
 # created if it not yet exists, impersonating as the context administrator, 
 # allowing 'read-only' kind of permissions for all users.
 com.openexchange.contact.storage.ldap.foldername=Exchange 2010
 
 # The priority of the contact storage that is being registered in the server. 
 # Any integer > 0 should be okay to override the default storage when 
 # requesting the exposed LDAP folder. Defaults to 17.
 com.openexchange.contact.storage.ldap.storagePriority=17
 
 
 # == LDAP Attribute mapping ==================================================
 
 # Controls the way object identifiers are mapped between OX and LDAP. Possible
 # values are 'static' and 'persistent'. A 'static' mapping requires numerical 
 # unique identifiers in LDAP, i.e. all mapped ID properties defined in the 
 # mapping file point to an integer-parsable LDAP attribute. If the LDAP server
 # does not provide unique numerical properties, a 'persistent' ID mapping must
 # be configured that stores the mappings inside a mapping table in the 
 # database. Required.
 com.openexchange.contact.storage.ldap.idMapping=persistent
 
 # The filename containing the OX <-> LDAP attribute mappings for contacts. 
 # Required. Multiple LDAP folders may share the same mapping properties file. 
 com.openexchange.contact.storage.ldap.mappingFile=mapping.e2k10.properties
 
 
 # == Cache ===================================================================
 
 # Configures the internal cache refresh interval in milliseconds. Contact 
 # caching requires an 'authtype' of either 'admindn' or 'anonymous', along 
 # with an 'idMapping' of either 'persistent' or 'static' and a properly 
 # configured cache via 'cacheConfigFile'. A refresh interval of '0' disables
 # the cache. Defaults to '100000'.
 com.openexchange.contact.storage.ldap.refreshinterval=100000
 
 # The filename containing the cache configuration. Required if caching is 
 # enabled via 'refreshinterval'. Multiple LDAP folders may share the same 
 # cache properties file.
 com.openexchange.contact.storage.ldap.cacheConfigFile=cache.properties
 
 
 # == LDAP Server =============================================================
 
 # The URI of the LDAP server, should be in the format 
 # ldap://myserver.example.com:389. For SSL access, use the ldaps protocol and 
 # the appropriate port, e.g. ldaps://myserver.example.com:636. Required.
 com.openexchange.contact.storage.ldap.uri=ldap://e2k10.example.com:389
 
 # Configures a base LDAP path. If defined, all Distinguished Names supplied to 
 # and received from LDAP operations will be relative to the LDAP path 
 # supplied. If not defined, the default naming context of the RootDSE is used 
 # as baseDN. 
 com.openexchange.contact.storage.ldap.baseDN=DC=e2k10,DC=example,DC=com
 
 # Specifies a general search filter that is applied to each LDAP search, e.g. 
 # to restrict the search to one or more object classes. Required.
 com.openexchange.contact.storage.ldap.searchfilter=(|(objectclass=group)(objectclass=user))
 
 # For testing purposes, it's possible to skip certificate validation and trust 
 # all server certificates. Possible values are 'true' and 'false'; defaults to
 # 'false'.  
 com.openexchange.contact.storage.ldap.trustAllCerts=false
 
 
 # == Authentication ==========================================================
 
 # Configures the authentication type when accessing the LDAP server. Possible 
 # values are 'anonymous', 'admindn' or 'user'. Setting the 'authtype' to 
 # 'admindn' will always impersonate using the properties in 'AdminDN' and 
 # 'AdminBindPW', while 'user' will always use the credentials from the current
 # session's user, where the user's distinguished name is dynamically looked up 
 # as configured via 'userLoginSource', 'userSearchAttribute' and following. 
 # Required. 
 com.openexchange.contact.storage.ldap.authtype=AdminDN
 
 # Specify the admin bind DN here. Required if 'authtype' is set to 'AdminDN', 
 # or if 'authtype' is set to 'user' and 'userAuthType' to 'AdminDN'. 
 com.openexchange.contact.storage.ldap.AdminDN=cn=administrator,cn=users,DC=e2k10,DC=example,DC=com
 
 # Specify the admin bind password here. Required if 'authtype' is set to 
 # 'AdminDN', or if 'authtype' is set to 'user' and 'userAuthType' to 'AdminDN'.
 com.openexchange.contact.storage.ldap.AdminBindPW=secret
 
 # Configures the format of the username that is used during dynamic lookups 
 # for the corresponding user on the LDAP server. Required if 'authtype' is set 
 # to 'user'. Possible values are 'login' (the user's individual mail login as 
 # defined in user storage), 'mail' (the user's individual primary email 
 # address) or 'name' (the user's individual system user name).
 com.openexchange.contact.storage.ldap.userLoginSource=
 
 # Configures the LDAP attribute name that matches the username as defined by 
 # 'userLoginSource' during dynamic lookups. Required if 'authtype' is set to 
 # 'user'.
 com.openexchange.contact.storage.ldap.userSearchAttribute=
 
 # Specify the authentication type when that is used during user lookup here. 
 # Required if 'authtype' is set to 'user'. Possible values are 'anonymous' and 
 # 'admindn', while the latter one will use the credentials defined in 
 # 'AdminDN' and 'AdminBindPW'.
 com.openexchange.contact.storage.ldap.userAuthType=
 
 # Configures the search scope that is used during user lookup. Possible values 
 # are 'sub', 'base' and 'one'. If left empty, the general 'searchScope' is 
 # used.
 com.openexchange.contact.storage.ldap.userSearchScope=
 
 # Configures the base LDAP path that is used during user lookup. If left empty, 
 # the general 'baseDN' is used.
 com.openexchange.contact.storage.ldap.userSearchBaseDN=
 
 # Configures the search filter that is used during user lookup. If left empty, 
 # the general 'searchfilter' is used.
 com.openexchange.contact.storage.ldap.userSearchFilter=
 
 
 # == Advanced LDAP configuration =============================================
 
 # Configures how deep to search within the search base. Possible values are 
 # 'sub' (entire subtree), 'base' (base object only) or 'one' (one level 
 # subordinate to the base object). Defaults to 'sub'.
 com.openexchange.contact.storage.ldap.searchScope=sub
 
 # Specifies how to handle referrals. Possible values are 'follow' to follow,
 # 'ignore' to ignore referrals or 'standard' to use the default JNDI setting. 
 # Defaults to 'follow'.
 com.openexchange.contact.storage.ldap.referrals=follow
 
 # Defines whether LDAP connection pooling is enabled or not. Possible values 
 # are 'true' and 'false'; defaults to 'false'.  
 com.openexchange.contact.storage.ldap.connectionPooling=true
 
 # If connection pooling is enabled, this configures the timeout when waiting 
 # to access a pooled connection. Optional. 
 com.openexchange.contact.storage.ldap.pooltimeout=
 
 # Controls how to dereference aliases. Possible values are 'always' to always,
 # 'never' to never dereference aliases, 'finding' to dereference during name
 # or 'searching' to dereference after name resolution. Defaults to 'always'.
 com.openexchange.contact.storage.ldap.derefAliases=always
 
 # Specifies the requested size for paged results. '0' disables paged results. 
 # Defaults to '500'.
 com.openexchange.contact.storage.ldap.pagesize=500
 
 # Configures where the search results should be sorted. Possible values are 
 # 'server' (sorting by the LDAP server) or 'groupware' (sorting by the 
 # groupware server). Defaults to 'groupware'.
 com.openexchange.contact.storage.ldap.sorting=server
 
 # Specifies if support for querying deleted objects is enabled or not. When 
 # enabled, deleted objects are identified with the filter 'isDeleted=TRUE',
 # which is only available in Active Directory. There, information about 
 # deleted objects are available for the following lifetimes:
 # 60 days for forests initially built using W2k and Server 2k3
 # 180 days for forests that were initially built with Server 2k3 SP1
 # Possible values are 'true' and 'false', defaults to 'false'. When disabled,
 # no results are available for this folder for the 'deleted' API call. 
 # Therefore, any synchronization-based usage will not be available.
 com.openexchange.contact.storage.ldap.ADS_deletion_support=true
 
 # Defines whether empty distribution lists, i.e. lists that do not contain any
 # member, are excluded or not. Possible values are 'true' and 'false'; 
 # defaults to 'true'.  
 com.openexchange.contact.storage.ldap.excludeEmptyLists=true
```

## Configure Mappings

Besides the configuration of the folder and how to access the LDAP directory, a mapping file is used to define how LDAP entries can be converted to groupware contacts and distribution lists. After installation, templates for mapping files can be found at "mappings.ads.properties.example" and "mappings.openldap.properties.example". To start the configuration, copy such a template file to the .properties file named as configured via ``com.openexchange.contact.storage.ldap.mappingFile`` in the previous step, e.g. ``mapping.e2k10.properties``. Then, open the file in a text editor, and adjust the attribute mappings as needed. Please refer to the included comments for details. The following example shows an example configuration for the aforementioned Microsoft Windows Server 2008 / Exchange 2010 environment:

```properties
 # Mapping between attributes of OX contacts and LDAP objects
 
 # Contact properties are set based on the mapped LDAP attribute name. Empty 
 # mappings are ignored. It's possible to define a second LDAP attribute name 
 # for a property that is used as fall-back if the first one is empty in a 
 # LDAP result, e.g. to define multiple attributes for a display name, or to 
 # have multiple mappings for contacts and distribution lists.  
 
 # For the data-types, each LDAP attribute value is converted/parsed to the type 
 # necessary on the server (Strings, Numbers, Booleans). Dates are assumed to
 # be in UTC and parsed using the pattern 'yyyyMMddHHmmss'. Binary properties
 # may be indicated by appending ';binary' to the LDAP attribute name. Boolean
 # properties may also be set based on a comparison with the LDAP attribute 
 # value, which is defined by the syntax '[LDAP_ATTRIBUTE_NAME]=
 # [EXPECTED_VALUE]', e.g. to set the 'mark_as_distribution_list' property 
 # based on a specific 'objectClass' value.  
 
 
 # == ID Mappings =============================================================
 
 # Numerical IDs are used by the groupware server to identify an object or 
 # user, and therefore should be mapped to an adequate LDAP unique property of
 # the LDAP server. If no numerical identifiers are provided by the LDAP server, 
 # a mapping between the LDAP attribute values and groupware IDs is applied in 
 # an additional step (as configured by the 'idMapping' in the folder's 
 # properties). Binary LDAP attributes are tried to be interpreted as 16-byte 
 # GUIDs. 
 
 # The object ID is always required and must be unique for the LDAP server 
 com.openexchange.contact.storage.ldap.objectid    = objectGUID;binary
 
 # The internal user ID is required if the folder is configured to replace the
 # global address book. Note that if used, the value of the attribute must 
 # match the internal user ID in the groupware server. 
 com.openexchange.contact.storage.ldap.internal_userid  = 
 
 # If not set, the 'created_by' contact property is set to the context admin. 
 # When using the folder as replacement for the global address book, and the
 # users should be able to edit their own entries, this property should be
 # mapped to the same LDAP attribute as 'internal_userid'.
 com.openexchange.contact.storage.ldap.created_by   = 
 
 # If not set, the 'modified_by' contact property is set to the context admin.
 # When using the folder as replacement for the global address book, and the
 # users should be able to edit their own entries, this property should be
 # mapped to the same LDAP attribute as 'internal_userid'.
 com.openexchange.contact.storage.ldap.modified_by   = 
 
 
 # == String mappings =========================================================
 
 com.openexchange.contact.storage.ldap.displayname   = displayName,name
 com.openexchange.contact.storage.ldap.file_as   = displayName,name
 com.openexchange.contact.storage.ldap.givenname    = givenName
 com.openexchange.contact.storage.ldap.surname    = sn
 com.openexchange.contact.storage.ldap.email1    = mail
 com.openexchange.contact.storage.ldap.department   = department
 com.openexchange.contact.storage.ldap.company    = company
 com.openexchange.contact.storage.ldap.branches    =
 com.openexchange.contact.storage.ldap.business_category  =
 com.openexchange.contact.storage.ldap.postal_code_business = postalCode
 com.openexchange.contact.storage.ldap.state_business  = st
 com.openexchange.contact.storage.ldap.street_business  = streetAddress
 com.openexchange.contact.storage.ldap.telephone_callback =
 com.openexchange.contact.storage.ldap.city_home    =
 com.openexchange.contact.storage.ldap.commercial_register =
 com.openexchange.contact.storage.ldap.country_home   =
 com.openexchange.contact.storage.ldap.email2    =
 com.openexchange.contact.storage.ldap.email3    =
 com.openexchange.contact.storage.ldap.employeetype   =
 com.openexchange.contact.storage.ldap.fax_business   = facsimileTelehoneNumber
 com.openexchange.contact.storage.ldap.fax_home    =
 com.openexchange.contact.storage.ldap.fax_other    =
 com.openexchange.contact.storage.ldap.instant_messenger1 =
 com.openexchange.contact.storage.ldap.instant_messenger2 =
 com.openexchange.contact.storage.ldap.telephone_ip   =
 com.openexchange.contact.storage.ldap.telephone_isdn  = internationaliSDNNumber
 com.openexchange.contact.storage.ldap.manager_name   =
 com.openexchange.contact.storage.ldap.marital_status  =
 com.openexchange.contact.storage.ldap.cellular_telephone1 = mobile
 com.openexchange.contact.storage.ldap.cellular_telephone2 =
 com.openexchange.contact.storage.ldap.nickname    =
 com.openexchange.contact.storage.ldap.number_of_children =
 com.openexchange.contact.storage.ldap.number_of_employee =
 com.openexchange.contact.storage.ldap.note     = info
 com.openexchange.contact.storage.ldap.telephone_pager  = pager
 com.openexchange.contact.storage.ldap.telephone_assistant =
 com.openexchange.contact.storage.ldap.telephone_business1 = telephoneNumber
 com.openexchange.contact.storage.ldap.telephone_business2 =
 com.openexchange.contact.storage.ldap.telephone_car   =
 com.openexchange.contact.storage.ldap.telephone_company  =
 com.openexchange.contact.storage.ldap.telephone_home1  = homePhone
 com.openexchange.contact.storage.ldap.telephone_home2  =
 com.openexchange.contact.storage.ldap.telephone_other  =
 com.openexchange.contact.storage.ldap.postal_code_home  =
 com.openexchange.contact.storage.ldap.profession   =
 com.openexchange.contact.storage.ldap.telephone_radio  =
 com.openexchange.contact.storage.ldap.room_number   = physicalDeliveryOfficeName
 com.openexchange.contact.storage.ldap.sales_volume   =
 com.openexchange.contact.storage.ldap.city_other   =
 com.openexchange.contact.storage.ldap.country_other   =
 com.openexchange.contact.storage.ldap.middle_name   = middleName
 com.openexchange.contact.storage.ldap.postal_code_other  =
 com.openexchange.contact.storage.ldap.state_other   =
 com.openexchange.contact.storage.ldap.street_other   =
 com.openexchange.contact.storage.ldap.spouse_name   =
 com.openexchange.contact.storage.ldap.state_home   =
 com.openexchange.contact.storage.ldap.street_home   =
 com.openexchange.contact.storage.ldap.suffix    =
 com.openexchange.contact.storage.ldap.tax_id    =
 com.openexchange.contact.storage.ldap.telephone_telex  = telexNumber
 com.openexchange.contact.storage.ldap.telephone_ttytdd  =
 com.openexchange.contact.storage.ldap.url     = wWWHomePage
 com.openexchange.contact.storage.ldap.userfield01   =
 com.openexchange.contact.storage.ldap.userfield02   =
 com.openexchange.contact.storage.ldap.userfield03   =
 com.openexchange.contact.storage.ldap.userfield04   =
 com.openexchange.contact.storage.ldap.userfield05   =
 com.openexchange.contact.storage.ldap.userfield06   =
 com.openexchange.contact.storage.ldap.userfield07   =
 com.openexchange.contact.storage.ldap.userfield08   =
 com.openexchange.contact.storage.ldap.userfield09   =
 com.openexchange.contact.storage.ldap.userfield10   = 
 com.openexchange.contact.storage.ldap.userfield11   =
 com.openexchange.contact.storage.ldap.userfield12   =
 com.openexchange.contact.storage.ldap.userfield13   =
 com.openexchange.contact.storage.ldap.userfield14   =
 com.openexchange.contact.storage.ldap.userfield15   =
 com.openexchange.contact.storage.ldap.userfield16   =
 com.openexchange.contact.storage.ldap.userfield17   =
 com.openexchange.contact.storage.ldap.userfield18   =
 com.openexchange.contact.storage.ldap.userfield19   =
 com.openexchange.contact.storage.ldap.userfield20   =
 com.openexchange.contact.storage.ldap.city_business   = l
 com.openexchange.contact.storage.ldap.country_business  = co
 com.openexchange.contact.storage.ldap.assistant_name  =
 com.openexchange.contact.storage.ldap.telephone_primary  =
 com.openexchange.contact.storage.ldap.categories   =
 com.openexchange.contact.storage.ldap.title     = title
 com.openexchange.contact.storage.ldap.position    = 
 com.openexchange.contact.storage.ldap.uid     = objectGUID;binary
 com.openexchange.contact.storage.ldap.profession   = title
 
 
 # == Date mappings ===========================================================
 
 com.openexchange.contact.storage.ldap.birthday    =
 com.openexchange.contact.storage.ldap.anniversary   =
 
 # The last-modified and creation dates are required by the groupware server, 
 # therefore an implicit default date is assumed when no LDAP property is 
 # mapped here, and no results are available for this folder for the 'modified'
 # and 'deleted' API calls. Therefore, any synchronization-based usage will not
 # be available.
 com.openexchange.contact.storage.ldap.lastmodified   = whenChanged
 com.openexchange.contact.storage.ldap.creationdate   = whenCreated
 
 
 # == Misc mappings ===========================================================
 
 # Distribution list members are resolved dynamically using the DNs found in 
 # the  mapped LDAP property
 com.openexchange.contact.storage.ldap.distributionlist  = member
 
 # Special mapping where the boolean value is evaluated using a string 
 # comparison with the attribute value
 com.openexchange.contact.storage.ldap.markasdistributionlist= objectClass=group
 
 # Integer value between 1 and 3 specifying one of the values in email1... email3
 com.openexchange.contact.storage.ldap.defaultaddress  = 
 
 # Contact image, binary format is expected.
 com.openexchange.contact.storage.ldap.image1    = jpegPhoto
 # Should map to the actual image property
 com.openexchange.contact.storage.ldap.number_of_images  = jpegPhoto
 # Should map to the actual image property
 com.openexchange.contact.storage.ldap.image_last_modified = jpegPhoto
 # Should map to the actual image property
 com.openexchange.contact.storage.ldap.image1_content_type = jpegPhoto
```

# Upgrading from Contacts LDAP

Due to different configuration parameters and a different config file handling, there's no automatic migration when upgrading from previous installations that used the previous LDAP integration bundle [open-xchange-contacts-ldap](https://www.oxpedia.org/wiki/index.php?title=Contacts_LDAP_AD), so that the configuration steps above need to be performed, too. However, most of the configuration values and mapping attributes can be copied over. Please mind the following main differences between both bundles: 

## Configuration files
All configuration files are expected to be in the ``contact-storage-ldap`` subfolder, i.e. there's no need to create separate subfolders for the configuration files indicating the desired contexts. Furthermore, the property names don't need to be changed according to the context ID, since there's now a separate property for that purpose:

```
 com.openexchange.contact.storage.ldap.contextID=1
```

## Distribution list handling
There are no longer separate mappings for display name and object ID of distribution lists. Instead, if the LDAP attribute name differs from the one used for contacts, both LDAP attributes can be declared in the mapping, e.g.: 
 com.openexchange.contact.storage.ldap.displayname=displayName,name

## ID Mappings
Along with the previously used 'static' ID mappings, there's a new option that allows a persistent mapping between LDAP identifiers and groupware object IDs, even if the LDAP server uses non-numerical unique identifiers. In contrast to the previously available 'dynamic' ID mapping, this non-volatile mapping allows referencing LDAP objects from external clients across sessions. Persistent mapping can be enabled via:
 com.openexchange.contact.storage.ldap.idMapping=persistent

# Misc

## Incremental Synchronization

Different external Clients accessing the server via CardDAV have the requirement that the server is able to report all changes in a contact folder since the last synchronization. This includes updates to existing contacts, as well as deleted and newly created contacts. In order to use folders backed by the LDAP contact storage with such clients, the LDAP directory needs to be able to deliver these information, especially the so-called "tombstones" for deleted contacts, i.e. the knowledge about deleted directory entries, which is currently only available in Active Directoy and must be explicitly turned on in (see the documentation of the property for details):

```
 com.openexchange.contact.storage.ldap.ADS_deletion_support=true
```

Furthermore, a valid attribute mapping for "last modified" must be provided.

## Caching

To speed-up access to the LDAP directory, some contact properties are held in a distributed JCS cache. Therefore, the new configuration file cache.properties is used (the default values should normally not be changed). Cache entries are dynamically updated after a specified interval. If possible, i.e. there's a mapping for the last modification time of the contacts, only changed entries are refreshed, otherwise, all contacts are reloaded from the LDAP directory.

## Read-only

The LDAP contact storage works in read-only mode, meaning that any calls to create new, delete, or modify existing contacts in the folder are rejected.

## Folders

It's possible to define either the ID of an existing folder that should be used by the LDAP storage, or the name of a public folder that is created dynamically if it not yet exists. If the folder is created by the LDAP storage itself, a default set of permissions is applied (owned by the context administrator, granting read-only permissions to all users). So, for the context administrator, it's still possible to apply detailed access rights to the folder by adjusting the permissions of the folder afterwards. It's also possible to to create the folder on your own and use it's folder ID in the configuration file, e.g. if the folder should not be a public folder.

