---
title: User
---

This page shows all properties with the tag: User

| __Key__ | com.openexchange.user.beta |
|:----------------|:--------|
| __Description__ | Specifies whether beta features are enabled/disabled per default. The value is remembered for every user in its attributes.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a> |
| __File__ | user.properties |

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
| __Key__ | com.openexchange.user.maxClientCount |
|:----------------|:--------|
| __Description__ | Specify the max. allowed number of client identifiers stored/tracked per user.<br>A value equal to or less than zero means unlimited.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | user.properties |

---
| __Key__ | DEFAULT_TIMEZONE |
|:----------------|:--------|
| __Description__ | Specifies the default time zone to assume when creating a user<br>and no explicit time zone is given in arguments.<br> |
| __Default__ | Europe/Berlin |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timezone.html">Timezone</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Provisioning.html">Provisioning</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | AUTO_TO_LOWERCASE_UID |
|:----------------|:--------|
| __Description__ | This check will be performed on user names in case CHECK_USER_UID_FOR_NOT_ALLOWED_CHARS is set to true.<br> |
| __Default__ | [$@%\.+a-zA-Z0-9_-] |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | CHECK_USER_UID_FOR_NOT_ALLOWED_CHARS |
|:----------------|:--------|
| __Description__ | This will check the user name using the check defined in CHECK_USER_UID_REGEXP.<br> |
| __Default__ | true |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | CHECK_USER_UID_REGEXP |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | CHECK_USER_UID_REGEXP |
|:----------------|:--------|
| __Description__ | This check will be performed on user names in case CHECK_USER_UID_FOR_NOT_ALLOWED_CHARS is set to true.<br> |
| __Default__ | [$@%\.+a-zA-Z0-9_-] |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | CHECK_USER_UID_FOR_NOT_ALLOWED_CHARS |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | USERNAME_CHANGEABLE |
|:----------------|:--------|
| __Description__ | WARNING: Changing the username might have impact on external systems like<br>imap server that allows acls to be set. Imap ACLS usually use<br>symbolic names so when changing the user name without changing<br>the ACL breaks the ACL. open-xchange-admin will NOT change those<br>ACLS, so DO NOT CHANGE THIS SETTING ON OX Express<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | PRIMARY_MAIL_UNCHANGEABLE |
|:----------------|:--------|
| __Description__ | Here you can set whether the primary mail address can be changed or not.<br>If set to false, it is possible to change the primary mail address.<br>Only change, if you know what you are doing (Outlook might<br>not work anymore under certain circumstances)<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | AVERAGE_USER_SIZE |
|:----------------|:--------|
| __Description__ | The average file storage occupation for a user in MB.<br> |
| __Default__ | 100 |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | ALLOW_CHANGING_QUOTA_IF_NO_FILESTORE_SET |
|:----------------|:--------|
| __Description__ | Defines whether it is allowed to change the quota value for a user that has no individual file storage set<br><br>If set to "true" and the user has not yet an individual file storage set, an appropriate file storage gets<br>assigned to the user. This implicitly includes to move the user's files from context file storage to that<br>newly assigned file storage, which might be a long operation.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | AdminUser.properties |

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
