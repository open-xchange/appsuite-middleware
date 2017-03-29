---
title: Mail
---

This page shows all properties with the tag: Mail

| __Key__ | com.openexchange.mail.categories |
|:----------------|:--------|
| __Description__ | General capability to enable/disable mail categories for primary inbox<br> |
| __Default__ | false |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.enabled |
|:----------------|:--------|
| __Description__ | Switch to show or hide mail categories feature during the first start. <br>Notice that this property only influence the starting value. <br>Changing this value will probably have no effect on users with already have "com.openexchange.mail.categories" set to true.<br> |
| __Default__ | true |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.forced |
|:----------------|:--------|
| __Description__ | Switch to force showing the mail categories feature. <br>If set to true, the com.openexchange.mail.categories.enabled property is always true and can't be changed.<br> |
| __Default__ | false |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.general.name.fallback |
|:----------------|:--------|
| __Description__ | The fallback name of the default general category.<br> |
| __Default__ | General |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.general.name.[locale] |
|:----------------|:--------|
| __Description__ | For each language which should be supported a translated name for the general category should be defined.<br>For each entry [locale] must be replaced with a ISO-639-2 language code followed by a underscore followed by a ISO-3166 country code (e.g. de_DE or en_US)<br><br>NOTE: Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe"<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.identifiers |
|:----------------|:--------|
| __Description__ | Specifies a comma separated list of system category identifiers ([category]).<br><br>System categories can be forced but not renamed.<br>Please note that the use of "general" is prohibited!<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.user.categories.identifiers |
|:----------------|:--------|
| __Description__ | Specifies a comma separated list of user category identifiers ([category]). E.g.: "uc1,uc2,uc3"<br><br>User categories can be renamed but not be forced.<br>Please note that the use of "general" is prohibited!<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].flag |
|:----------------|:--------|
| __Description__ | Specifies the category's flag name that is supposed to be used for filter/search expressions executed by mail back-end;<br>e.g. "com.openexchange.mail.categories.offers.flag=$offers"<br>Required. <br>[category] must be replaced with the actual category identifier.<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].force |
|:----------------|:--------|
| __Description__ | Specifies whether the category is forced; meaning a user is not allowed to disable the category.<br>Required. Only for system categories.<br>[category] must be replaced with the actual category identifier.<br> |
| __Default__ | false |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].active |
|:----------------|:--------|
| __Description__ | Specifies whether the category is activated/deactivate for a user. Only effective if "force" is set to "false".<br>This setting can be set by a user.<br>Required.<br>[category] must be replaced with the actual category identifier.<br> |
| __Default__ | true |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].name.fallback |
|:----------------|:--------|
| __Description__ | Specifies the category's fall-back name.<br>Required.<br>[category] must be replaced with the actual category identifier.<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].name.[locale] |
|:----------------|:--------|
| __Description__ | For each language which should be supported a translated name for the category should be defined.<br>For each entry [locale] must be replaced with a ISO-639-2 language code followed by a underscore followed by a ISO 3166 country code (e.g. de_DE or en_US).<br>[category] must be replaced with the actual category identifier.<br><br>NOTE: Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe"<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].description |
|:----------------|:--------|
| __Description__ | Specifies an optional system category description.<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].description.[locale] |
|:----------------|:--------|
| __Description__ | For each language which should be supported a translated description for the category should be defined.<br>For each entry [locale] must be replaced with a ISO-639-2 language code followed by a underscore followed by a ISO 3166 country code (e.g. de_DE or en_US).<br>[category] must be replaced with the actual category identifier.<br><br>NOTE: Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe"<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.apply.ox.rules |
|:----------------|:--------|
| __Description__ | It is possible to create some predefined rules for the system categories (see com.openexchange.mail.categories.rules.[category]).<br>This rules will be added for each user which has the mail_categories capability and has the mail_categories feature enabled.<br>This property enables/disables this feature. Nevertheless it is strongly recommended to use system wide rules instead.<br>Please notice that these rules must only be used instead of system wide rules! Don't use this feature if there are already system wide rules defined!       <br> |
| __Default__ | false |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.categories.rules.[category] |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.rules.[category] |
|:----------------|:--------|
| __Description__ | For each system category a comma separated list of mail addresses can be defined. <br>This addresses will be used to create a starting rule for this category if com.openexchange.mail.categories.apply.ox.rules is set to 'true'.  <br>It is also possible to use mail address parts here. For example "@amazon.com".<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.categories.apply.ox.rules |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.imap.host |
|:----------------|:--------|
| __Description__ | Specifies the IMAP host name.<br>If not set, falls-back to internal settings for accessing the primary account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.imap.port |
|:----------------|:--------|
| __Description__ | Specifies the IMAP port.<br>If not set, falls-back to internal settings for accessing the primary account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.imap.secure |
|:----------------|:--------|
| __Description__ | Specifies whether a secure connection is supposed to established to access the IMAP server.<br>If not set, falls-back to internal settings for accessing the primary account.<br><br>Possible values: true&#124;false<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.smtp.host |
|:----------------|:--------|
| __Description__ | Specifies the SMTP host name.<br>If not set, falls-back to internal settings for accessing the primary account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.smtp.port |
|:----------------|:--------|
| __Description__ | Specifies the SMTP port.<br>If not set, falls-back to internal settings for accessing the primary account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.smtp.secure |
|:----------------|:--------|
| __Description__ | Specifies whether a secure connection is supposed to established to access the SMTP server.<br>If not set, falls-back to internal settings for accessing the primary account.<br><br>Possible values: true&#124;false<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
| __File__ | client-onboarding-mail.properties |

---
| __Key__ | com.openexchange.client.onboarding.mailapp.store.google.playstore |
|:----------------|:--------|
| __Description__ | Specifies the URL to Google Play Store for the Mail App.<br> |
| __Default__ | https://play.google.com/store/apps/details?id=com.openexchange.mobile.mailapp.enterprise |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | client-onboarding-mailapp.properties |

---
| __Key__ | com.openexchange.client.onboarding.mailapp.store.apple.appstore |
|:----------------|:--------|
| __Description__ | Specifies the URL to Apple App Store for the Mail App.<br> |
| __Default__ | https://itunes.apple.com/us/app/ox-mail/id1008644994 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | client-onboarding-mailapp.properties |

---
| __Key__ | com.openexchange.mail.loginSource |
|:----------------|:--------|
| __Description__ | Set the login source for primary mail/transport account; meaning which source is taken to determine a user's<br>login for mailing system. If 'login' is set, then user's individual mail login<br>as defined in user storage is taken. If 'mail' is set, then user's individual<br>primary email address is taken. If 'name' is set, then user's individual system's<br>user name is taken.<br>Currently known values: login, mail, and name<br> |
| __Default__ | login |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.passwordSource |
|:----------------|:--------|
| __Description__ | Set the password source for primary mail/transport account; meaning which source is taken to determine a user's<br>password to login into mailing system. If 'session' is set, then user's individual<br>system's password is taken. If 'global' is set, then the value specified through<br>property 'com.openexchange.mail.masterPassword' is taken.<br>Currently known values: session and global<br> |
| __Default__ | session |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailServerSource |
|:----------------|:--------|
| __Description__ | Set the mail server source for primary mail account; meaning which source is taken to determine the mail<br>server into which the user wants to login to access mails. Set to 'global' to take<br>the value specified through property "com.openexchange.mail.mailServer". Set to<br>'user' to take user's individual mail server settings as specified in storage.<br>Currently known values: user and global<br> |
| __Default__ | user |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transportServerSource |
|:----------------|:--------|
| __Description__ | Set the transport server source for primary mail account; meaning which source is taken to determine the transport<br>server into which the user wants to login to transport mails. Set to 'global' to take<br>the value specified through property "com.openexchange.mail.transportServer". Set to<br>'user' to take user's individual transport server settings as specified in storage.<br>Currently known values: user and global<br> |
| __Default__ | user |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.masterPassword |
|:----------------|:--------|
| __Description__ | The master password for primary mail/transport server. Only takes effect when property<br>"com.openexchange.mail.passwordSource" is set to "global"<br> |
| __Default__ | secret |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailServer |
|:----------------|:--------|
| __Description__ | Primary mail server: e.g. 192.168.178.32:8143 or imap://192.168.178.32:7143<br>Only takes effect when property "com.openexchange.mail.mailServerSource" is set to "global"<br> |
| __Default__ | imap://localhost |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transportServer |
|:----------------|:--------|
| __Description__ | Primary transport server: e.g. 192.168.178.32:125 or smtp://192.168.178.32:225<br>Only takes effect when property "com.openexchange.mail.transportServerSource" is set to "global"<br> |
| __Default__ | 127.0.0.1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailStartTls |
|:----------------|:--------|
| __Description__ | Set if STARTTLS should be used when connecting to the primary mail server<br>Only takes effect when property "com.openexchange.mail.mailServerSource" is set to "global"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transportStartTls |
|:----------------|:--------|
| __Description__ | Set if STARTTLS should be used when connecting to the primary transport server<br>Only takes effect when property "com.openexchange.mail.transportServerSource" is set to "global" <br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.defaultMailProvider |
|:----------------|:--------|
| __Description__ | The mail provider fallback if an URL does not contain/define a protocol<br> |
| __Default__ | imap |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | mail.mime.charset |
|:----------------|:--------|
| __Description__ | Define the default MIME charset used for character encoding. This setting will then be<br>accessible through system property "mail.mime.charset". This parameter takes<br>effect for the complete mail module where no charset is given.<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.saneLogin |
|:----------------|:--------|
| __Description__ | Controls whether a login string is supposed to be converted to its ACE representation in case it is an E-Mail address;<br>e.g. "someone@m&uuml;ller.de" is converted to "someone@xn--mller-kva.de"<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.replaceWithComma |
|:----------------|:--------|
| __Description__ | Controls a possibly semi-colon-separated address list should be converted to comma-separated one<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailFetchLimit |
|:----------------|:--------|
| __Description__ | Define the max. fetch limit; meaning all mails whose count is less than or equal to<br>this value are going to be fetched with all attributes set. Thus these mails can be<br>put into message cache for subsequent list requests.<br> |
| __Default__ | 1000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.bodyDisplaySizeLimit |
|:----------------|:--------|
| __Description__ | Specifies the maximum size for message bodies that are allowed to be displayed.<br>Default is 10485760 (10 MB)<br> |
| __Default__ | 10485760 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.attachmentDisplaySizeLimit |
|:----------------|:--------|
| __Description__ | Specifies the maximum size (in bytes) for email text attachments that will be displayed inline<br>Default is 8192 (8 KB)<br> |
| __Default__ | 8192 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailAccessCacheShrinkerSeconds |
|:----------------|:--------|
| __Description__ | Define the interval seconds of the mail access cache's shrinker thread<br> |
| __Default__ | 3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.mailAccessCacheIdleSeconds |
|:----------------|:--------|
| __Description__ | Define the idle seconds a mail access may reside in mail access cache before it is removed by shrinker thread.<br> |
| __Default__ | 4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.quoteLineColors |
|:----------------|:--------|
| __Description__ | Comma-separated hex values of colors for displaying quoted text emails<br>Only used by OX6 UI<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.forwardUnquoted |
|:----------------|:--------|
| __Description__ | Define if forwarded text is supposed to be quoted<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.userFlagsEnabled |
|:----------------|:--------|
| __Description__ | Enable/disable user defined flags<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.allowNestedDefaultFolderOnAltNamespace |
|:----------------|:--------|
| __Description__ | This property defines if the default folders of an user (Draft, Sent, Spam<br>& Trash) are going to be placed right below folder "INBOX" even if<br>feature "altNamespace" is enabled. NOTE: This property requires that<br>subfolders are still permitted below initial folder "INBOX" even though<br>"altNamespace" is enabled.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.ignoreSubscription |
|:----------------|:--------|
| __Description__ | Defines if folder subscription is ignored when listing mail folders<br>If set to 'true', all folders - regardless of their subscription status - are<br>going to be listed<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.supportSubscription |
|:----------------|:--------|
| __Description__ | Define if underlying store should support subscription<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.defaultSeparator |
|:----------------|:--------|
| __Description__ | Define the separator within folder full names if not available from mail server<br> |
| __Default__ | / |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.watcherEnabled |
|:----------------|:--------|
| __Description__ | The watcher checks after watcherFrequency for mail connections used for<br>more than watcherTime milliseconds and logs this mail connection. If<br>watcherShallClose is set to true those connections will be closed.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.watcherFrequency |
|:----------------|:--------|
| __Description__ | Define watcher's frequency in milliseconds<br> |
| __Default__ | 10000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.watcherTime |
|:----------------|:--------|
| __Description__ | Define exceeding time in milliseconds for mail connections. If use time<br>of an mail connection exceeds this value it is logged. Thus unclosed<br>connections can be detected.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.watcherShallClose |
|:----------------|:--------|
| __Description__ | Define if watcher is allowed to close exceeded mail connections<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.JavaMailProperties |
|:----------------|:--------|
| __Description__ | JavaMail Properties<br> |
| __Default__ | javamail.properties |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.phishingHeader |
|:----------------|:--------|
| __Description__ | A comma-separated list of headers which identifies phishing headers; e.g.<br>X-Phishing1,X-Phishing2,etc.<br>Leave empty for no phishing header.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.adminMailLoginEnabled |
|:----------------|:--------|
| __Description__ | Define whether a context admin is allowed to login to mail system or not.<br>Note that a mail account is supposed to exist if set to true; if not an<br>authentication error will occur.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.addClientIPAddress |
|:----------------|:--------|
| __Description__ | Set whether client's IP address should be added to mail headers on delivery<br>as custom header "X-Originating-IP"<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.rateLimitPrimaryOnly |
|:----------------|:--------|
| __Description__ | Define if the rateLimit and maxToCcBcc settings below will only affect<br>the primary account or all accounts<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.rateLimit |
|:----------------|:--------|
| __Description__ | Define the time (in milliseconds) which must pass by before a new mail can be sent<br>A value of 0 disables the limit.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.rateLimitDisabledRange |
|:----------------|:--------|
| __Description__ | Define the comma-separated IP ranges for which a rate limit will not be applied.<br>Default is empty<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maxToCcBcc |
|:----------------|:--------|
| __Description__ | Define the allowed maximum number of recipients in a mail<br>A value of 0 disables the limit.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.hidePOP3StorageFolders |
|:----------------|:--------|
| __Description__ | Whether folders which carry a POP3 account shall not be displayed.<br>This property affects primary account only.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.deleteDraftOnTransport |
|:----------------|:--------|
| __Description__ | Whether to delete draft messages when sent out<br>Note: Client MUST provide appropriate value in "sendtype" field;<br>see http://oxpedia.org/wiki/index.php?title=HTTP_API#Send_a_mail<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.supportMsisdnAddresses |
|:----------------|:--------|
| __Description__ | Define if MSISDN addresses are supported or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.translateDefaultFolders |
|:----------------|:--------|
| __Description__ | If set to 'false', translation of names of the mail default folders is not performed,<br>if naming differs from pre-defined default values. Thus custom set names can be specified<br>for Trash, Drafts, Sent & Spam folder.<br>By default this value is 'true'.<br>Pre-defined names are:<br>"Trash"<br>"Drafts"<br>"Sent objects"<br>"Spam"<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.authProxyDelimiter |
|:----------------|:--------|
| __Description__ | Define a delimiter to be used to seperate Proxy from Userlogin<br>If defined, a proxy user can login on behalf of a user using the form<br><PROXYACCOUNT><DELIMITER><USERACCOUNT><br>NOTE: The underlying Mailserver must support SASL AUTHPROXYING<br>The open-xchange mailfilterbundle does not support it, so it will raise errors<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maxMailSize |
|:----------------|:--------|
| __Description__ | Specifies the max. mail size allowed being transported<br>A value of zero or less means infinite.<br>Default is -1 (infinite)<br> |
| __Default__ | -1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.hideDetailsForDefaultAccount |
|:----------------|:--------|
| __Description__ | Whether to hide rather technical data from JSON representation of the primary mail account<br>e.g. port, server name, secure flag, etc.<br>Default is false<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maxForwardCount |
|:----------------|:--------|
| __Description__ | Specifies the max. number of message attachments that are allowed to be forwarded as attachment<br>Default is 8<br> |
| __Default__ | 8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.account.blacklist |
|:----------------|:--------|
| __Description__ | Specifies a black-list for those hosts that are covered by denoted IP range; e.g. "127.0.0.1-127.255.255.255, localhost, internal.domain.org"<br>Creation of mail accounts with this hosts will be prevented. Also the validation of those accounts will fail.<br>An empty value means no black-listing is active<br>Default is "127.0.0.1-127.255.255.255,localhost"<br> |
| __Default__ | 127.0.0.1-127.255.255.255,localhost |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Account.html">Mail Account</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.account.whitelist.ports |
|:----------------|:--------|
| __Description__ | Specifies a white-list for such ports that are allowed to connect against when setting up/validating an external mail account<br>An empty value means no white-listing is active<br>Default is: 143,993, 25,465,587, 110,995<br> |
| __Default__ | 143,993, 25,465,587, 110,995 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Account.html">Mail Account</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.imageHost |
|:----------------|:--------|
| __Description__ | Specifies the host/domain from which to load inlined images contained in message content<br>Example "com.openexchange.mail.imageHost=http://my.imagehost.org".<br>In case no protocol/schema is specified, "http" is assumed by default<br>Default is empty; meaning to load from originating host<br>Exemplary setup:<br>- Artificially add a host name to /etc/hosts:<br>  127.0.0.1     imageserver.open-xchange.com<br>  - Enable the "com.openexchange.mail.imageHost" property in mail.properties:<br>    com.openexchange.mail.imageHost=http://imageserver.open-xchange.com<br>    - Check a mail with an inline image<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.signature.maxImageSize |
|:----------------|:--------|
| __Description__ | Specifies the maximum size (in MB) for one image contained in the HTML mail signature<br> |
| __Default__ | 1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Signature.html">Signature</a> |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.signature.maxImageLimit |
|:----------------|:--------|
| __Description__ | Specified the maximum amount of images that are allowed in a single HTML mail signature<br> |
| __Default__ | 3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Signature.html">Signature</a> |
| __File__ | mail.properties |

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
| __Key__ | com.openexchange.mailmapping.lookUpByDomain |
|:----------------|:--------|
| __Description__ | This option specifies how look-up of mail addresses is performed.<br><br>Setting this option to "true" means that the domain part of a mail address (the part after the "@" sign) is used to find a matching<br>context by checking the login mappings. That mechanism does only work if Open-Xchange setup strictly defines a dedicated and unique domain<br>per context. Otherwise that look-up mechanism will lead to wrong results.<br><br>Setting this option to "false" means that the mail address is going to be looked-up on a per database schema basis. For each known schema<br>a query is performed to check whether there is such an internal user.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mailresolver.properties |

---
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
| __Key__ | notify_participants_on_delete |
|:----------------|:--------|
| __Description__ | If set to 'true' all participants will be notified when the appointment or task is deleted <br>with the exception of the person deleting the appointment/task.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | notification.properties |

---
| __Key__ | imipForInternalUsers |
|:----------------|:--------|
| __Description__ | Enables/Disables imip-mails for internal users.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | notification.properties |

---
| __Key__ | com.openexchange.notification.fromSource |
|:----------------|:--------|
| __Description__ | This property defines which email address of a user is used as from header when this user triggers notification mails.<br>Possible values are: "primaryMail" or "defaultSenderAddress".<br> |
| __Default__ | defaultSenderAddress |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | notification.properties |

---
| __Key__ | com.openexchange.calendar.notify.poolenabled |
|:----------------|:--------|
| __Description__ | This property enables/disables the notification Pool for Appointment notifications and invitations.<br>If enabled, changes are combined and mails are summarized according different heuristics.<br>If disabled, changes are sent immediately.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Notification.html">Notification</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Task.html">Task</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | notification.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpLocalhost |
|:----------------|:--------|
| __Description__ | The localhost name that is going to be used on SMTP's HELO or EHLO command.<br>The default is set to InetAddress.getLocalHost().getHostName() but if either JDK or name service are not<br>configured properly, this routine fails and the HELO or EHLO command is send without a name which<br>leads to an error: "501 HELO requires domain address"<br>The value "null" falls back to InetAddress.getLocalHost().getHostName() which works in most cases.<br>Default is "null"<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpAuthEnc |
|:----------------|:--------|
| __Description__ | Define the encoding for SMTP authentication<br>Default is UTF-8<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp.setSMTPEnvelopeFrom |
|:----------------|:--------|
| __Description__ | Defines if SMTP header ENVELOPE-FROM should be explicitly set to<br>user's primary email address or not<br>Default is false<br>Applies only to primary SMTP account; considered as "false" for every external SMTP account.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpTimeout |
|:----------------|:--------|
| __Description__ | Define the socket read timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.smtp.timeout<br>Default is 50000<br> |
| __Default__ | 50000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].smtpConnectionTimeout |
|:----------------|:--------|
| __Description__ | Define the socket connect timeout value in milliseconds. A value less<br>or equal to zero is infinite timeout. See also mail.smtp.connectiontimeout<br>Default is 10000<br> |
| __Default__ | 10000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp.logTransport |
|:----------------|:--------|
| __Description__ | Specifies whether a transported message shall be logged providing "Message-Id" header, login, and SMTP server information<br>Default is false<br>Applies only to primary SMTP account; considered as "false" for every external SMTP account<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].ssl.protocols |
|:----------------|:--------|
| __Description__ | Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br>Default is empty<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp[.primary].ssl.ciphersuites |
|:----------------|:--------|
| __Description__ | Specifies the SSL cipher suites that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br><br>Check "http://<ox-grizzly-hostname>:<ox-grizzly-port>/stats/diagnostic?param=ciphersuites" to check available cipher suites.<br><br>Default value is empty (fall-back to current JVM's default SSL cipher suite)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.smtp.sendPartial |
|:----------------|:--------|
| __Description__ | Whether partial send is allowed or message transport is supposed to be aborted<br>Default is "false"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.path |
|:----------------|:--------|
| __Description__ | Path to the local configuration files for mail domains.<br>See https://developer.mozilla.org/en/Thunderbird/Autoconfiguration<br> |
| __Default__ | /opt/open-xchange/ispdb |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.ispdb |
|:----------------|:--------|
| __Description__ | The ISPDB is a central database, currently hosted by Mozilla Messaging, but free to use for any client.<br>It contains settings for the world's largest ISPs.<br>We hope that the database will soon have enough information to autoconfigure approximately 50% of our user's email accounts.<br> |
| __Default__ | https://live.mozillamessaging.com/autoconfig/v1.1/ |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy |
|:----------------|:--------|
| __Description__ | Provides the possibility to specify a proxy that is used to access any HTTP end-points. If empty, no proxy is used.<br>Notation is: <optional-protocol> + "://" + <proxy-host> + ":" + <proxy-port><br>             With "http" as fall-back protocol<br>E.g. "67.177.104.230:58720" (using HTTP protocol) or "https://78.0.25.45:8345" (using HTTPS protocol)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy.login |
|:----------------|:--------|
| __Description__ | Specifies the login/username to use in case specified proxy in property "com.openexchange.mail.autoconfig.http.proxy"<br>requires authentication.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.autoconfig.http.proxy.password, com.openexchange.mail.autoconfig.http.proxy |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.http.proxy.password |
|:----------------|:--------|
| __Description__ | Specifies the password to use in case specified proxy in property "com.openexchange.mail.autoconfig.http.proxy"<br>requires authentication.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.autoconfig.http.proxy.login, com.openexchange.mail.autoconfig.http.proxy |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.mail.autoconfig.allowGuess |
|:----------------|:--------|
| __Description__ | Specifies whether it is allowed to "guess" the mail/transport settings.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Autoconfig.html">Autoconfig</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | autoconfig.properties |

---
| __Key__ | com.openexchange.find.basic.mail.allMessagesFolder |
|:----------------|:--------|
| __Description__ | Some mail backends provide a virtual folder that contains all messages of<br>a user to enable cross-folder mail search. Open-Xchange can make use of<br>this feature to improve the search experience.<br>Set the value to the name of the virtual mail folder containing all messages.<br>Leave blank if no such folder exists.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Find.html">Find</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | findbasic.properties |

---
| __Key__ | com.openexchange.find.basic.mail.searchmailbody |
|:----------------|:--------|
| __Description__ | Denotes if mail search queries should be matched against mail bodies.<br>This improves the search experience within the mail module, if your mail<br>backend supports fast full text search. Otherwise it can slow down the<br>search requests significantly.<br>Change the value to 'true', if fast full text search is supported.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Find.html">Find</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | findbasic.properties |

---
| __Key__ | com.openexchange.mail.transport.referencedPartLimit |
|:----------------|:--------|
| __Description__ | Define the limit in bytes for keeping an internal copy of a referenced<br>MIME message's part when sending a mail. If a part exceeds this limit<br>a temporary file is created holding part's copy.<br> |
| __Default__ | 1048576 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | transport.properties |

---
| __Key__ | com.openexchange.mail.defaultTransportProvider |
|:----------------|:--------|
| __Description__ | The transport provider fallback if an URL does not contain/define a protocol.<br> |
| __Default__ | smtp |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | transport.properties |

---
| __Key__ | com.openexchange.mail.transport.removeMimeVersionInSubParts |
|:----------------|:--------|
| __Description__ | Specify whether to strictly obey suggestion in RFC 2045.<br>The MIME-Version header field is required at the top level of a message, but is _not_ required for each body part of a multipart entity.<br>If set to "true", each message is processed to not contain a MIME-Version header in sub-parts.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | transport.properties |

---
| __Key__ | com.openexchange.userfeedback.mail.senderName |
|:----------------|:--------|
| __Description__ | The sender name, that should be displayed to the recepients.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.mail.senderAddress |
|:----------------|:--------|
| __Description__ | The address that should be displayed for the recepients.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.hostname |
|:----------------|:--------|
| __Description__ | The SMTP server to connect to<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.port |
|:----------------|:--------|
| __Description__ | The SMTP server port to connect to.<br> |
| __Default__ | 587 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.timeout |
|:----------------|:--------|
| __Description__ | Socket read timeout value in milliseconds.<br> |
| __Default__ | 50000 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.connectionTimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | 10000 |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.username |
|:----------------|:--------|
| __Description__ | Default username for SMTP.<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.smtp.password |
|:----------------|:--------|
| __Description__ | Password for the provided username<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.pgp.signKeyFile |
|:----------------|:--------|
| __Description__ | Path to PGP secret key used to sign mails<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.pgp.signKeyPassword |
|:----------------|:--------|
| __Description__ | Password for PGP secret key used to sign mails<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.push.malpoll.period |
|:----------------|:--------|
| __Description__ | Define the amount of time in milliseconds when to periodically check for new mails.<br> |
| __Default__ | 300000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MAL_Poll.html">MAL Poll</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | malpoll.properties |

---
| __Key__ | com.openexchange.push.malpoll.global |
|:----------------|:--------|
| __Description__ | Whether a global timer is set or a timer per user.<br>Or in other words: Do you want a global heartbeat or a heartbeat per user?<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MAL_Poll.html">MAL Poll</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a> |
| __File__ | malpoll.properties |

---
| __Key__ | com.openexchange.push.malpoll.concurrentglobal |
|:----------------|:--------|
| __Description__ | Whether the tasks executed by global timer are executed concurrently<br>or by calling timer's thread.<br>Note: This property only has effect if "com.openexchange.push.malpoll.global"<br>is set to "true"<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.push.malpoll.global |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MAL_Poll.html">MAL Poll</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Thread.html">Thread</a> |
| __File__ | malpoll.properties |

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
| __Key__ | com.openexchange.authentication.ucs.mailAttribute |
|:----------------|:--------|
| __Description__ | Specifies the attribute containing the email address from which domain part will be used to identify the context.<br> |
| __Default__ | mailPrimaryAddress |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Univention.html">Univention</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | authplugin.properties |

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
| __Key__ | CHECK_AND_REMOVE_PAST_REMINDERS |
|:----------------|:--------|
| __Description__ | If this option is enabled no event is triggered<br>and no mail will be sent if the reminder is in<br>the past relative to the start date.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Event.html">Event</a> |
| __File__ | calendar.properties |

---
| __Key__ | CHECK_AND_AVOID_SOLO_REMINDER_TRIGGER_EVENTS |
|:----------------|:--------|
| __Description__ | This option prevents the trigger and mail sending<br>if only a reminder has been changed. If the application<br>should inform about each change no matter what has been<br>changed in the object this option should be disabled.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Calendar.html">Calendar</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Event.html">Event</a> |
| __File__ | calendar.properties |

---
| __Key__ | mail.debug |
|:----------------|:--------|
| __Description__ | The initial debug mode<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.from |
|:----------------|:--------|
| __Description__ | The return email address of the current user, used by the InternetAddress method getLocalAddress.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.address.strict |
|:----------------|:--------|
| __Description__ | The MimeMessage class uses the InternetAddress method parseHeader to parse headers in messages.<br>This property controls the strict flag passed to the parseHeader method.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.host |
|:----------------|:--------|
| __Description__ | The default host name of the mail server for both Stores and Transports. Used if the mail.protocol.host property isn't set.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.[protocol].host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.protocol |
|:----------------|:--------|
| __Description__ | Specifies the default message access protocol. The Session method getStore() returns a Store object that implements this protocol.<br>By default the first Store provider in the configuration files is returned.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.transport.protocol |
|:----------------|:--------|
| __Description__ | Specifies the default message access protocol. The Session method getTransport() returns a Transport object that implements this protocol.<br>By default the first Transport provider in the configuration files is returned.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.user |
|:----------------|:--------|
| __Description__ | The default user name to use when connecting to the mail server. Used if the mail.protocol.user property isn't set.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.protocol.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.[protocol].class |
|:----------------|:--------|
| __Description__ | Specifies the fully qualified class name of the provider for the specified protocol.<br>Used in cases where more than one provider for a given protocol exists; this property can be used to specify which provider to use by default.<br>The provider must still be listed in a configuration file.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.[protocol].host |
|:----------------|:--------|
| __Description__ | The host name of the mail server for the specified protocol. Overrides the mail.host property.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.[protocol].port |
|:----------------|:--------|
| __Description__ | The port number of the mail server for the specified protocol. If not specified the protocol's default port number is used.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.[protocol].user |
|:----------------|:--------|
| __Description__ | The user name to use when connecting to mail servers using the specified protocol. Overrides the mail.user property.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.charset |
|:----------------|:--------|
| __Description__ | The default charset to be used by JavaMail. If not set (the normal case), the standard J2SE file.encoding System property is used.<br>This allows applications to specify a default character set for sending messages that's different than the character set used for files stored on the system.<br>This is common on Japanese systems.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodetext.strict |
|:----------------|:--------|
| __Description__ | RFC 2047 requires that encoded text start at the beginning of a whitespace separated word.<br>Some mailers, especially Japanese mailers, improperly encode text and include encoded text in the middle of words.<br>This property controls whether JavaMail will attempt to decode such incorrectly encoded text.<br>The default is true, meaning that JavaMail wil not attempt to decode such improperly decoded text.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodeeol.strict |
|:----------------|:--------|
| __Description__ | When choosing an encoding for the data of a message, JavaMail assumes that any of CR, LF, or CRLF are valid line<br>terminators in message parts that contain only printable ASCII characters, even if the part is not a MIME text type.<br>It's common, especially on UNIX systems, for data of MIME type application/octet-stream (for example) to really be<br>textual data that should be transmitted with the encoding rules for MIME text. In rare cases, such pure ASCII text<br>may in fact be binary data in which the CR and LF characters must be preserved exactly.<br>If this property is set to true, JavaMail will consider a lone CR or LF in a body part that's not a MIME text type to<br>indicate that the body part needs to be encoded.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.user |
|:----------------|:--------|
| __Description__ | Default user name for SMTP<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.host |
|:----------------|:--------|
| __Description__ | The SMTP server to connect to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.port |
|:----------------|:--------|
| __Description__ | The SMTP server port to connect to, if the connect() method doesn't explicitly specify one.<br> |
| __Default__ | 25 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.connectiontimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.timeout |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.from |
|:----------------|:--------|
| __Description__ | Email address to use for SMTP MAIL command. This sets the envelope return address.<br> Defaults to msg.getFrom() or InternetAddress.getLocalAddress(). NOTE: mail.smtp.user was previously used for this. <br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.smtp.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.localhost |
|:----------------|:--------|
| __Description__ | Local host name. Defaults to InetAddress.getLocalHost().getHostName().<br>Should not normally need to be set if your JDK and your name service are configured properly.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.ehlo |
|:----------------|:--------|
| __Description__ | If false, do not attempt to sign on with the EHLO command. Normally failure of the EHLO command will<br>fallback to the HELO command; this property exists only for servers that don't fail EHLO properly or<br>don't implement EHLO properly.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.auth |
|:----------------|:--------|
| __Description__ | If true, attempt to authenticate the user using the AUTH command.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.dsn.notify |
|:----------------|:--------|
| __Description__ | The NOTIFY option to the RCPT command. Either NEVER, or some combination of SUCCESS, FAILURE, and DELAY (separated by commas).<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.dns.ret |
|:----------------|:--------|
| __Description__ | The RET option to the MAIL command. Either FULL or HDRS.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.allow8bitmime |
|:----------------|:--------|
| __Description__ | If set to true, and the server supports the 8BITMIME extension, text parts of messages that use the "quoted-printable" or "base64"<br>encodings are converted to use "8bit" encoding if they follow the RFC2045 rules for 8bit text. <br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.sendpartial |
|:----------------|:--------|
| __Description__ | If set to true, and a message has some valid and some invalid addresses, send the message anyway, reporting the partial failure with<br>a SendFailedException. If set to false , the message is not sent to any of the recipients if there is an invalid recipient<br>address.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.saslrealm |
|:----------------|:--------|
| __Description__ | The realm to use with DIGEST-MD5 authentication.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.quitwait |
|:----------------|:--------|
| __Description__ | If set to true, causes the transport to wait for the response to the QUIT command.<br>If set to false, the QUIT command is sent and the connection is immediately closed.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.reportsuccess |
|:----------------|:--------|
| __Description__ | If set to true, causes the transport to include an SMTPAddressSucceededException for each address that is successful.<br>Note also that this will cause a SendFailedException to be thrown from the sendMessage method of SMTPTransport even if all addresses were<br>correct and the message was sent successfully.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.mailextension |
|:----------------|:--------|
| __Description__ | Extension string to append to the MAIL command. The extension string can be used to specify standard SMTP service extensions as well as vendor-specific extensions.<br>Typically the application should use the SMTPTransport method supportsExtension to verify that the server supports the desired service extension.<br>See RFC 1869 and other RFCs that define specific extensions.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.userset |
|:----------------|:--------|
| __Description__ | If set to true, use the RSET command instead of the NOOP command in the isConnected method.<br>In some cases sendmail will respond slowly after many NOOP commands; use of RSET avoids this sendmail issue.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.user |
|:----------------|:--------|
| __Description__ | Default user name for IMAP.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.host |
|:----------------|:--------|
| __Description__ | The IMAP server to connect to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.port |
|:----------------|:--------|
| __Description__ | The IMAP server port to connect to, if the connect() method doesn't explicitly specify one.<br> |
| __Default__ | 143 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.partialfetch |
|:----------------|:--------|
| __Description__ | Controls whether the IMAP partial-fetch capability should be used.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.fetchsize |
|:----------------|:--------|
| __Description__ | Partial fetch size in bytes.<br> |
| __Default__ | 16K |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.connectiontimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.timeout |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.statuscachetimeout |
|:----------------|:--------|
| __Description__ | Timeout value in milliseconds for cache of STATUS command response. Default is 1000 (1 second). Zero disables cache.<br> |
| __Default__ | 1000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.appendbuffersize |
|:----------------|:--------|
| __Description__ | Maximum size of a message to buffer in memory when appending to an IMAP folder.<br>If not set, or set to -1, there is no maximum and all messages are buffered. If set to 0, no messages are buffered.<br>If set to (e.g.) 8192, messages of 8K bytes or less are buffered, larger messages are not buffered.<br>Buffering saves cpu time at the expense of short term memory usage. If you commonly append very large messages to<br>IMAP mailboxes you might want to set this to a moderate value (1M or less).<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.connectionpoolsize |
|:----------------|:--------|
| __Description__ | Maximum number of available connections in the connection pool.<br> |
| __Default__ | 1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.connectionpooltimeout |
|:----------------|:--------|
| __Description__ | Timeout value in milliseconds for connection pool connections.<br> |
| __Default__ | 45000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.seperatestoreconnection |
|:----------------|:--------|
| __Description__ | Flag to indicate whether to use a dedicated store connection for store commands.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.allowreadonlyselect |
|:----------------|:--------|
| __Description__ | If false, attempts to open a folder read/write will fail if the SELECT command succeeds but indicates that the folder is READ-ONLY.<br>This sometimes indicates that the folder contents can'tbe changed, but the flags are per-user and can be changed, such as might be<br>the case for public shared folders. If true, such open attempts will succeed, allowing the flags to be changed.<br>The getMode method on the Folder object will return Folder.READ_ONLY in this case even though the open method specified Folder.READ_WRITE.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.auth.login.disable |
|:----------------|:--------|
| __Description__ | If true, prevents use of the non-standard AUTHENTICATE LOGIN command, instead using the plain LOGIN command.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.auth.plain.disable |
|:----------------|:--------|
| __Description__ | If true, prevents use of the AUTHENTICATE PLAIN command.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.localaddress |
|:----------------|:--------|
| __Description__ | Local address (host name) to bind to when creating the IMAP socket. Defaults to the address picked by the Socket class.<br>Should not normally need to be set, but useful with multi-homed hosts where it's important to pick a particular local address to bind to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.localport |
|:----------------|:--------|
| __Description__ | Local port number to bind to when creating the IMAP socket. Defaults to the port number picked by the Socket class.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.sasl.enable |
|:----------------|:--------|
| __Description__ | If set to true, attempt to use the javax.security.sasl package to choose an authentication mechanism for login.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.sasl.mechanisms |
|:----------------|:--------|
| __Description__ | A space or comma separated list of SASL mechanism names to try to use.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.imap.sasl.authorizationid |
|:----------------|:--------|
| __Description__ | The authorization ID to use in the SASL authentication. If not set, the authentication ID (user name) is used.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.user |
|:----------------|:--------|
| __Description__ | Default user name for POP3.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.user |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.host |
|:----------------|:--------|
| __Description__ | The POP3 server to connect to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | mail.host |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.port |
|:----------------|:--------|
| __Description__ | The POP3 server port to connect to, if the connect() method doesn't explicitly specify one.<br> |
| __Default__ | 110 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.connectiontimeout |
|:----------------|:--------|
| __Description__ | Socket connection timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.timeout |
|:----------------|:--------|
| __Description__ | Socket I/O timeout value in milliseconds.<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.rsetbeforequit |
|:----------------|:--------|
| __Description__ | Send a POP3 RSET command when closing the folder, before sending the QUIT command. Useful with POP3 servers that implicitly mark all<br>messages that are read as "deleted"; this will prevent such messages from being deleted and expunged unless the client requests so.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.message.class |
|:----------------|:--------|
| __Description__ | Class name of a subclass of com.sun.mail.pop3.POP3Message. The subclass can be used to handle (for example) non-standard Content-Type headers.<br>The subclass must have a public constructor of the form MyPOP3Message(Folder f, int msgno) throws MessagingException.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.localaddress |
|:----------------|:--------|
| __Description__ | Local address (host name) to bind to when creating the POP3 socket. Defaults to the address picked by the Socket class.<br>Should not normally need to be set, but useful with multi-homed hosts where it's important to pick a particular local address to bind to.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.localport |
|:----------------|:--------|
| __Description__ | Local port number to bind to when creating the POP3 socket. Defaults to the port number picked by the Socket class.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.apop.enable |
|:----------------|:--------|
| __Description__ | If set to true, use APOP instead of USER/PASS to login to the POP3 server, if the POP3 server supports APOP.<br>APOP sends a digest of the password rather than the clear text password.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.disabletop |
|:----------------|:--------|
| __Description__ | If set to true, the POP3 TOP command will not be used to fetch message headers. This is useful for POP3 servers that don't properly implement the TOP command,<br>or that provide incorrect information in the TOP command results.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.pop3.forgettopheaders |
|:----------------|:--------|
| __Description__ | If set to true, the headers that might have been retrieved using the POP3 TOP command will be forgotten and replaced by headers retrieved as part of the POP3 RETR command.<br>Some servers, such as some version of Microsft Exchange, will return slightly different headers each time the TOP or RETR command is used.<br>To allow the POP3 provider to properly parse the message content returned from the RETR command, the headers also returned by the RETR command must be used.<br>Setting this property to true will cause these headers to be used, even if they differ from the headers returned previously as a result of using the TOP command.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.autocreatedir |
|:----------------|:--------|
| __Description__ | Whether to auto-create specified maildir directory on maildir store connect or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

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
| __Key__ | mail.store.maildir.checkmessagesizebeforeappend |
|:----------------|:--------|
| __Description__ | Whether to check quota limitations or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.quota.size |
|:----------------|:--------|
| __Description__ | Define the quota limit on STORAGE resource.<br> |
| __Default__ | 0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.quota.count |
|:----------------|:--------|
| __Description__ | Define the quota limit on MESSAGE resource.<br> |
| __Default__ | 0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.headerindex |
|:----------------|:--------|
| __Description__ | Whether to use a header index or not.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.store.maildir.inmemorysize |
|:----------------|:--------|
| __Description__ | Define the maximum RFC822 message size (in bytes) for being kept in memory completely. If a message exceeds this size, its corresponding<br>file is accessed with random access instead of reading file with an input stream.<br> |
| __Default__ | 131072 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
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
| __Key__ | mail.store.maildir.headercacheexpire |
|:----------------|:--------|
| __Description__ | Define the timeout for header caches (in milliseconds).<br> |
| __Default__ | 3600000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.address.strict |
|:----------------|:--------|
| __Description__ | The mail.mime.address.strict session property controls the parsing of address headers.<br>By default, strict parsing of address headers is done. If this property is set to "false",<br>strict parsing is not done and many illegal addresses that sometimes occur in real messages are allowed.<br>See the InternetAddress class for details.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.charset |
|:----------------|:--------|
| __Description__ | The mail.mime.charset System property can be used to specify the default MIME charset to use for encoded words and text parts that don't<br>otherwise specify a charset. Normally, the default MIME charset is derived from the default Java charset, as specified in the file.encoding<br>System property. Most applications will have no need to explicitly set the default MIME charset.<br>In cases where the default MIME charset to be used for mail messages is different than the charset used for files stored on the system,<br>this property should be set.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodetext.strict |
|:----------------|:--------|
| __Description__ | The mail.mime.decodetext.strict property controls decoding of MIME encoded words.<br>The MIME spec requires that encoded words start at the beginning of a whitespace separated word.<br>Some mailers incorrectly include encoded words in the middle of a word.<br>If the mail.mime.decodetext.strict System property is set to "false", an attempt will be made to decode these illegal encoded words.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodeeol.strict |
|:----------------|:--------|
| __Description__ | The mail.mime.encodeeol.strict property controls the choice of Content-Transfer-Encoding for MIME parts that are not of type "text".<br>Often such parts will contain textual data for which an encoding that allows normal end of line conventions is appropriate.<br>In rare cases, such a part will appear to contain entirely textual data, but will require an encoding that preserves CR and LF characters<br>without change. If the mail.mime.encodeeol.strict System property is set to "true", such an encoding will be used when necessary.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodefilename |
|:----------------|:--------|
| __Description__ | If set to "true", the getFileName method uses the MimeUtility method decodeText to decode any non-ASCII characters in the filename.<br>Note that this decoding violates the MIME specification, but is useful for interoperating with some mail clients that use this convention.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodefilename |
|:----------------|:--------|
| __Description__ | If set to "true", the setFileName method uses the MimeUtility method encodeText to encode any non-ASCII characters in the filename.<br>Note that this encoding violates the MIME specification, but is useful for interoperating with some mail clients that use this convention.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodeparameters |
|:----------------|:--------|
| __Description__ | If set to "true", non-ASCII parameters in a ParameterList, e.g., in a Content-Type header, will be encoded as specified by RFC 2231.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodeparameters |
|:----------------|:--------|
| __Description__ | If set to "true", non-ASCII parameters in a ParameterList, e.g., in a Content-Type header, will be decoded as specified by RFC 2231.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.ignoremissingendboundary |
|:----------------|:--------|
| __Description__ | Normally, when parsing a multipart MIME message, a message that is missing the final end boundary line is not considered an error.<br>The data simply ends at the end of the input. Note that messages of this form violate the MIME specification.<br>If the property mail.mime.multipart.ignoremissingendboundary is set to false, such messages are considered an error and a MesagingException<br>will be thrown when parsing such a message.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.ignoremissingboundaryparameter |
|:----------------|:--------|
| __Description__ | If the Content-Type header for a multipart content does not have a boundary parameter, the multipart parsing code will look for the first<br>line in the content that looks like a boundary line and extract the boundary parameter from the line.<br>If this property is set to "false", a MessagingException will be thrown if the Content-Type header doesn't specify a boundary parameter.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.base64.ignoreerrors |
|:----------------|:--------|
| __Description__ | If set to "true", the BASE64 decoder will ignore errors in the encoded data, returning EOF.<br>This may be useful when dealing with improperly encoded messages that contain extraneous data at the end of the encoded stream.<br>Note however that errors anywhere in the stream will cause the decoder to stop decoding so this should be used with extreme caution.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.foldtext |
|:----------------|:--------|
| __Description__ | If set to "true", header fields containing just text such as the Subject and Content-Description header fields, and long parameter values<br>in structured headers such as Content-Type will be folded (broken into 76 character lines) when set and unfolded when read.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.setcontenttypefilename |
|:----------------|:--------|
| __Description__ | If set to "true", the setFileName method will also set the name parameter on the Content-Type header to the specified filename.<br>This supports interoperability with some old mail clients.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.setdefaulttextcharset |
|:----------------|:--------|
| __Description__ | When updating the headers of a message, a body part with a text content type but no charset parameter will have<br>a charset parameter added to it if this property is set to "true".<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.alternates |
|:----------------|:--------|
| __Description__ | A string containing other email addresses that the current user is known by. The MimeMessage reply method will eliminate any of<br>these addresses from the recipient list in the message it constructs, to avoid sending the reply back to the sender.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.replyallcc |
|:----------------|:--------|
| __Description__ | If set to "true", the MimeMessage reply method will put all recipients except the original sender in the Cc list of the newly constructed message.<br>Normally, recipients in the To header of the original message will also appear in the To list of the newly constructed message.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a> |
| __File__ | javamail.properties |

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
| __Key__ | com.openexchange.mail.filter.loginType |
|:----------------|:--------|
| __Description__ | Specifies which sieve server should be used. Two options are allowed here:<br>user : use the imap server setting stored for user in the database<br>global : use the sieve server given in com.openexchange.mail.filter.server for all users.<br> |
| __Default__ | user |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.filter.server |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.credentialSource |
|:----------------|:--------|
| __Description__ | Specifies which sieve credentials should be use. Four options are allowed here:<br>"session" : login name and password are used from the current session.<br>"session-full-login" : full login (incl. context part) name and password are used from the current session.<br>"imapLogin" : the login name is taken from the field imapLogin of the current<br>              user the password is taken from the current session.<br>"mail" : use the primary mail address of the user and the password from the session.<br> |
| __Default__ | session |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.server |
|:----------------|:--------|
| __Description__ | Specifies a default value for the sieve server.<br> |
| __Default__ | localhost |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.port |
|:----------------|:--------|
| __Description__ | Specifies the SIEVE port<br>-----------------------------------------------------------<br>NOTE: 2000 is the deprecated port number for SIEVE (now assigned to some Cisco SCCP protocol by the IANA)<br>      4190 is the new one used with most recent Linux and IMAP implementations.<br>Please check your system's default port defined at /etc/services.<br>-----------------------------------------------------------<br> |
| __Default__ | 4190 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.scriptName |
|:----------------|:--------|
| __Description__ | Specifies the name of the script, which will be generated.<br>Note that the mail filter bundle will leave the old script with the old<br>script name behind, and doesn't delete it.<br> |
| __Default__ | Open-Xchange |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.authenticationEncoding |
|:----------------|:--------|
| __Description__ | Define the charset encoding to use for authentication to sieve server.<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.nonRFCCompliantTLSRegex |
|:----------------|:--------|
| __Description__ | Define the regex which recognizes servers with incorrect sieve TLS implementation.<br> |
| __Default__ | ^Cyrus.\*v([0-1]\.[0-9].\*&#124;2\.[0-2].\*&#124;2\.3\.[0-9]&#124;2\.3\.[0-9][^0-9].\*)$ |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.tls |
|:----------------|:--------|
| __Description__ | Whether to use TLS if available.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.vacationDomains |
|:----------------|:--------|
| __Description__ | Specifies if a vacation messages should only be sent to specific domains.<br>If multiple domains are given, they should be separated by ","<br>e.g. com.openexchange.mail.filter.vacationDomains=testdomain.com,example.com<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.connectionTimeout |
|:----------------|:--------|
| __Description__ | Specifies when the connection should time out (value in milliseconds).<br> |
| __Default__ | 30000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.authTimeout |
|:----------------|:--------|
| __Description__ | Specifies when the connection should time out (value in milliseconds) when performing SASL authentication against Sieve end-point.<br> |
| __Default__ | 6000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.passwordSource |
|:----------------|:--------|
| __Description__ | Set the password source; meaning which source is taken to determine a user's password to login into mail filter system. <br>If 'session' is set, then user's individual system's password is taken. <br>If 'global' is set, then the value specified through property 'com.openexchange.mail.filter.masterPassword' is taken.<br>Currently known values: session and global<br> |
| __Default__ | session |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.filter.masterPassword |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.masterPassword |
|:----------------|:--------|
| __Description__ | The master password for mail/transport server. Only takes effect when property<br>"com.openexchange.mail.filter.passwordSource" is set to "global".<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.filter.passwordSource |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.useUTF7FolderEncoding |
|:----------------|:--------|
| __Description__ | This property defines if mailbox names shall be UTF-7 encoded as specified in RFC2060; section 5.1.3. "Mailbox International Naming Convention".<br>If set to "false" no UTF-7 encoding is performed.<br><br>Set to "true" for those Cyrus IMAP server versions that do NOT support "sieve_utf8fileinto" property (e.g. lower than v2.3.11)<br>Set to "true" for those Cyrus IMAP server versions that support "sieve_utf8fileinto" property having that property set to "0".<br>Thus moving mails with the 'fileinto' command will properly work for mailbox names that contain non-ascii characters.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.punycode |
|:----------------|:--------|
| __Description__ | Enable punycode encoding for the username used in authentication against the managesieve server.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.useSIEVEResponseCodes |
|:----------------|:--------|
| __Description__ | Interpret SIEVE Response Codes, see https://tools.ietf.org/html/rfc5804#section-1.3<br>In most cases, this option must be kept to false.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.redirectWhitelist |
|:----------------|:--------|
| __Description__ | Specifies a comma-separated list of domains (wild-card syntax supported) that are allowed for redirect rules.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.filter.preferredSaslMech |
|:----------------|:--------|
| __Description__ | Specifies the preferred SASL authentication mechanism.<br>An empty value falls-back to "PLAIN"<br><br>Known values: GSSAPI, XOAUTH2, OAUTHBEARER<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Filter.html">Mail Filter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SIEVE.html">SIEVE</a> |
| __File__ | mailfilter.properties |

---
| __Key__ | com.openexchange.mail.compose.share.enabled |
|:----------------|:--------|
| __Description__ | The main switch to enable/disable to send composed share messages<br>Note: In order to effectively enable composed share messages, the "infostore" and "share_links" capabilities need also to be available<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.name |
|:----------------|:--------|
| __Description__ | Specifies the naming for the feature to send composed share messages<br> |
| __Default__ | Drive Mail |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.threshold |
|:----------------|:--------|
| __Description__ | Specifies the threshold in bytes when the client is supposed to send a share compose message.<br>Setting this option to 0 (zero) disables "forced" switch to a share compose message.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.externalRecipientsLocale |
|:----------------|:--------|
| __Description__ | Defines the locale to use when sending a composed share message to an external recipient.<br>Expects a locale identifier compliant to RFC 2798 and 2068; such as "en_US".<br>Special value "user-defined" means to select the sending user's locale.<br> |
| __Default__ | user-defined |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.requiredExpiration |
|:----------------|:--------|
| __Description__ | Defines whether an expiration date is required to be set, which applied to<br>the folder/files that were shared via a share compose message.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.forceAutoDelete |
|:----------------|:--------|
| __Description__ | Defines whether shared folder/files get automatically cleansed if an expiration date is exceeded<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.preview.timeout |
|:----------------|:--------|
| __Description__ | Defines default timeout in milliseconds for preview image creation.<br> |
| __Default__ | 1000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.mail.compose.share.documentPreviewEnabled |
|:----------------|:--------|
| __Description__ | If set to true, preview images for documents are generated. Needs readerengine.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a> |
| __File__ | mail-compose.properties |

---
| __Key__ | com.openexchange.noreply.address |
|:----------------|:--------|
| __Description__ | Specifies the E-Mail address for the no-reply account that is used as "From" header for<br>system-initiated messages. Values must apply to the syntax defined in RFC 822, section 6.<br>Examples:<br>  - noreply@example.com<br>  - OX App Suite <noreply@example.com><br>  - "App Suite, OX" <noreply@example.com><br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/No-Reply.html">No-Reply</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | noreply.properties |

---
| __Key__ | com.openexchange.noreply.login |
|:----------------|:--------|
| __Description__ | Specifies the login for the no-reply account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/No-Reply.html">No-Reply</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | noreply.properties |

---
| __Key__ | com.openexchange.noreply.password |
|:----------------|:--------|
| __Description__ | Specifies the password for the no-reply account.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/No-Reply.html">No-Reply</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | noreply.properties |

---
| __Key__ | com.openexchange.noreply.server |
|:----------------|:--------|
| __Description__ | Specifies the SMTP server address for the no-reply account. The value must be an IP address<br>or a full qualified domain name.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/No-Reply.html">No-Reply</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | noreply.properties |

---
| __Key__ | com.openexchange.noreply.port |
|:----------------|:--------|
| __Description__ | Specifies the SMTP server port for the no-reply account.<br> |
| __Default__ | 25 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/No-Reply.html">No-Reply</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | noreply.properties |

---
| __Key__ | com.openexchange.noreply.secureMode |
|:----------------|:--------|
| __Description__ | Specifies the secure mode for the no-reply account;<br>supported values are either "SSL", "TLS" or "plain".<br> |
| __Default__ | plain |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/No-Reply.html">No-Reply</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | noreply.properties |

---
| __Key__ | com.openexchange.pop3.pop3Timeout |
|:----------------|:--------|
| __Description__ | Define the socket read timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.smtp.timeout<br>Default is 50000<br> |
| __Default__ | 50000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3ConnectionTimeout |
|:----------------|:--------|
| __Description__ | Define the socket connect timeout value in milliseconds. A value less<br>or equal to zero is infinite timeout. See also mail.smtp.connectiontimeout<br>Default is 20000<br> |
| __Default__ | 20000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3TemporaryDown |
|:----------------|:--------|
| __Description__ | Define the amount of time in milliseconds a POP3 server is treated as being temporary down.<br>A POP3 server is treated as being temporary down if a socket connect fails. Further requests to<br>the affected POP3 server are going to be denied for the specified amount of time.<br>A value less or equal to zero disables this setting.<br> |
| __Default__ | 10000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3AuthEnc |
|:----------------|:--------|
| __Description__ | Define the encoding for POP3 authentication<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.spamHandler |
|:----------------|:--------|
| __Description__ | Define the registration name of the appropriate spam handler to use<br>Note: This value gets overwritten by "com.openexchange.spamhandler.name" property<br> |
| __Default__ | DefaultSpamHandler |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.spamhandler.name |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3ConnectionIdleTime |
|:----------------|:--------|
| __Description__ | Define the amount of time in milliseconds an established POP3 connection is kept<br>open although being idle. Since some POP3 servers limit the time period in which<br>connections may be opened/closed, this property allows to keep the connection open<br>to avoid an error on a subsequent login.<br>This property overwrites default connection idle time specified through property<br>"com.openexchange.mail.mailAccessCacheIdleSeconds".<br> |
| __Default__ | 300000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.pop3BlockSize |
|:----------------|:--------|
| __Description__ | Specify the number of messages (positive integer!) which are allowed to be processed at once.<br>Default is 100.<br>Zero or negative value is defaulted to 100.<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.allowPing |
|:----------------|:--------|
| __Description__ | Whether ping operation is allowed for POP3 account<br>Many POP3 account limit number of allowed login attempts in a certain time interval<br>Default is false<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.logDeniedPing |
|:----------------|:--------|
| __Description__ | Whether denied ping operation shall be indicated as a warning to client<br>Only effective if "com.openexchange.pop3.allowPing" is set to false.<br>Default is true<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.ssl.protocols |
|:----------------|:--------|
| __Description__ | Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br>Default is empty<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.pop3.ssl.ciphersuites |
|:----------------|:--------|
| __Description__ | Specifies the SSL cipher suites that will be enabled for SSL connections. The property value is a whitespace separated list of tokens<br><br>Check "http://<ox-grizzly-hostname>:<ox-grizzly-port>/stats/diagnostic?param=ciphersuites" to check available cipher suites.<br><br>Default value is empty (fall-back to current JVM's default SSL cipher suite)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a> |
| __File__ | pop3.properties |

---
| __Key__ | com.openexchange.capability.archive_emails |
|:----------------|:--------|
| __Description__ | Enables/disables the archive functionalities that is to move on demand older mails from any selected folder to a special archive folder.<br>If disabled the server will reject to perform archive requests.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | permissions.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapSort |
|:----------------|:--------|
| __Description__ | Define where to sort emails: Value "imap" to let the  IMAP Server sort (faster but less reliable).<br>Leave blank or fill in value "application" to sort within application (slower but good quality).<br>The sorting is done on IMAP server if a mailbox' size exceeds the mailFetchLimit as defined in<br>mail.properties.<br>NOTE:<br>This value is going to be set to "application" if IMAP server capabilities do not contain string "SORT".<br>Moreover, please also refer to property "com.openexchange.imap.fallbackOnFailedSORT" to specify how to react to a possible "NO" response.<br>Default is "imap"<br> |
| __Default__ | imap |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapSearch |
|:----------------|:--------|
| __Description__ | Define where to search for emails:<br>- Use value "imap" to let the IMAP Server search. The search is done on IMAP server if a mailbox' size exceeds the mailFetchLimit as defined in mail.properties.<br>- Use value "force-imap" to let the IMAP Server search in every case.<br>- Leave blank or fill in value "application" to search within application.<br>Default is "force-imap"<br> |
| __Default__ | force-imap |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.umlautFilterThreshold |
|:----------------|:--------|
| __Description__ | Specify the threshold for number of search results returned by IMAP server for which manual umlauts-filtering<br>will be applied. If less than or equal to zero, no manual filtering will be applied.<br>Default value is 50.<br> |
| __Default__ | 50 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapFastFetch |
|:----------------|:--------|
| __Description__ | This property determines whether a fast fetch is performed on large mail<br>folders or not. Although the fetch is fast on IMAP side, a lot of data is<br>transfered during reading response which cause a temporary memory peak.<br>If disabled only the necessary fields are used as command arguments,<br>which is slower but needs less memory.<br>NOTE: See property "imapMessageFetchLimit" to know which mail folders are<br>treated as large mail folders<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapSupportsACL |
|:----------------|:--------|
| __Description__ | Define if IMAP server supports ACLs. Possible values: true/false/auto<br>NOTE: Value "auto" means to use server-defined ACL support as indicated<br>through response to IMAP command "CAPABILITY"<br> |
| __Default__ | auto |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapTimeout |
|:----------------|:--------|
| __Description__ | Define the socket read timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.imap.timeout<br> |
| __Default__ | 50000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapConnectionTimeout |
|:----------------|:--------|
| __Description__ | Define the socket connect timeout value in milliseconds. A value less than<br>or equal to zero is infinite timeout. See also mail.imap.connectiontimeout<br> |
| __Default__ | 20000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapTemporaryDown |
|:----------------|:--------|
| __Description__ | Define the amount of time in milliseconds an IMAP server is treated as being temporary down.<br>An IMAP server is treated as being temporary down if a socket connect fails. Further requests to<br>the affected IMAP server are going to be denied for the specified amount of time.<br>A value less or equal to zero disables this setting.<br> |
| __Default__ | 10000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].imapAuthEnc |
|:----------------|:--------|
| __Description__ | Define the encoding for IMAP authentication<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.User2ACLImpl |
|:----------------|:--------|
| __Description__ | Name of the class that implements User2ACL, their alias or "auto" to use auto-detection.<br>Currently known aliases: Cyrus, Courier, Dovecot, and Sun (Sun Java(tm) System Messaging Server)<br> |
| __Default__ | auto |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].blockSize |
|:----------------|:--------|
| __Description__ | IMAP operations which shall be applied to a number of messages which exceeds the block size<br>are performed in blocks. Example: A folder containing thousands of messages shall be cleared.<br>To avoid the risk of an IMAP timeout when trying to delete all messages at once, the messages<br>are deleted in block size portions.<br>A block size equal to or less than zero means no block size.<br> |
| __Default__ | 1000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.spamHandler |
|:----------------|:--------|
| __Description__ | Define the registration name of the appropriate spam handler to use<br>Note: This value gets overwritten by "com.openexchange.spamhandler.name" property<br> |
| __Default__ | DefaultSpamHandler |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.spamhandler.name |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].propagateClientIPAddress |
|:----------------|:--------|
| __Description__ | Whether client's IP address should be propagated by a NOOP command; e.g. "A01 NOOP <CLIENT_IP>"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].propagateHostNames |
|:----------------|:--------|
| __Description__ | Configure a comma-separated list of external IMAP server's host names which should receive client's IP address by a NOOP command, too<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].maxNumConnections |
|:----------------|:--------|
| __Description__ | The max. number of connection allowed being established for a user to an IMAP server. Less than or equal to zero means infinite.<br>Please also consider "com.openexchange.imap.storeContainerType".<br><br>Note: This setting overrides possibles restrictions specified through property "com.openexchange.imap.maxNumExternalConnections" if<br>this property's value is less than the other one.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.imap.storeContainerType |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.storeContainerType |
|:----------------|:--------|
| __Description__ | An IMAP connection cache acts a temporary keep-alive cache for already connected resources to an IMAP server's account.<br>Meaning it keeps a resource open/connected for a short amount of time (see "com.openexchange.mail.mailAccessCacheIdleSeconds")<br>and performs a "close elapsed ones" run periodically (see "com.openexchange.mail.mailAccessCacheShrinkerSeconds").<br><br>In addition to that behavior there are two modes of operation - bounded and unbounded.<br><br>For an unbounded cache, set this property to "unbounded".<br>Thus a user is allowed to establish as many connections to his IMAP account as demanded by his active clients (Web UI, EAS, Outlook OXtender, etc.).<br><br>A bounded cache allows only as many concurrently opened resources as specified through "com.openexchange.imap.maxNumConnections" property.<br>Taking the wording "resource" was chosen by intention, since two types of resource abstractions exist:<br>IMAP store and IMAP protocol (an authenticated login's socket connection).<br><br>The default setting "boundary-aware" considers an "IMAP store" as limited resources to an IMAP server.<br>The vague thing about IMAP store is that it maintains a connection pool internally by itself.<br>Thus it is possible that there are actually more active socket connections open than specified,<br>because an IMAP store is allowed to open further connections when needed;<br>e.g. when accessing another IMAP folder while INBOX has been opened, too.<br>Practical experience showed that there will be at max.: "com.openexchange.imap.maxNumConnections" + 1<br><br>The setting "non-caching" does an exact mapping of resource to an established/authenticated socket connection to the IMAP account.<br>It is named "non-caching" as it does no caching on its own, but delegates it to a custom queuing 'com.sun.mail.imap.IMAPStore' class.<br>Thus an exact limitation of connected socket connections ('com.sun.mail.imap.protocol.IMAPProtocol' instances) is achieved.<br>Specifying a quite small limitation - let's say "1" - arises the possibility that JavaMail gets dead-locked by itself.<br>E.g. an IMAP store attempts to create a second connection. That attempt may get stalled as it waits for itself to free the already<br>acquired connection which never happens.<br>So, please use this exact mapping only if you specify a reasonable limitation.<br> |
| __Default__ | boundary-aware |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.maxNumExternalConnections |
|:----------------|:--------|
| __Description__ | Configure the max. number of concurrent connections which are allowed being established to a subscribed/external IMAP account.<br>Notation is a comma-separated list of: <host> + ':' + <max-count>; e.g.:<br>    com.openexchange.imap.maxNumExternalConnections=imap.host1.com:4, imap.host2.com:6<br>For convenience a max-count can be specified which applies to all subscribed/external IMAP accounts; e.g.:<br>    com.openexchange.imap.maxNumExternalConnections=4<br>Zero or less is interpreted as unlimited.<br>If not set, unlimited concurrent connections are allowed.<br> |
| __Default__ | imap.gmail.com:2,imap.googlemail.com:2 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].enableTls |
|:----------------|:--------|
| __Description__ | Enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection.<br>Note: This property is statically used in IMAP authentication bundle<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.invalidMailboxNameCharacters |
|:----------------|:--------|
| __Description__ | Specifies a space-separated list of characters that are not allowed to be contained in a mailbox name;<br>e.g. >>com.openexchange.imap.invalidMailboxNameCharacters="; / . &#124; \\"<<<br>Default is empty.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].allowFolderCaches |
|:----------------|:--------|
| __Description__ | Enables/disables caching of IMAP folders.<br>Default is true.<br>Note: Only disable IMAP folder cache if you certainly know what you are doing.<br>Disabling that cache may result in noticeable performance decrease.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Caching.html">Caching</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].ssl.protocols |
|:----------------|:--------|
| __Description__ | Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br>Default is empty<br><br>Note: This property is statically used in IMAP authentication bundle<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].ssl.ciphersuites |
|:----------------|:--------|
| __Description__ | Specifies the SSL cipher suites that will be enabled for SSL connections. The property value is a whitespace separated list of tokens.<br>Check "http://<ox-grizzly-hostname>:<ox-grizzly-port>/stats/diagnostic?param=ciphersuites" to check available cipher suites.<br>Default value is empty (fall-back to current JVM's default SSL cipher suite)<br><br>Note: This property is statically used in IMAP authentication bundle<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.maxMailboxNameLength |
|:----------------|:--------|
| __Description__ | The max. length of a mailbox name<br> |
| __Default__ | 60 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.invalidMailboxNameCharacters |
|:----------------|:--------|
| __Description__ | Specifies a space-separated list of characters that are not allowed to be contained in a mailbox name;<br>e.g. >>com.openexchange.imap.invalidMailboxNameCharacters="; / . &#124; \\"<<<br>Default is empty.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.namespacePerUser |
|:----------------|:--------|
| __Description__ | This option controls whether there it is expected to have a dedicated NAMESPACE per user or not.<br>In case of "true" each mailbox account on associated IMAP server is allowed to have its own NAMESPACE set; might be "" (root) or "INBOX.".<br>Otherwise for "false" every mailbox is assumed to have the same NAMESPACE set.<br><br>This influences the way Open-Xchange Server detects & caches NAMESPACE information; either on a per user basis (more IMAP traffic) or<br>globally (only requested once).<br><br>Do not touch unless you certainly know IMAP sever's behavior.<br><br>Default is "true"<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.rootSubfoldersAllowed |
|:----------------|:--------|
| __Description__ | If either "true" or "false" set, it enforces whether to assume root sub-folder capability for primary account.<br>If not set, root sub-folder capability is probed through creating a temporary folder.<br><br>Default is empty (probe through temp. folder)<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.allowSORTDISPLAY |
|:----------------|:--------|
| __Description__ | Specifies if "SORT=DISPLAY" IMAP extension is supposed to be considered when returning a mail listing sorted by From/To<br><br>Default is "false"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.fallbackOnFailedSORT |
|:----------------|:--------|
| __Description__ | Specifies if a fall-back to in-application sort is supposed to be performed in case the IMAP server quits with a "NO" response for the<br>issued SORT command.<br>Note: Doing in-application sort contains the danger of utilizing too much memory (especially for big mailboxes).<br><br>Default is "false"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap[.primary].auditLog.enabled |
|:----------------|:--------|
| __Description__ | Enables the audit log for issued IMAP commands via 'com.sun.mail.imap.AuditLog' class.<br><br>Accepts the "primary." suffix to only enable for primary account;<br>e.g. "com.openexchange.imap.primary.auditLog.enabled=true"<br><br>Default is "false"<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.imap.initWithSpecialUse |
|:----------------|:--------|
| __Description__ | Specifies whether the primary mail account should be initialized with special-use folders from the imap server in case no standard folder names are configured.<br><br>Default is 'true'<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a> |
| __File__ | imap.properties |

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
| __Key__ | com.openexchange.dovecot.doveadm.enabled |
|:----------------|:--------|
| __Description__ | Specifies whether the connector for the Dovecot DoveAdm REST interface will be enabled or not.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/REST.html">REST</a> |
| __File__ | doveadm.properties |

---
| __Key__ | com.openexchange.dovecot.doveadm.endpoints |
|:----------------|:--------|
| __Description__ | Specifies the URIs to the Dovecot DoveAdm REST interface end-points. <br>e.g. "http://dovecot1.host.invalid:8081, http://dovecot2.host.invalid:8081, http://dovecot3.host.invalid:8081"<br><br>Moreover connection-related attributes are allowed to be specified to influence HTTP connection and pooling behavior<br>com.openexchange.dovecot.doveadm.endpoints.totalConnections        The number of total connections held in HTTP connection pool<br>com.openexchange.dovecot.doveadm.endpoints.maxConnectionsPerRoute  The number of connections per route held in HTTP connection pool; or less than/equal to 0 (zero) for auto-determining<br>com.openexchange.dovecot.doveadm.endpoints.readTimeout             The read time-out in milliseconds (default is 10sec)<br>com.openexchange.dovecot.doveadm.endpoints.connectTimeout          The connect time-out in milliseconds (default is 3sec)<br>com.openexchange.dovecot.doveadm.endpoints.checkInterval           The time interval in milliseconds when to check if a previously black-listed end-point is re-available again (default is 60sec)<br><br>"Full example :"<br>com.openexchange.dovecot.doveadm.endpoints=http://dovecot1.host.invalid:8081, http://dovecot2.host.invalid:8081<br>com.openexchange.dovecot.doveadm.endpoints.totalConnections=100<br>com.openexchange.dovecot.doveadm.endpoints.maxConnectionsPerRoute=0 (max. connections per route is then determined automatically by specified end-points)<br>com.openexchange.dovecot.doveadm.endpoints.readTimeout=10000<br>com.openexchange.dovecot.doveadm.endpoints.connectTimeout=3000<br>com.openexchange.dovecot.doveadm.endpoints.checkInterval=60000<br><br>The values can be configured within a dedicated .properties file; e.g. 'doveadm.properties'.<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/REST.html">REST</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | doveadm.properties |

---
| __Key__ | com.openexchange.dovecot.doveadm.apiSecret |
|:----------------|:--------|
| __Description__ | Specifies the API secret to communicate with the Dovecot DoveAdm REST interface.<br> |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/REST.html">REST</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | doveadm.properties |

---
