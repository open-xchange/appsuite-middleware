---
title: Spam Handler
---

This page shows all properties with the tag: Spam Handler

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
| __Key__ | com.openexchange.spamhandler.cloudmark.targetSpamEmailAddress |
|:----------------|:--------|
| __Description__ | Defines the eMail address to which the selected eMails will be bounced. If no<br>address is specified the bounce will be skipped, but moving the selected mails<br>to the target folder will still be processed (if configured)<br> |
| __Default__ | empty |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cloudmark.html">Cloudmark</a> |
| __File__ | spamhandler_cloudmark.properties |

---
| __Key__ | com.openexchange.spamhandler.cloudmark.targetHamEmailAddress |
|:----------------|:--------|
| __Description__ | Defines the eMail address to which the selected eMails will be bounced, if they<br>are marked as Ham. If no address is specified the bounce will be skipped,<br>but moving the selected mails back to the Inbox will still be processed (if configured).<br>"Privacy note:" This will send private mails of users to that address when marked as Ham<br> |
| __Default__ | empty |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cloudmark.html">Cloudmark</a> |
| __File__ | spamhandler_cloudmark.properties |

---
| __Key__ | com.openexchange.spamhandler.cloudmark.targetSpamFolder |
|:----------------|:--------|
| __Description__ | Defines to which folder the selected mails should be moved to after they have<br>been bounced to the target eMail address. If no option is configured the<br>selected mails will be moved to the users trash folder. "Possible options are:"<br><br>0 = Do not move the message at all<br>1 = User's trash folder (Default)<br>2 = User's SPAM folder<br>3 = Subscribed confirmed-spam folder (experimental)<br> |
| __Default__ | 2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cloudmark.html">Cloudmark</a> |
| __File__ | spamhandler_cloudmark.properties |

---
| __Key__ | com.openexchange.spamhandler.cloudmark.wrapMessage |
|:----------------|:--------|
| __Description__ | Defines if the spam/ham message is passed as a nested message to the target address<br><br>----------------------------------== /!\ ==----------------------------------------<br>"Note:" This option needs to be clarified with your running Cloudmark service that<br>      passing the spam/ham message as a nested message is accepted and properly<br>      handled!<br>-----------------------------------------------------------------------------------<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spam_Handler.html">Spam Handler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cloudmark.html">Cloudmark</a> |
| __File__ | spamhandler_cloudmark.properties |

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
