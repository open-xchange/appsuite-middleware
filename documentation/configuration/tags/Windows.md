---
title: Windows
---

This page shows all properties with the tag: Windows

| __Key__ | com.openexchange.drive.version.[client].softMinimum |
|:----------------|:--------|
| __Description__ | This property allows the configuration of "soft" limit restrictions for<br>the supported clients. This limit has informational<br>character only, i.e. the client is just informed about an available update<br>when identifying with a lower version number. <br>The property is disabled by default to always fall back to the<br>recommended setting, but can be overridden if needed.<br>[client] must be replaced with one of the following:<br>  \* windows<br>  \* macos<br>  \* ios<br>  \* android<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Windows.html">Windows</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.version.[client].hardMinimum |
|:----------------|:--------|
| __Description__ | This property allows the configuration of "hard" limit restrictions for<br>the supported clients. This limit will restrict further synchronization <br>of clients that identify themselves with a lower version number.<br>The property is disabled by default to always fall back to the<br>recommended setting, but can be overridden if needed.<br>[client] must be replaced with one of the following:<br>  \* windows<br>  \* macos<br>  \* ios<br>  \* android<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Windows.html">Windows</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Apple.html">Apple</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.client.onboarding.windows.displayName |
|:----------------|:--------|
| __Description__ | The display name of the Windows platform.<br> |
| __Default__ | Windows |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Windows.html">Windows</a> |
| __File__ | client-onboarding.properties |

---
| __Key__ | com.openexchange.client.onboarding.windows.description |
|:----------------|:--------|
| __Description__ | The description of the Windows platform.<br> |
| __Default__ | The Windows platform |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Windows.html">Windows</a> |
| __File__ | client-onboarding.properties |

---
| __Key__ | com.openexchange.client.onboarding.windows.desktop.scenarios |
|:----------------|:--------|
| __Description__ | The on-boarding properties for Windows Desktop device.<br> |
| __Default__ | drivewindowsclientinstall, emclientinstall, mailmanual, oxupdaterinstall |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Windows.html">Windows</a> |
| __File__ | client-onboarding.properties |

---
