---
title: Mail compose
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
