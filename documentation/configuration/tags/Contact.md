---
title: Contact
---

This page shows all properties with the tag: Contact

| __Key__ | com.openexchange.user.contactCollectOnMailAccess |
|:----------------|:--------|
| __Description__ | Define the default behavior whether to collect contacts on mail access.<br>Note: Appropriate user access permission still needs to be granted in order to take effect.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | user.properties |

---
| __Key__ | com.openexchange.user.contactCollectOnMailTransport |
|:----------------|:--------|
| __Description__ | Define the default behavior whether to collect contacts on mail transport.<br>Note: Appropriate user access permission still needs to be granted in order to take effect.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | user.properties |

---
| __Key__ | IGNORE_SHARED_ADDRESSBOOK |
|:----------------|:--------|
| __Description__ | Determine whether to ignore 'shared addressbook' folder or not.<br>Possible values: TRUE / FALSE<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cache.html">Cache</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | foldercache.properties |

---
| __Key__ | ENABLE_INTERNAL_USER_EDIT |
|:----------------|:--------|
| __Description__ | Define if users are allowed to edit their own contact object<br>contained in folder 'Global Address Book' aka 'Internal Users'.<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | foldercache.properties |

---
| __Key__ | com.openexchange.quota.contact |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of contacts that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any contact in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.subscribe.google.contact.pageSize |
|:----------------|:--------|
| __Description__ | Defines the amount of contacts to fetch in a single request.<br> |
| __Default__ | 25 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | googlesubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.google.contact.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for Google contacts.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | googlesubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.linkedin |
|:----------------|:--------|
| __Description__ | Enable/disable LinkedIn subscribe service.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | linkedinsubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.linkedin.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for LinkedIn.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | linkedinsubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.mslive.contact.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for MS Live.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MSLiveConnect.html">MSLiveConnect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | mslivesubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.microformats.contacts.http |
|:----------------|:--------|
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Microformats.html">Microformats</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | microformatSubscription.properties |

---
| __Key__ | com.openexchange.subscribe.microformats.contacts.http.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for Microformats.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Microformats.html">Microformats</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | microformatSubscription.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.yahoo |
|:----------------|:--------|
| __Description__ | Enable/disable Yahoo subscribe service.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | yahoosubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.yahoo.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for Yahoo.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | yahoosubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.xing |
|:----------------|:--------|
| __Description__ | Enable/disable XING subscribe service.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/XING.html">XING</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | xingsubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.xing.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for XING.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/XING.html">XING</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | xingsubscribe.properties |

---
| __Key__ | contact_first_letter_field |
|:----------------|:--------|
| __Description__ | First Letter Field, the field you sort in when you hit one of the start letters<br>field02 Last name<br>field03 First name<br> |
| __Default__ | field02 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | validate_contact_email |
|:----------------|:--------|
| __Description__ | Check the entered email address from a new contact for correctness<br>(syntactic check user@domain.tld)<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | max_image_size |
|:----------------|:--------|
| __Description__ | "The maximum size in bytes for the upload of contact images. Remember: If you decrease the size after some images where uploaded already, <br> you might trigger errors, for example when trying to modify the contact without adding an image of the correct size." <br> |
| __Default__ | 4194304 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.mailAddressAutoSearch |
|:----------------|:--------|
| __Description__ | Determines if a search is triggered if the dialog for searching for emailable<br>contacts is opened. This dialog is used for selecting recipients for an email<br>and for creating distribution lists.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.singleFolderSearch |
|:----------------|:--------|
| __Description__ | Searching for contacts can be done in a single folder or globally across all folders. Searching across all folders can cause high server<br>and database load because first all visible folders must be determined and if a user has object read permissions in that folders. Software<br>internal default is true to prevent high load if the property is not defined. Default here is false because it is easier for the user to<br>find contacts.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contacts.characterSearch |
|:----------------|:--------|
| __Description__ | Enables/Disables the start letter based quick select of contacts<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contacts.allFoldersForAutoComplete |
|:----------------|:--------|
| __Description__ | "The auto complete search for email addresses may be triggered easily and quite often if a new email is written and a part of a recipients<br>address is written. This can lead to high load on the database system if a context has a lot of users and a lot of contacts. Therefore the<br>scope if this search can be configured. Set this parameter to true and the auto complete search looks in every readable contact folder for<br>contacts with emails addresses matching the already typed letters. If this parameter is configured to false, only three folders are<br>considered for the search: the users private default contact folder, his contact folder for collected contacts and the global address book<br>if that is enabled for the user."<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.scaleVCardImages |
|:----------------|:--------|
| __Description__ | Enables/Disables the start letter based quick select of contacts<br> |
| __Default__ | 200x200 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.storeVCards |
|:----------------|:--------|
| __Description__ | Specifies whether the original files are persisted during vCard import or <br>CardDAV synchronization. If enabled, the original vCard files will be stored <br>in the appropriate filestore and are considered during export again. If <br>disabled, all not mapped information is discarded and is no longer available<br>when exporting the vCard again. <br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.maxVCardSize |
|:----------------|:--------|
| __Description__ | Configures the maximum allowed size of a (single) vCard file in bytes. <br>vCards larger than the configured maximum size are rejected and not parsed <br>by the server. A value of "0" or smaller is considered as unlimited. <br> |
| __Default__ | 4194304 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.image.scaleImages |
|:----------------|:--------|
| __Description__ | Enables/Disables the scaling of contact images to a smaller size. <br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.image.maxWidth |
|:----------------|:--------|
| __Description__ | Defines the width of scaled contact images<br> |
| __Default__ | 250 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.image.maxHeight |
|:----------------|:--------|
| __Description__ | Defines the height of scaled contact images<br> |
| __Default__ | 250 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.image.scaleType |
|:----------------|:--------|
| __Description__ | Defines the scale type<br>1 = contain - maxWidth and maxHeight defines the maximum target dimension<br>2 = cover - maxWidth and maxHeight defines the minimum target dimension<br>3 = auto<br> |
| __Default__ | 2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.fulltextAutocomplete |
|:----------------|:--------|
| __Description__ | Configures if a FULLTEXT index dedicated for auto-completion can be used or<br>not. Once enabled, an appropriate index is created on the 'prg_contacts' <br>table automatically (covering fields specified through "com.openexchange.contact.fulltextIndexFields" property),<br>and is used afterwards to serve the "find as you type" <br>auto-completion requests in an efficient way.<br>Note that this index requires support for FULLTEXT index types on the used<br>InnoDB table, which is available starting with MySQL 5.6.4 (see<br>http://dev.mysql.com/doc/refman/5.6/en/fulltext-restrictions.html for <br>details).<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.fulltextIndexFields |
|:----------------|:--------|
| __Description__ | "Specifies the contact fields for which a FULLTEXT index is supposed to be created<br>provided that property "com.openexchange.contact.fulltextAutocomplete" is set to "true".<br><br>Supported fields<br> - DISPLAY_NAME<br> - SUR_NAME<br> - GIVEN_NAME<br> - TITLE<br> - SUFFIX<br> - MIDDLE_NAME<br> - COMPANY<br> - EMAIL1<br> - EMAIL2<br> - EMAIL3<br> - DEPARTMENT<br><br> Note:<br> These fields are only checked one time if "com.openexchange.contact.fulltextAutocomplete" is set to "true".<br> In case the fields are altered later on, manual execution of the associated update task is required to adapt<br> the FULLTEXT index to the newly specified fields:<br><br> /opt/open-xchange/sbin/forceupdatetask --task com.openexchange.contact.storage.rdb.groupware.AddFulltextIndexTask <other command-line arguments"<br> |
| __Default__ | DISPLAY_NAME, SUR_NAME, GIVEN_NAME, TITLE, SUFFIX, MIDDLE_NAME, COMPANY, EMAIL1, EMAIL2, EMAIL3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.autocomplete.fields |
|:----------------|:--------|
| __Description__ | "Defines the fields considered within the autocomplete search operation.<br>Must be a comma separated list of available fields.<br>The available fields are: GIVEN_NAME, SUR_NAME, DISPLAY_NAME, EMAIL1, EMAIL2, EMAIL3, DEPARTMENT"<br> |
| __Default__ | GIVEN_NAME, SUR_NAME, DISPLAY_NAME, EMAIL1, EMAIL2, EMAIL3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contact.search.fields |
|:----------------|:--------|
| __Description__ | "Defines the fields considered within the normal search operation.<br>Must be a comma separated list of available fields, groups of fields or any combination of both.<br>The available groups and their fields are the following:<br>"ADDRESS_FIELDS":  <br>   "STREET_BUSINESS", "STREET_HOME", "STREET_OTHER","POSTAL_CODE_BUSINESS", "POSTAL_CODE_HOME","POSTAL_CODE_OTHER","CITY_BUSINESS","CITY_HOME", <br>   "CITY_OTHER", "STATE_BUSINESS", "STATE_HOME", "STATE_OTHER", "COUNTRY_BUSINESS", "COUNTRY_HOME", "COUNTRY_OTHER"<br>"EMAIL_FIELDS":<br>   "EMAIL1", "EMAIL2", "EMAIL3", "DISTRIBUTIONLIST"<br>"NAME_FIELDS":<br>   "DISPLAY_NAME", "SUR_NAME", "MIDDLE_NAME", "GIVEN_NAME", "TITLE", "YOMI_FIRST_NAME", "YOMI_LAST_NAME", "SUFFIX"<br>"PHONE_FIELDS":<br>   "TELEPHONE_ASSISTANT", "TELEPHONE_BUSINESS1", "TELEPHONE_BUSINESS2", "TELEPHONE_CALLBACK", "TELEPHONE_CAR", "TELEPHONE_COMPANY", "TELEPHONE_HOME1",<br>   "TELEPHONE_HOME2", "TELEPHONE_IP", "TELEPHONE_ISDN", "TELEPHONE_OTHER", "TELEPHONE_PAGER", "TELEPHONE_PRIMARY", "TELEPHONE_RADIO", "TELEPHONE_TELEX",<br>   "TELEPHONE_TTYTDD", "CELLULAR_TELEPHONE1", "CELLULAR_TELEPHONE2", "NUMBER_OF_CHILDREN", <br>"USER_FIELDS":<br>   "USERFIELD01", "USERFIELD02", "USERFIELD03", "USERFIELD04", "USERFIELD05", "USERFIELD06",<br>   "USERFIELD07", "USERFIELD08", "USERFIELD09", "USERFIELD10", "USERFIELD11", "USERFIELD12",<br>   "USERFIELD13", "USERFIELD14", "USERFIELD15", "USERFIELD16", "USERFIELD17", "USERFIELD18",<br>   "USERFIELD19", "USERFIELD20"<br><br><br>The remaining unassigned fields are:<br>   "CATEGORIES", "COMPANY", "DEPARTMENT", "COMMERCIAL_REGISTER", "MARITAL_STATUS", "PROFESSION", "NICKNAME", "SPOUSE_NAME", "NOTE", "POSITION", "EMPLOYEE_TYPE",<br>   "ROOM_NUMBER", "TAX_ID", "BRANCHES", "BUSINESS_CATEGORY", "INFO", "MANAGER_NAME", "ASSISTANT_NAME", "FAX_BUSINESS", "FAX_HOME", "FAX_OTHER", "URL", "INSTANT_MESSENGER1",<br>   "INSTANT_MESSENGER2",   "<br> |
| __Default__ | ADDRESS_FIELDS, EMAIL_FIELDS, NAME_FIELDS, PHONE_FIELDS, CATEGORIES, COMPANY, DEPARTMENT, COMMERCIAL_REGISTER, POSITION |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contact.properties |

---
| __Key__ | com.openexchange.contactcollector.enabled |
|:----------------|:--------|
| __Description__ | Whether enabled or disabled regardless of bundle start-up<br>Enabled by default (provided that bundle is installed and properly started)<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contactcollector.properties |

---
| __Key__ | com.openexchange.contactcollector.folder.deleteDenied |
|:----------------|:--------|
| __Description__ | Whether deletion of contact collector folder will be denied<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | contactcollector.properties |

---
