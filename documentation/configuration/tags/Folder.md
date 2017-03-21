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
