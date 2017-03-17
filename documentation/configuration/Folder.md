---
title: Folder
---

| __Key__ | com.openexchange.folderstorage.defaultPermissions |
|:----------------|:--------|
| __Description__ | Specifies default permission to use in case folder is supposed to be created below a certain parent folder.<br>The value is a pipe ("&#124;") separated listing of expressions; each expression defines the default permissions<br>for a denoted parent folder. Currently the reserved folder identifiers "2" and "15" are considered as "2"<br>denoted the public PIM folder whereas "15" denotes the public Drive folder.<br><br>An expression starts with the parent folder identifier followed by '=' character; e.g. "2=".<br>Then there is a comma-separated list of permissions to assume per entity (user or group).<br><br>Each permission either starts with "user_", "admin_user_", "group_" or "admin_group_" (the prefix "admin_" controls<br>whether the entity is supposed to be set as folder administrator) followed by the numeric entity identifier.<br><br>Then an '@' character is supposed to occur and finally followed by rights expression. The rights may be dot-separated<br>listing (<folder-permission> + "." + <read-permission> + "." + <write-permission> + "." + <delete-permission>) or one<br>of the tokens  "viewer", "writer" or "author".<br><br>More formally<br>expressions = expression ("&#124;" expression)\*<br>expression = folder "=" permission ("," permission)\*<br>permission = ("admin_")? ("group_" &#124; "user_") entity(int) "@" rights<br>rights = (folder-permission(int) "." read-permission(int) "." write-permission(int) "." delete-permission(int)) &#124; ("viewer" &#124; "writer" &#124; "author")<br><br>Example<br>2=group_2@2.4.0.0,admin_user_5@8.4.4.4&#124;15=admin_group_2@8.8.8.8<br>2=group_2@viewer,admin_user_5@author&#124;15=admin_group_2@writer<br> |
| __Default__ | No defaut value |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __File__ | foldercache.properties |

---
| __Key__ | com.openexchange.folderstorage.inheritParentPermissions |
|:----------------|:--------|
| __Description__ | Specifies if permissions of new parent folder should be applied when moving a folder into the public folder tree.<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __File__ | foldercache.properties |

---
