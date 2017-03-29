---
title: Drive Client Windows
---

| __Key__ | com.openexchange.drive.updater.path |
|:----------------|:--------|
| __Description__ | The path of the "files"-directory where branded updater's and the branding property file's will be placed.<br>The file's must be placed within an own folder per branding.<br>For example the folder tree could look like this":"<br>[path]<br>    -generic<br>        -generic.branding<br>        -OX Drive_vX.Y.Z.exe<br>        -OX Drive_vX.Y.Z.msi<br>    -yourOwnBranding<br>        -[...]<br> |
| __Default__ | /opt/open-xchange/files/drive-client/windows |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive_Client_Windows.html">Drive Client Windows</a> |
| __File__ | drive-client-windows.properties |

---
| __Key__ | com.openexchange.drive.updater.tmpl |
|:----------------|:--------|
| __Description__ | The filename of the drive updater template<br> |
| __Default__ | oxdrive_update.tmpl |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive_Client_Windows.html">Drive Client Windows</a> |
| __File__ | drive-client-windows.properties |

---
| __Key__ | com.openexchange.drive.update.branding |
|:----------------|:--------|
| __Description__ | The name of the system wide drive branding identifier<br>This name must be equal to the name of one of the subfolder's under com.openexchange.drive.updater.path<br>This property can be overwritten through the config-cascade mechanism. <br>Therefore you can configure different branding's for different context-sets or even single users.<br> |
| __Default__ | generic |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive_Client_Windows.html">Drive Client Windows</a> |
| __File__ | drive-client-windows.properties |

---
| __Key__ | com.openexchange.drive.windows.binaryRegex.exe |
|:----------------|:--------|
| __Description__ | The regex to match the exe artifacts of the drive clients build job.<br> |
| __Default__ | .\*_v([0-9.]+).exe |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive_Client_Windows.html">Drive Client Windows</a> |
| __File__ | drive-client-windows.properties |

---
| __Key__ | com.openexchange.drive.windows.binaryRegex.msi |
|:----------------|:--------|
| __Description__ | The regex to match the msi artifacts of the drive clients build job.<br> |
| __Default__ | .\*_v([0-9.]+).msi |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Drive_Client_Windows.html">Drive Client Windows</a> |
| __File__ | drive-client-windows.properties |

---
