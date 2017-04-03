---
title: Android
---

This page shows all properties with the tag: Android

| __Key__ | com.openexchange.drive.events.gcm.enabled |
|:----------------|:--------|
| __Description__ | Enables or disables push event notifications to clients using the Google<br>Cloud Messaging (GCM) service. This requires a valid configuration for the<br>GCM API key, see options below.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | drive.properties |

---
| __Key__ | com.openexchange.drive.events.gcm.key |
|:----------------|:--------|
| __Description__ | Specifies the API key of the server application. Required if<br>"com.openexchange.drive.events.gcm.enabled" is "true" and the package<br>containing the restricted drive components is not installed.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.drive.events.gcm.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Push.html">Push</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | drive.properties |

---
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
| __Key__ | com.openexchange.client.onboarding.android.displayName |
|:----------------|:--------|
| __Description__ | The display name of the Android/Google platform.<br> |
| __Default__ | Android |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | client-onboarding.properties |

---
| __Key__ | com.openexchange.client.onboarding.android.description |
|:----------------|:--------|
| __Description__ | The description of the Android/Google platform.<br> |
| __Default__ | The Android platform |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | client-onboarding.properties |

---
| __Key__ | com.openexchange.client.onboarding.android.tablet.scenarios |
|:----------------|:--------|
| __Description__ | The on-boarding properties for Android tablet device.<br> |
| __Default__ | mailappinstall, driveappinstall, mailmanual, syncappinstall |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | client-onboarding.properties |

---
| __Key__ | com.openexchange.client.onboarding.android.phone.scenarios |
|:----------------|:--------|
| __Description__ | The on-boarding properties for Android phone device.<br> |
| __Default__ | mailappinstall, driveappinstall, mailmanual, syncappinstall |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | client-onboarding.properties |

---
| __Key__ | com.openexchange.client.onboarding.driveapp.store.google.playstore |
|:----------------|:--------|
| __Description__ | Specifies the URL to Google Play Store for the Drive App.<br> |
| __Default__ | https://play.google.com/store/apps/details?id=com.openexchange.drive.vanilla |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive.html">Drive</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | client-onboarding-driveapp.properties |

---
| __Key__ | com.openexchange.client.onboarding.mailapp.store.google.playstore |
|:----------------|:--------|
| __Description__ | Specifies the URL to Google Play Store for the Mail App.<br> |
| __Default__ | https://play.google.com/store/apps/details?id=com.openexchange.mobile.mailapp.enterprise |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | client-onboarding-mailapp.properties |

---
| __Key__ | com.openexchange.client.onboarding.syncapp.store.google.playstore |
|:----------------|:--------|
| __Description__ | Specifies the URL to Google Play Store for the Sync App.<br> |
| __Default__ | https://itunes.apple.com/us/app/ox-mail/id1008644994 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Onboarding.html">Onboarding</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Sync_App.html">Sync App</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Android.html">Android</a> |
| __File__ | client-onboarding-syncapp.properties |

---
