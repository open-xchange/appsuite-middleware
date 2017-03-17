---
title: Password Change Script
---

The package open-xchange-passwordchange-script allows you to run a command to
change a password in an external subsystem like e.g. LDAP


| __Key__ | com.openexchange.passwordchange.script.shellscript |
|:----------------|:--------|
| __Description__ | Script which updates the user's password. Must be executable (+x) and<br>have correct interpreter set e.g. #!/bin/bash<br><br>The following values are passed by the servlet to the script:<br>1. --cid - Context ID<br>2. --username - Username of the logged in user<br>3. --userid - User ID of the logged in user<br>4. --oldpassword - Old user password<br>5. --newpassword - New user password<br><br>See http://oxpedia.org/wiki/index.php?title=ChangePasswordExternal<br>for some examples<br><br>If script does not exit with status code 0, an io-error is shown in<br>the GUI.<br><br>The following exit codes will display a defined error:<br>1: Cannot change password < %s >, see logfiles for details.<br>2: New password is too short<br>3: New password is too weak<br>4: Cannot find user<br>5: LDAP error<br> |
| __Default__ |  |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.passwordchange.script.base64 |
| __File__ | change_pwd_script.properties |

---
| __Key__ | com.openexchange.passwordchange.script.base64 |
|:----------------|:--------|
| __Description__ | Indicates if the string based script parameters like username,<br>oldpassword and newpassword should be encoded as Base64 to circumvent<br>character encoding issues on improperly configured distributions not<br>providing an unicode environment for the process.<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.passwordchange.script.shellscript |
| __File__ | change_pwd_script.properties |

---
