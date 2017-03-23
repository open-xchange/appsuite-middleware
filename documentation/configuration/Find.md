---
title: Find
---

| __Key__ | com.openexchange.find.basic.mail.allMessagesFolder |
|:----------------|:--------|
| __Description__ | Some mail backends provide a virtual folder that contains all messages of<br>a user to enable cross-folder mail search. Open-Xchange can make use of<br>this feature to improve the search experience.<br>Set the value to the name of the virtual mail folder containing all messages.<br>Leave blank if no such folder exists.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Find.html">Find</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | findbasic.properties |

---
| __Key__ | com.openexchange.find.basic.mail.searchmailbody |
|:----------------|:--------|
| __Description__ | Denotes if mail search queries should be matched against mail bodies.<br>This improves the search experience within the mail module, if your mail<br>backend supports fast full text search. Otherwise it can slow down the<br>search requests significantly.<br>Change the value to 'true', if fast full text search is supported.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Find.html">Find</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | findbasic.properties |

---
