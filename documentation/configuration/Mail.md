# Mail

| __Key__ | com.openexchange.mail.useStaticDefaultFolders |
|:----------------|:--------|
| __Description__ | Defines whether standard folder names should be initialized with the default values or not. <br>The default values can be configured within the Adminuser.properties file.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.imap.setSpecialUseFlags |
|:----------------|:--------|
| __Description__ | Defines whether the ox middleware is allowed to set special use flags.<br>If set to 'false' the ox middleware will never set any special use flags on folders on the imap server.<br>If set to 'true' the ox middleware will only set special use flags if no special use flag of that type exist yet.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __File__ | imap.properties |

---
| __Key__ | com.openexchange.mail.preferSentDate |
|:----------------|:--------|
| __Description__ | Specifies what to consider as the date of a mail; either the internal received date or mail's sent date (as given by "Date" header).<br>This property is considered in case a client passes special "date" (661) column to "columns" parameter and/or "sort" parameter.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.maxDriveAttachments |
|:----------------|:--------|
| __Description__ | Specifies the max. number of Drive documents that are allowed to be sent via E-Mail<br> |
| __Default__ | 20 |
| __Version__ | 7.6.2 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for primary account's mail access. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.mail.transport.authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type which should be used for primary account's mail transport. Known values: 'login', 'xoauth2', and "oauthbearer"<br> |
| __Default__ | login |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
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
| __File__ | mail.properties |

---
| __Key__ | com.openexchange.imap.greeting.host.regex |
|:----------------|:--------|
| __Description__ | Specifies the regular expression to use to extract the host name/IP address information out of the greeting string advertised by primary<br>IMAP server. Only applicable for primary IMAP server! Default is empty.<br><br>The regular expression is supposed to be specified in Java notation: http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html<br><br>Moreover, either the complete regex is considered or in case a capturing group is present that group will be preferred.<br>I.e. "Dovecot at ([0-9a-zA-Z._-]*) is ready", then the capturing group is supposed to extract the host name/IP addres information<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | imap.properties |

---
