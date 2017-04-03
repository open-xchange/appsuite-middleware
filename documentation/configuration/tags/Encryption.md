---
title: Encryption
---

This page shows all properties with the tag: Encryption

| __Key__ | com.openexchange.share.cryptKey |
|:----------------|:--------|
| __Description__ | Defines a key that is used to encrypt the password/pin of anonymously <br>accessible shares in the database.  <br>Defaults to "erE2e8OhAo71", and should be changed before the creation of the<br>first share on the system.<br> |
| __Default__ | erE2e8OhAo71 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.userfeedback.pgp.signKeyFile |
|:----------------|:--------|
| __Description__ | Path to PGP secret key used to sign mails<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Feedback.html">Feedback</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.userfeedback.pgp.signKeyPassword |
|:----------------|:--------|
| __Description__ | Password for PGP secret key used to sign mails<br> |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Feedback.html">Feedback</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | userfeedbackmail.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption |
|:----------------|:--------|
| __Description__ | Enables or disables symmetric encryption. When enabled, the entire<br>communication between the hazelcast members is encrypted at socket level.<br>Ensure that all symmetric encryption settings are equal on all<br>participating nodes in the cluster. More advanced options (including<br>asymmetric encryption and SSL) may still be configured via the<br>"hazelcast.xml" file, see instructions on top of this file.<br><br> /!\ ---==== Additional note ====--- /!\<br>If symmetric encryption is enabled, it might have impact on Hazelcast<br>cluster stability. Hazelcast nodes start loosing cluster connectivity under<br>high load scenarios<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption.algorithm |
|:----------------|:--------|
| __Description__ | Configures the name of the symmetric encryption algorithm to use, such as<br>"DES/ECB/PKCS5Padding", "PBEWithMD5AndDES", "Blowfish" or "DESede". The<br>available cipher algorithms may vary based on the underlying JCE.<br> |
| __Default__ | PBEWithMD5AndDES |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption.salt |
|:----------------|:--------|
| __Description__ | Specifies the salt value to use when generating the secret key for symmetric<br>encryption.<br> |
| __Default__ | 2mw67LqNDEb3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption.password |
|:----------------|:--------|
| __Description__ | Specifies the pass phrase to use when generating the secret key for<br>symmetric encryption.<br> |
| __Default__ | 2mw67LqNDEb3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
| __Key__ | com.openexchange.hazelcast.network.symmetricEncryption.iterationCount |
|:----------------|:--------|
| __Description__ | Configures the iteration count to use when generating the secret key for<br>symmetric encryption. <br> |
| __Default__ | 19 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Hazelcast.html">Hazelcast</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a> |
| __File__ | hazelcast.properties |

---
