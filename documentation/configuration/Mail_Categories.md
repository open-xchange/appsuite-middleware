---
title: Mail Categories
---

This pages describes the properties of the mail categories feature aka tabbed inbox.

There are two types of categories: 'system' and 'user' categories.

System categories are predefined categories which includes predefined rules. 
The hoster is able to force showing this categories.
In addition the user is unable to rename them.

User categories are a finite set of categories which can be used by the user to create own categories.
Thats means that there are no predefined rules. This categories cannot be forced, but they can be renamed by the user. 
The hoster should nevertheless give them some meaningful names to help the users. E.g. "Friends".


| __Key__ | com.openexchange.mail.categories |
|:----------------|:--------|
| __Description__ | General capability to enable/disable mail categories for primary inbox<br> |
| __Default__ | false |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.enabled |
|:----------------|:--------|
| __Description__ | Switch to show or hide mail categories feature during the first start. <br>Notice that this property only influence the starting value. <br>Changing this value will probably have no effect on users with already have "com.openexchange.mail.categories" set to true.<br> |
| __Default__ | true |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.forced |
|:----------------|:--------|
| __Description__ | Switch to force showing the mail categories feature. <br>If set to true, the com.openexchange.mail.categories.enabled property is always true and can't be changed.<br> |
| __Default__ | false |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.general.name.fallback |
|:----------------|:--------|
| __Description__ | The fallback name of the default general category.<br> |
| __Default__ | General |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.general.name.[locale] |
|:----------------|:--------|
| __Description__ | For each language which should be supported a translated name for the general category should be defined.<br>For each entry [locale] must be replaced with a ISO-639-2 language code followed by a underscore followed by a ISO-3166 country code (e.g. de_DE or en_US)<br><br>NOTE: Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe"<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.identifiers |
|:----------------|:--------|
| __Description__ | Specifies a comma separated list of system category identifiers ([category]).<br><br>System categories can be forced but not renamed.<br>Please note that the use of "general" is prohibited!<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.user.categories.identifiers |
|:----------------|:--------|
| __Description__ | Specifies a comma separated list of user category identifiers ([category]). E.g.: "uc1,uc2,uc3"<br><br>User categories can be renamed but not be forced.<br>Please note that the use of "general" is prohibited!<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].flag |
|:----------------|:--------|
| __Description__ | Specifies the category's flag name that is supposed to be used for filter/search expressions executed by mail back-end;<br>e.g. "com.openexchange.mail.categories.offers.flag=$offers"<br>Required. <br>[category] must be replaced with the actual category identifier.<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].force |
|:----------------|:--------|
| __Description__ | Specifies whether the category is forced; meaning a user is not allowed to disable the category.<br>Required. Only for system categories.<br>[category] must be replaced with the actual category identifier.<br> |
| __Default__ | false |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].active |
|:----------------|:--------|
| __Description__ | Specifies whether the category is activated/deactivate for a user. Only effective if "force" is set to "false".<br>This setting can be set by a user.<br>Required.<br>[category] must be replaced with the actual category identifier.<br> |
| __Default__ | true |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].name.fallback |
|:----------------|:--------|
| __Description__ | Specifies the category's fall-back name.<br>Required.<br>[category] must be replaced with the actual category identifier.<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].name.[locale] |
|:----------------|:--------|
| __Description__ | For each language which should be supported a translated name for the category should be defined.<br>For each entry [locale] must be replaced with a ISO-639-2 language code followed by a underscore followed by a ISO 3166 country code (e.g. de_DE or en_US).<br>[category] must be replaced with the actual category identifier.<br><br>NOTE: Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe"<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].description |
|:----------------|:--------|
| __Description__ | Specifies an optional system category description.<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.[category].description.[locale] |
|:----------------|:--------|
| __Description__ | For each language which should be supported a translated description for the category should be defined.<br>For each entry [locale] must be replaced with a ISO-639-2 language code followed by a underscore followed by a ISO 3166 country code (e.g. de_DE or en_US).<br>[category] must be replaced with the actual category identifier.<br><br>NOTE: Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe"<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.apply.ox.rules |
|:----------------|:--------|
| __Description__ | It is possible to create some predefined rules for the system categories (see com.openexchange.mail.categories.rules.[category]).<br>This rules will be added for each user which has the mail_categories capability and has the mail_categories feature enabled.<br>This property enables/disables this feature. Nevertheless it is strongly recommended to use system wide rules instead.<br>Please notice that these rules must only be used instead of system wide rules! Don't use this feature if there are already system wide rules defined!       <br> |
| __Default__ | false |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.categories.rules.[category] |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
| __Key__ | com.openexchange.mail.categories.rules.[category] |
|:----------------|:--------|
| __Description__ | For each system category a comma separated list of mail addresses can be defined. <br>This addresses will be used to create a starting rule for this category if com.openexchange.mail.categories.apply.ox.rules is set to 'true'.  <br>It is also possible to use mail address parts here. For example "@amazon.com".<br> |
| __Version__ | 7.8.2 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Related__ | com.openexchange.mail.categories.apply.ox.rules |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail_Categories.html">Mail Categories</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | mail-categories.properties |

---
