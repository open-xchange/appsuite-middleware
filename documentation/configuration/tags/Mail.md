---
title: Mail
---

This page shows all properties with the tag: Mail

| __Key__ | com.openexchange.spamhandler.name |
|:----------------|:--------|
| __Description__ | Specifies the name of the spam handler to use for the primary mail account. The special name "NoSpamHandler" explicitly sets no spam handler<br>If such a setting is not specified, the spam handler as configured through the mail bundle is used;<br>e.g. "com.openexchange.imap.spamHandler" in file 'imap.properties'<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | spamhandler.properties |

---
| __Key__ | com.openexchange.spamhandler.enabled |
|:----------------|:--------|
| __Description__ | Allows to enable/disable spam handling per user/context/server via ConfigCascade (based on the configured spam handler).<br>If no configuration is available (for the mentioned property) the previously configured user setting mail permission bit will be taken into account. If there is a configuration for "com.openexchange.spamhandler.enabled" available these will be used for the defined scope <br><b>Caution:</b> if the property has been set via ConfigCascade only these source will be used. Changing the user configuration afterwards via /opt/open-xchange/sbin/changeuser ... --gui_spam_filter_capabilities_enabled true/false will have no effect! You can change it for instance on a user base as described here: http://oxpedia.org/wiki/index.php?title=ConfigCascade . If you remove the property from ConfigCascade sources the formerly overwritten permission bit will be used.<br> |
| __Default__ | UserSettingMail permission bit from database |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.spamhandler.name |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | spamhandler.properties |

---
| __Key__ | com.openexchange.dovecot.doveadm.enabled |
|:----------------|:--------|
| __Description__ | Specifies whether the connector for the Dovecot DoveAdm REST interface will be enabled or not<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Rest.html">Rest</a> |
| __File__ | doveadm.properties |

---
| __Key__ | com.openexchange.dovecot.doveadm.endpoints |
|:----------------|:--------|
| __Description__ | Specifies the URIs to the Dovecot DoveAdm REST interface end-points. <br>e.g. "http://dovecot1.host.invalid:8081, http://dovecot2.host.invalid:8081, http://dovecot3.host.invalid:8081"<br><br>Moreover connection-related attributes are allowed to be specified to influence HTTP connection and pooling behavior<br>com.openexchange.dovecot.doveadm.endpoints.totalConnections        The number of total connections held in HTTP connection pool<br>com.openexchange.dovecot.doveadm.endpoints.maxConnectionsPerRoute  The number of connections per route held in HTTP connection pool; or less than/equal to 0 (zero) for auto-determining<br>com.openexchange.dovecot.doveadm.endpoints.readTimeout             The read time-out in milliseconds (default is 10sec)<br>com.openexchange.dovecot.doveadm.endpoints.connectTimeout          The connect time-out in milliseconds (default is 3sec)<br>com.openexchange.dovecot.doveadm.endpoints.checkInterval           The time interval in milliseconds when to check if a previously black-listed end-point is re-available again (default is 60sec)<br><br>Full example :<br>com.openexchange.dovecot.doveadm.endpoints=http://dovecot1.host.invalid:8081, http://dovecot2.host.invalid:8081<br>com.openexchange.dovecot.doveadm.endpoints.totalConnections=100<br>com.openexchange.dovecot.doveadm.endpoints.maxConnectionsPerRoute=0 (max. connections per route is then determined automatically by specified end-points)<br>com.openexchange.dovecot.doveadm.endpoints.readTimeout=10000<br>com.openexchange.dovecot.doveadm.endpoints.connectTimeout=3000<br>com.openexchange.dovecot.doveadm.endpoints.checkInterval=60000<br><br>The values can be configured within a dedicated .properties file; e.g. 'doveadm.properties'.<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Rest.html">Rest</a> |
| __File__ | doveadm.properties |

---
| __Key__ | com.openexchange.dovecot.doveadm.apiSecret |
|:----------------|:--------|
| __Description__ | Specifies the API secret to communicate with the Dovecot DoveAdm REST interface<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Rest.html">Rest</a> |
| __File__ | doveadm.properties |

---
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
| __Key__ | com.openexchange.imap.setSpecialUseFlags |
|:----------------|:--------|
| __Description__ | Defines whether the ox middleware is allowed to set special use flags.<br>If set to 'false' the ox middleware will never set any special use flags on folders on the imap server.<br>If set to 'true' the ox middleware will only set special use flags if no special use flag of that type exist yet.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.mail.preferSentDate |
|:----------------|:--------|
| __Description__ | Specifies what to consider as the date of a mail; either the internal received date or mail's sent date (as given by "Date" header).<br>This property is considered in case a client passes special "date" (661) column to "columns" parameter and/or "sort" parameter.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maxDriveAttachments |
|:----------------|:--------|
| __Description__ | Specifies the max. number of Drive documents that are allowed to be sent via E-Mail<br> |
| __Default__ | 20 |
| __Version__ | 7.6.2 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for primary account's mail access. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transport.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for primary account's mail transport. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.flagging.mode |
|:----------------|:--------|
| __Description__ | Specifies how color labels and special \Flagged system flag are connected (or not). Possible values:<br>-<code>colorOnly</code> Only color flags are available. The special \Flagged system flag is not touched.<br>-<code>flaggedOnly</code> Only special \Flagged system flag is used. Color labels are not published.<br>-<code>flaggedAndColor</code> Both - color flags and special \Flagged system flag - are available and set independently.<br>-<code>flaggedImplicit</code>Both - color flags and special \Flagged system flag - are available. A certain color label is linked with the \Flagged system flag. That is to add a color to colorless flagged mails and to add flagged to unflagged but colored mails.<br> |
| __Default__ | colorOnly |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.flagging.color |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.flagging.color |
|:----------------|:--------|
| __Description__ | Specifies the color which should be added to colorless flagged mails in case the flagging mode is "flaggedImplicit". Only values from 1 to 10 are allowed.<br> |
| __Default__ | 1 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.flagging.mode |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
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
| __Key__ | com.openexchange.imap.greeting.host.regex |
|:----------------|:--------|
| __Description__ | Specifies the regular expression to use to extract the host name/IP address information out of the greeting string advertised by primary<br>IMAP server. Only applicable for primary IMAP server! Default is empty.<br><br>The regular expression is supposed to be specified in Java notation: http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html<br><br>Moreover, either the complete regex is considered or in case a capturing group is present that group will be preferred.<br>I.e. "Dovecot at ([0-9a-zA-Z._-]\*) is ready", then the capturing group is supposed to extract the host name/IP addres information<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.share.notification.usePersonalEmailAddress |
|:----------------|:--------|
| __Description__ | Specifies whether the user's personal E-Mail address (true) or the configured no-reply address (false) is supposed to be used in case a user<br>without mail permission sends out a sharing invitation<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.mailaccount.failedAuth.limit |
|:----------------|:--------|
| __Description__ | Specifies the max. number of failed authentication attempts until the associated mail account is disabled.<br> |
| __Default__ | 5 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mailaccount.failedAuth.span |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | mailaccount.properties |

---
| __Key__ | com.openexchange.mailaccount.failedAuth.span |
|:----------------|:--------|
| __Description__ | Specifies the time span in which the failed authentication attempts are tracked.<br>The value accepts known time span syntax like "1W" or "5m"<br> |
| __Default__ | 30m |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mailaccount.failedAuth.limit |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mailaccount.properties |

---
| __Key__ | com.openexchange.pns.mobile.api.facade.apn.badge.enabled |
|:----------------|:--------|
| __Description__ | Specifies if badges are enabled when using push notifications for the OX Mail app for iOS.<br>These get displayed on the app icon.<br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns-mobile-api-facade.properties |

---
| __Key__ | com.openexchange.pns.mobile.api.facade.apn.sound.enabled |
|:----------------|:--------|
| __Description__ | Specifies if a sound should be played when the OX Mail app on iOS receives a push notification.<br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns-mobile-api-facade.properties |

---
| __Key__ | com.openexchange.pns.mobile.api.facade.apn.sound.filename |
|:----------------|:--------|
| __Description__ | Specifies the filename of the sound to play when a push notification is received in the OX Mail app on iOS.<br>This file needs to be included in the app, otherwise a default sound is played. the string 'default' also causes<br>the default iOS sound to be played.<br> |
| __Default__ | default |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | pns-mobile-api-facade.properties |

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
