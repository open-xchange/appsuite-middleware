---
title: Spamexperts
---

This page shows all properties with the tag: Spamexperts

| __Key__ | com.openexchange.custom.spamexperts.panel_servlet |
|:----------------|:--------|
| __Description__ | The next property defines the mount point of the panel servlet<br> |
| __Default__ | /ajax/spamexperts/panel |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.panel.api_interface_url |
|:----------------|:--------|
| __Description__ | URL of spamexperts Interface to generate new sessions<br> |
| __Default__ | http://demo1.spambrand.com/api/authticket/create/username/ |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.panel.admin_user |
|:----------------|:--------|
| __Description__ | This property defines the username which should be used as basic auth<br> |
| __Default__ | admin |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.panel.admin_password |
|:----------------|:--------|
| __Description__ | This property defines the password which should be used as basic auth<br> |
| __Default__ | demo |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.panel.api_auth_attribute |
|:----------------|:--------|
| __Description__ | Which user attribute should be used for authentication against panel API<br><br>"Possible values:"<br>- imaplogin -> Users IMAP login<br>- mail -> Users mail address<br>- login -> String which user entered at login mask<br>- username -> Users "username" attribute.<br> |
| __Default__ | mail |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.panel.web_ui_url |
|:----------------|:--------|
| __Description__ | Defines the URL where the panel is available<br> |
| __Default__ | http://demo1.spambrand.com/?authticket= |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.imapurl |
|:----------------|:--------|
| __Description__ | the imap url to the spamexperts imap server<br><br>"Example1:" imap://myserver.example.com<br>if the imap server offers STARTTLS, communication will be encrypted<br><br>"Example2:" imaps:myserver.example.com:993<br>connect to port 993 using a secure connection directly<br> |
| __Default__ | empty |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.imapuser |
|:----------------|:--------|
| __Description__ | username to authenticate against the imap server<br> |
| __Default__ | empty |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.imappassword |
|:----------------|:--------|
| __Description__ | password to authenticate against the imap server<br> |
| __Default__ | empty |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.trainspamfolder |
|:----------------|:--------|
| __Description__ | Foldername of folder to place spam mails in order to train the system<br> |
| __Default__ | Spam |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
| __Key__ | com.openexchange.custom.spamexperts.trainhamfolder |
|:----------------|:--------|
| __Description__ | Foldername of folder to place ham mails in order to train the system<br> |
| __Default__ | Not Spam |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Spamexperts.html">Spamexperts</a> |
| __File__ | spamexperts.properties |

---
