---
title: Security
---

This page shows all properties with the tag: Security

| __Key__ | IMAP_USE_SECURE |
|:----------------|:--------|
| __Description__ | Set to true if connecting via imaps://<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP_Auth.html">IMAP Auth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Authentication.html">Authentication</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
| __File__ | imapauth.properties |

---
| __Key__ | com.openexchange.client.onboarding.mail.imap.secure |
|:----------------|:--------|
| __Description__ | Specifies whether a secure connection is supposed to established to access the IMAP server.<br>If not set, falls-back to internal settings for accessing the primary account.<br><br>Possible values: true&#124;false<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
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
| __Key__ | com.openexchange.share.cryptKey |
|:----------------|:--------|
| __Description__ | Defines a key that is used to encrypt the password/pin of anonymously <br>accessible shares in the database.  <br>Defaults to "erE2e8OhAo71", and should be changed before the creation of the<br>first share on the system.<br> |
| __Default__ | erE2e8OhAo71 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
| __File__ | share.properties |

---
