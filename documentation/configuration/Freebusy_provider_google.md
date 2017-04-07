---
title: Freebusy provider google
---

| __Key__ | com.openexchange.freebusy.provider.google.apiEndpoint |
|:----------------|:--------|
| __Description__ | Configures the URI of the Google Calendar API endpoint and should normally<br>not be changed. Required.<br> |
| __Default__ | https://www.googleapis.com/calendar/v3 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Freebusy.html">Freebusy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a> |
| __File__ | freebusy_provider_google.properties |

---
| __Key__ | com.openexchange.freebusy.provider.google.apiKey |
|:----------------|:--------|
| __Description__ | Sets the Google API key to be used for requests to the Google Calendar API.<br>Required.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Freebusy.html">Freebusy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a> |
| __File__ | freebusy_provider_google.properties |

---
| __Key__ | com.openexchange.freebusy.provider.google.emailSuffixes |
|:----------------|:--------|
| __Description__ | Allows the definition of a comma-separated list of e-mail-address suffixes <br>(e.g. domain parts like "@googlemail.com") that are used to pre-filter the <br>requested participants before passing them to Google. Optional, but strongly <br>recommended to reduce the amount of transferred data.<br> |
| __Default__ | @gmail.com,@googlemail.com |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Freebusy.html">Freebusy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a> |
| __File__ | freebusy_provider_google.properties |

---
| __Key__ | com.openexchange.freebusy.provider.google.validEmailsOnly |
|:----------------|:--------|
| __Description__ | Configures whether only valid e-mail addresses are used in the free/busy <br>lookup or not.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Freebusy.html">Freebusy</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a> |
| __File__ | freebusy_provider_google.properties |

---
