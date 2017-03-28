---
title: spamhandler
---

This page shows all properties with the tag: spamhandler

| __Key__ | com.openexchange.spamhandler.cloudmark.targetSpamEmailAddress |
|:----------------|:--------|
| __Description__ | Defines the eMail address to which the selected eMails will be bounced. If no<br>address is specified the bounce will be skipped, but moving the selected mails<br>to the target folder will still be processed (if configured)<br> |
| __Default__ | empty |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/spamhandler.html">spamhandler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/cloudmark.html">cloudmark</a> |
| __File__ | spamhandler_cloudmark.properties |

---
| __Key__ | com.openexchange.spamhandler.cloudmark.targetHamEmailAddress |
|:----------------|:--------|
| __Description__ | Defines the eMail address to which the selected eMails will be bounced, if they<br>are marked as Ham. If no address is specified the bounce will be skipped,<br>but moving the selected mails back to the Inbox will still be processed (if configured).<br>"Privacy note:" This will send private mails of users to that address when marked as Ham<br> |
| __Default__ | empty |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/spamhandler.html">spamhandler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/cloudmark.html">cloudmark</a> |
| __File__ | spamhandler_cloudmark.properties |

---
| __Key__ | com.openexchange.spamhandler.cloudmark.targetSpamFolder |
|:----------------|:--------|
| __Description__ | Defines to which folder the selected mails should be moved to after they have<br>been bounced to the target eMail address. If no option is configured the<br>selected mails will be moved to the users trash folder. "Possible options are:"<br><br>0 = Do not move the message at all<br>1 = User's trash folder (Default)<br>2 = User's SPAM folder<br>3 = Subscribed confirmed-spam folder (experimental)<br> |
| __Default__ | 2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/spamhandler.html">spamhandler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/cloudmark.html">cloudmark</a> |
| __File__ | spamhandler_cloudmark.properties |

---
| __Key__ | com.openexchange.spamhandler.cloudmark.wrapMessage |
|:----------------|:--------|
| __Description__ | Defines if the spam/ham message is passed as a nested message to the target address<br><br>----------------------------------== /!\ ==----------------------------------------<br>"Note:" This option needs to be clarified with your running Cloudmark service that<br>      passing the spam/ham message as a nested message is accepted and properly<br>      handled!<br>-----------------------------------------------------------------------------------<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/spamhandler.html">spamhandler</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/cloudmark.html">cloudmark</a> |
| __File__ | spamhandler_cloudmark.properties |

---
