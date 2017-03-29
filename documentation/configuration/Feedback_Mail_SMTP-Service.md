---
title: Feedback Mail SMTP-Service
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
| __Key__ | com.openexchange.userfeedback.mail.exportPrefix |
|:----------------|:--------|
| __Description__ | Prefix for mail attachment<br> |
| __Default__ | OX_App_Suite_Feedback_Report |
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
