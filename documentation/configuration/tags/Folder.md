---
title: Folder
---

This page shows all properties with the tag: Folder

| __Key__ | com.openexchange.mail.useStaticDefaultFolders |
|:----------------|:--------|
| __Description__ | Defines whether standard folder names should be initialized with the default values or not. <br>The default values can be configured within the Adminuser.properties file.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maliciousFolders.enabled |
|:----------------|:--------|
| __Description__ | Enables/disables support for malicious folders.<br><br>If enabled and a mail is fetched from a folder contained in listing configured through "com.openexchange.mail.maliciousFolders.listing" property, the mail's JSON representation contains an additional "malicious: true" field and possible HTML content is processed in the way to disable any hyper-links.<br><br>I.e.<br>"...&lt;a href="http://evil.com/click.me"&gt;Get something for free here&lt;/a&gt;..."<br>is turned to<br>"...&lt;a href="#" onclick"return false;" data-disabled="true"&gt;Get something for free here&lt;/a&gt;..."<br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.maliciousFolders.listing |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maliciousFolders.listing |
|:----------------|:--------|
| __Description__ | Specifies the full paths for such mail folders in the primary account's folder tree that are supposed being considered as malicious and therefore a special treatment happens; e.g. hyper-links that occur in mail content are not displayed and/or are not clickable.<br><br>The value is supposed to be comma-separated list of folder paths. An entry in the CSV list is either a full path of a folder in the primary mail account (e.g. "INBOX/Malware") or a reserved identifier denoting the standard folder and its sub-folders of every mail account (incl. Unified Mail). Supported reserved identifiers are: "$Spam", "$Drafts", "$Sent", "$Trash", "$Confirmed-spam", "$Confirmed-ham".<br><br>Example: $Spam, INBOX/Malware<br>Special treatment happens for standard Spam folder and for the "INBOX/Malware" folder in primary account's folder tree hierarchy.<br> |
| __Default__ | $Spam, $Confirmed-spam |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.maliciousFolders.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.folder.json.module |
|:----------------|:--------|
| __Description__ | Define the module which also serves as the appendix for registered servlet:<br>If module is set to "myfolders", the servlet path is changed to "/ajax/myfolders"<br>Default is "folders"<br> |
| __Default__ | folders |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/JSON.html">JSON</a> |
| __File__ | folderjson.properties |

---
| __Key__ | com.openexchange.folder.json.servletPath |
|:----------------|:--------|
| __Description__ | Define the path for registered servlet<br>If set, the complete path is set to specified value regardless of the value of "com.openexchange.folder.json.module" property<br>Default is empty to let path be: "/ajax/" + <module><br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/JSON.html">JSON</a> |
| __File__ | folderjson.properties |

---
| __Key__ | com.openexchange.mail.transport.publishingPublicInfostoreFolder |
|:----------------|:--------|
| __Description__ | Specify the name of the publishing infostore folder which is created below public infostore folder.<br>The denoted folder is created if absent only if "com.openexchange.mail.transport.enablePublishOnExceededQuota" is enabled.<br>The special identifier "i18n-defined" indicates to use translation of text "E-Mail attachments".<br> |
| __Default__ | i18n-defined |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.mail.transport.enablePublishOnExceededQuota |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Infostore.html">Infostore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | transport.properties |

---
| __Key__ | SENT_MAILFOLDER_[language] |
|:----------------|:--------|
| __Description__ | Default sent mail folder fallback for a specific language if not sent by rmi client. <br>The [language] variable must be replaced by an upper case language identifier. E.g. SENT_MAILFOLDER_DE_DE<br>Default values:<br>  SENT_MAILFOLDER_DE_DE=Gesendete Objekte<br>  SENT_MAILFOLDER_EN_GB=Sent Mai<br>  SENT_MAILFOLDER_EN_US=Sent Items<br>  SENT_MAILFOLDER_FR_FR=Objets envoy\u00e9s<br>  SENT_MAILFOLDER_NL_NL=Verzonden items<br>  SENT_MAILFOLDER_SV_SV=Skickat<br>  SENT_MAILFOLDER_ES_ES=Elementos enviados<br>  SENT_MAILFOLDER_JA_JP=\u9001\u4FE1\u6E08\u30A2\u30A4\u30C6\u30E0<br>  SENT_MAILFOLDER_PL_PL=Elementy wys\u0142ane<br>  SENT_MAILFOLDER_IT_IT=Posta inviata<br>  SENT_MAILFOLDER_ZH_CN=\u5df2\u53d1\u9001\u90ae\u4ef6<br>  SENT_MAILFOLDER_CS_CZ=Odeslan\u00e9 polo\u017eky<br>  SENT_MAILFOLDER_HU_HU=Elk\u00fcld\u00f6tt elemek<br>  SENT_MAILFOLDER_SK_SK=Odoslan\u00e9 polo\u017eky<br>  SENT_MAILFOLDER_LV_LV=Nos\u016Bt\u012Bt\u0101s vien\u012Bbas<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | TRASH_MAILFOLDER_[language] |
|:----------------|:--------|
| __Description__ | Default trash mail folder fallback if not sent by rmi client.<br>The [language] variable must be replaced by an upper case language identifier. E.g. TRASH_MAILFOLDER_DE_DE<br>Default values:<br>  TRASH_MAILFOLDER_DE_DE=Papierkorb<br>  TRASH_MAILFOLDER_EN_GB=Trash<br>  TRASH_MAILFOLDER_EN_US=Trash<br>  TRASH_MAILFOLDER_FR_FR=Corbeille<br>  TRASH_MAILFOLDER_NL_NL=Prullenbak<br>  TRASH_MAILFOLDER_SV_SV=Papperskorgen<br>  TRASH_MAILFOLDER_ES_ES=Papelera<br>  TRASH_MAILFOLDER_JA_JP=\u524A\u9664\u6E08\u307F\u30A2\u30A4\u30C6\u30E0<br>  TRASH_MAILFOLDER_PL_PL=Kosz<br>  TRASH_MAILFOLDER_IT_IT=Cestino<br>  TRASH_MAILFOLDER_ZH_CN=\u5783\u573e\u7b52<br>  TRASH_MAILFOLDER_CS_CZ=Ko\u0161<br>  TRASH_MAILFOLDER_HU_HU=T\u00f6r\u00f6lt elemek<br>  TRASH_MAILFOLDER_SK_SK=K\u00f4\u0161<br>  TRASH_MAILFOLDER_LV_LV=Atkritumi<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | DRAFTS_MAILFOLDER_[language] |
|:----------------|:--------|
| __Description__ | Default drafts mail folder fallback if not sent by rmi client.<br>The [language] variable must be replaced by an upper case language identifier. E.g. DRAFTS_MAILFOLDER_DE_DE<br>Default values:<br>  DRAFTS_MAILFOLDER_DE_DE=Entw\u00fcrfe<br>  DRAFTS_MAILFOLDER_EN_GB=Drafts<br>  DRAFTS_MAILFOLDER_EN_US=Drafts<br>  DRAFTS_MAILFOLDER_FR_FR=Brouillons<br>  DRAFTS_MAILFOLDER_NL_NL=Concepten<br>  DRAFTS_MAILFOLDER_SV_SV=Utkast<br>  DRAFTS_MAILFOLDER_ES_ES=Borradores<br>  DRAFTS_MAILFOLDER_JA_JP=\u4E0B\u66F8\u304D<br>  DRAFTS_MAILFOLDER_PL_PL=Szkice<br>  DRAFTS_MAILFOLDER_IT_IT=Bozze<br>  DRAFTS_MAILFOLDER_ZH_CN=\u8349\u7a3f<br>  DRAFTS_MAILFOLDER_CS_CZ=Koncepty<br>  DRAFTS_MAILFOLDER_HU_HU=Piszkozatok<br>  DRAFTS_MAILFOLDER_SK_SK=Rozp\u00edsan\u00e9<br>  DRAFTS_MAILFOLDER_LV_LV=Melnraksti<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | SPAM_MAILFOLDER_[language] |
|:----------------|:--------|
| __Description__ | Default spam mail folder fallback if not sent by rmi client.<br>The [language] variable must be replaced by an upper case language identifier. E.g. SPAM_MAILFOLDER_DE_DE<br>Default values:<br>  SPAM_MAILFOLDER_DE_DE=Spam<br>  SPAM_MAILFOLDER_EN_GB=Spam<br>  SPAM_MAILFOLDER_EN_US=Spam<br>  SPAM_MAILFOLDER_FR_FR=Pourriel<br>  SPAM_MAILFOLDER_NL_NL=Spam<br>  SPAM_MAILFOLDER_SV_SV=Skr\u00E4ppost<br>  SPAM_MAILFOLDER_ES_ES=Correo no deseado<br>  SPAM_MAILFOLDER_JA_JP=\u8FF7\u60D1\u30E1\u30FC\u30EB<br>  SPAM_MAILFOLDER_PL_PL=Spam<br>  SPAM_MAILFOLDER_IT_IT=Posta Indesiderata<br>  SPAM_MAILFOLDER_ZH_CN=\u5783\u573e\u90ae\u4ef6<br>  SPAM_MAILFOLDER_CS_CZ=Spam<br>  SPAM_MAILFOLDER_HU_HU=Lev\u00e9lszem\u00e9t<br>  SPAM_MAILFOLDER_SK_SK=Spam<br>  SPAM_MAILFOLDER_LV_LV=M\u0113stules<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | CONFIRMED_SPAM_MAILFOLDER_[language] |
|:----------------|:--------|
| __Description__ | Default confirmed spam mail folder fallback if not sent by rmi client.<br>The [language] variable must be replaced by an upper case language identifier. E.g. CONFIRMED_SPAM_MAILFOLDER_DE_DE<br>Default values: <br>  CONFIRMED_SPAM_MAILFOLDER_DE_DE=confirmed-spam<br>  CONFIRMED_SPAM_MAILFOLDER_EN_GB=confirmed-spam<br>  CONFIRMED_SPAM_MAILFOLDER_EN_US=confirmed-spam<br>  CONFIRMED_SPAM_MAILFOLDER_FR_FR=pourriel-confirme<br>  CONFIRMED_SPAM_MAILFOLDER_NL_NL=bevestigde spam<br>  CONFIRMED_SPAM_MAILFOLDER_SV_SV=bekr\u00E4ftad-skr\u00E4ppost<br>  CONFIRMED_SPAM_MAILFOLDER_ES_ES=correo basura confirmado<br>  CONFIRMED_SPAM_MAILFOLDER_JA_JP=\u8FF7\u60D1\u30E1\u30FC\u30EB\uFF08\u78BA\u8A8D\u6E08\uFF09<br>  CONFIRMED_SPAM_MAILFOLDER_PL_PL=Potwierdzony spam<br>  CONFIRMED_SPAM_MAILFOLDER_IT_IT=Posta indesiderata accertata<br>  CONFIRMED_SPAM_MAILFOLDER_ZH_CN=\u5df2\u786e\u8ba4\u7684\u5783\u573e\u90ae\u4ef6<br>  CONFIRMED_SPAM_MAILFOLDER_CS_CZ=Potvrzen\u00fd spam<br>  CONFIRMED_SPAM_MAILFOLDER_HU_HU=Elfogadott k\u00e9retlen<br>  CONFIRMED_SPAM_MAILFOLDER_SK_SK=Potvrden\u00fd spam<br>  CONFIRMED_SPAM_MAILFOLDER_LV_LV=Apstiprin\u0101ta "m\u0113stule"<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | CONFIRMED_HAM_MAILFOLDER_[language] |
|:----------------|:--------|
| __Description__ | Default confirmed ham mail folder fallback if not sent by rmi client.<br>The [language] variable must be replaced by an upper case language identifier. E.g. CONFIRMED_HAM_MAILFOLDER__DE_DE<br>Default values: <br>  CONFIRMED_HAM_MAILFOLDER_DE_DE=confirmed-ham<br>  CONFIRMED_HAM_MAILFOLDER_EN_GB=confirmed-ham<br>  CONFIRMED_HAM_MAILFOLDER_EN_US=confirmed-ham<br>  CONFIRMED_HAM_MAILFOLDER_FR_FR=non-pourriel-confirme<br>  CONFIRMED_HAM_MAILFOLDER_NL_NL=bevestigde ham<br>  CONFIRMED_HAM_MAILFOLDER_SV_SV=felaktigt-bekr\u00E4ftad-spam<br>  CONFIRMED_HAM_MAILFOLDER_ES_ES=correo leg\u00EDtimo confirmado<br>  CONFIRMED_HAM_MAILFOLDER_JA_JP=\u4E00\u822C\u30E1\u30FC\u30EB\uFF08\u78BA\u8A8D\u6E08\uFF09<br>  CONFIRMED_HAM_MAILFOLDER_PL_PL=Potwierdzony nie-spam<br>  CONFIRMED_HAM_MAILFOLDER_IT_IT=Posta attendibile accertata<br>  CONFIRMED_HAM_MAILFOLDER_ZH_CN=\u5df2\u786e\u8ba4\u7684\u6b63\u5e38\u90ae\u4ef6<br>  CONFIRMED_HAM_MAILFOLDER_CS_CZ=Potvrzen\u00e1 norm\u00e1ln\u00ed po\u0161ta<br>  CONFIRMED_HAM_MAILFOLDER_HU_HU=Elfogadott \u00e1l-k\u00e9retlen<br>  CONFIRMED_HAM_MAILFOLDER_SK_SK=Potvrden\u00e9 ako nie spam<br>  CONFIRMED_HAM_MAILFOLDER_LV_LV=Apstiprin\u0101ts "ham"<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | com.openexchange.folder.tree |
|:----------------|:--------|
| __Description__ | Defines the default folder tree that should be used if a user has not selected one.<br> |
| __Default__ | 1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | user.properties |

---
| __Key__ | com.openexchange.folderstorage.defaultPermissions |
|:----------------|:--------|
| __Description__ | Specifies default permission to use in case folder is supposed to be created below a certain parent folder.<br>The value is a pipe ("&#124;") separated listing of expressions; each expression defines the default permissions<br>for a denoted parent folder. Currently the reserved folder identifiers "2" and "15" are considered as "2"<br>denoted the public PIM folder whereas "15" denotes the public Drive folder.<br><br>An expression starts with the parent folder identifier followed by '=' character; e.g. "2=".<br>Then there is a comma-separated list of permissions to assume per entity (user or group).<br><br>Each permission either starts with "user_", "admin_user_", "group_" or "admin_group_" (the prefix "admin_" controls<br>whether the entity is supposed to be set as folder administrator) followed by the numeric entity identifier.<br><br>Then an '@' character is supposed to occur and finally followed by rights expression. The rights may be dot-separated<br>listing (<folder-permission> + "." + <read-permission> + "." + <write-permission> + "." + <delete-permission>) or one<br>of the tokens  "viewer", "writer" or "author".<br><br>More formally<br>expressions = expression ("&#124;" expression)\*<br>expression = folder "=" permission ("," permission)\*<br>permission = ("admin_")? ("group_" &#124; "user_") entity(int) "@" rights<br>rights = (folder-permission(int) "." read-permission(int) "." write-permission(int) "." delete-permission(int)) &#124; ("viewer" &#124; "writer" &#124; "author")<br><br>Example<br>2=group_2@2.4.0.0,admin_user_5@8.4.4.4&#124;15=admin_group_2@8.8.8.8<br>2=group_2@viewer,admin_user_5@author&#124;15=admin_group_2@writer<br> |
| __Default__ | No defaut value |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a> |
| __File__ | folder.properties |

---
| __Key__ | com.openexchange.folderstorage.inheritParentPermissions |
|:----------------|:--------|
| __Description__ | Specifies if permissions of new parent folder should be applied when moving a folder into the public folder tree.<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a> |
| __File__ | folder.properties |

---
| __Key__ | com.openexchange.folderstorage.inheritParentPermissions |
|:----------------|:--------|
| __Description__ | Specifies if permissions of new parent folder should be applied when moving a folder into the public folder tree.<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a> |
| __File__ | folder.properties |

---
| __Key__ | ENABLE_DB_GROUPING |
|:----------------|:--------|
| __Description__ | Define where to perform folder grouping.<br>This filters the database results for duplicate folders in the where <br>clause of the db statement or afterwards in the application. <br>Possible values: TRUE / FALSE<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | foldercache.properties |

---
| __Key__ | ENABLE_FOLDER_CACHE |
|:----------------|:--------|
| __Description__ | Enable or disable folder caching. Possible values: TRUE / FALSE<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | foldercache.properties |

---
| __Key__ | IGNORE_SHARED_ADDRESSBOOK |
|:----------------|:--------|
| __Description__ | Determine whether to ignore 'shared addressbook' folder or not.<br>Possible values: TRUE / FALSE<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
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
| __Key__ | com.openexchange.folderstorage.outlook.showPersonalBelowInfoStore |
|:----------------|:--------|
| __Description__ | Specifies whether a user's default InfoStore folder should appear below<br>InfoStore root folder:<br><br>- Infostore<br>-- My files<br>-- Public infostore<br>-- Userstore<br>-- Other infstore folders<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | foldercache.properties |

---
| __Key__ | com.openexchange.folderstorage.database.preferDisplayName |
|:----------------|:--------|
| __Description__ | Specifies whether default InfoStore folders are labeled with owning user's display name<br>or name is read from folder storage (database).<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Database.html">Database</a> |
| __File__ | foldercache.properties |

---
| __Key__ | com.openexchange.drive.useTempFolder |
|:----------------|:--------|
| __Description__ | Specifies whether the synchronization logic will make use of a folder named<br>".drive" below the root synchronization folder or not. If enabled, this<br>folder is used to store temporary uploads and removed files, which usually<br>leads to a better user experience since previously synchronized files can<br>be restored from there for example. If not, removed files are not kept, and<br>uploads are performed directly in the target folder.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.cleaner.interval |
|:----------------|:--------|
| __Description__ | Configures the interval between runs of the cleaner process for the<br>temporary ".drive" folder. A cleaner run is only initiated if the<br>synchronization is idle, i.e. the last synchronization resulted in no<br>actions to be performed, and the last run was before the configured<br>interval. The value can be defined using units of measurement: "D" (=days),<br>"W" (=weeks) and "H" (=hours).<br> |
| __Default__ | 1D |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.cleaner.maxAge |
|:----------------|:--------|
| __Description__ | Defines the maximum age of files and directories to be kept inside the<br>temporary ".drive" folder. Files or directories that were last modified<br>before the configured age are deleted during the next run of the cleaner<br>process. The value can be defined using units of measurement: "D" (=days),<br>"W" (=weeks) and "H" (=hours).<br> |
| __Default__ | 1D |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxDirectories |
|:----------------|:--------|
| __Description__ | Defines the maximum number of synchronizable directories per root folder. A<br>value of "-1" disables the limitation.<br> |
| __Default__ | 65535 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.maxFilesPerDirectory |
|:----------------|:--------|
| __Description__ | Defines the maximum number of synchronizable files per root folder. A<br>value of "-1" disables the limitation.<br> |
| __Default__ | 65535 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.excludedFolders |
|:----------------|:--------|
| __Description__ | Allows to exclude specific root folders from OX Drive synchronization explicitly. <br>Excluded folders may not be used as root folder for the synchronization, <br>however, this does not apply to their subfolders automatically.<br>Excluded folders should be specified in a comma-separated list of their unique identifiers.<br>Typical candidates for the blacklist would be folder 15 (the "public folders" root) <br>or folder 10 (the "shared folders" root) in large enterprise installations.      <br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.spamhandler.defaultspamhandler.createConfirmedSpam |
|:----------------|:--------|
| __Description__ | Indicates whether to create the confirmed-spam folder during check for default mail folders<br>during login if spam is enabled for logged-in user.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | defaultspamhandler.properties |

---
| __Key__ | com.openexchange.spamhandler.defaultspamhandler.createConfirmedHam |
|:----------------|:--------|
| __Description__ | Indicates whether to create the confirmed-ham folder during check for default mail folders<br>during login if spam is enabled for logged-in user.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | defaultspamhandler.properties |

---
| __Key__ | com.openexchange.spamhandler.defaultspamhandler.unsubscribeSpamFolders |
|:----------------|:--------|
| __Description__ | Indicates whether the confirmed-spam/confirmed-ham folders shall automatically be unsubscribed during login.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | defaultspamhandler.properties |

---
| __Key__ | com.openexchange.push.malpoll.folder |
|:----------------|:--------|
| __Description__ | Define the folder to look-up for new mails in each mailbox.<br> |
| __Default__ | INBOX |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MAL_Poll.html">MAL Poll</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | malpoll.properties |

---
| __Key__ | com.openexchange.caldav.tree |
|:----------------|:--------|
| __Description__ | Configures the ID of the folder tree used by the CalDAV interface.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.caldav.url |
|:----------------|:--------|
| __Description__ | Tells users where to find a caldav folder. This can be displayed in frontends.<br>You can use the variables [hostname] and [folderId] <br>If you chose to deploy caldav as a virtual host (say 'dav.open-xchange.com') use<br>https://dav.open-xchange.com/caldav/[folderId] as the value<br>If you are using user-agent sniffing use<br>https://[hostname]/caldav/[folderId]        <br> |
| __Default__ | https://[hostname]/caldav/[folderId] |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CalDAV.html">CalDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | caldav.properties |

---
| __Key__ | com.openexchange.carddav.ignoreFolders |
|:----------------|:--------|
| __Description__ | A comma-separated list of folder IDs to exclude from the synchronization. <br>Use this to disable syncing of very large folders (e.g. the global address <br>list in large contexts, which always has ID 6). By default, no folders are<br>excluded.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.tree |
|:----------------|:--------|
| __Description__ | Configures the ID of the folder tree used by the CardDAV interface.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.exposedCollections |
|:----------------|:--------|
| __Description__ | Controls which collections are exposed via the CardDAV interface. Possible <br>values are '0', '1' and '2'. A value of '1' makes each visible folder <br>available as a resource collection, while '2' only exposes an aggregated <br>collection containing  all contact resources from all visible folders. The <br>default value '0' exposes either an aggregated collection or individual <br>collections for each folder, depending on the client's user-agent that is <br>matched against the pattern in 'userAgentForAggregatedCollection'. <br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | carddav.properties |

---
| __Key__ | com.openexchange.carddav.userAgentForAggregatedCollection |
|:----------------|:--------|
| __Description__ | Specifies if all visible folders are used to create the aggregated <br>collection, or if a reduced set of folders only containing the global <br>addressbook and the personal contacts folders should be used. This setting<br>only influences the aggregated collection that is used for clients that<br>don't support multiple collections. Possible values are 'true' and 'false.<br> |
| __Default__ | .\*CFNetwork.\*Darwin.\*&#124;.\*AddressBook.\*CardDAVPlugin.\*Mac_OS_X.\*&#124;.\*Mac OS X.\*AddressBook.\* |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.carddav.exposedCollections |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/CardDAV.html">CardDAV</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | carddav.properties |

---
| __Key__ | mail.store.maildir.cachefolders |
|:----------------|:--------|
| __Description__ | Whether to cache maildir folder objects or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.headercachemaxsize |
|:----------------|:--------|
| __Description__ | Define the maximum number of message headers that are kept in a maildir folder's header cache.<br> |
| __Default__ | 1000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.maxnumofheadercaches |
|:----------------|:--------|
| __Description__ | Define the maximum number of maildir folder header caches.<br> |
| __Default__ | 10 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a> |
| __File__ | javamail.properties |

---
| __Key__ | com.openexchange.caching.jcs.remoteInvalidationForPersonalFolders |
|:----------------|:--------|
| __Description__ | Specifies if changes to personal folders (personal in terms of non-global e.g. folders kept in database) are supposed to be propagated<br>to remote nodes. This option is only useful for installations that do offer collaboration features or do not support session stickyness.<br>For instance users are able to share mail folders or might be load-balanced to other nodes while active in a single session.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Session.html">Session</a> |
| __File__ | cache.properties |

---
