---
title: Contact
---

This page shows all properties with the tag: Contact

| __Key__ | com.openexchange.user.contactCollectOnMailAccess |
|:----------------|:--------|
| __Description__ | Define the default behavior whether to collect contacts on mail access.<br>Note: Appropriate user access permission still needs to be granted in order to take effect.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | user.properties |

---
| __Key__ | com.openexchange.user.contactCollectOnMailTransport |
|:----------------|:--------|
| __Description__ | Define the default behavior whether to collect contacts on mail transport.<br>Note: Appropriate user access permission still needs to be granted in order to take effect.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Transport.html">Transport</a> |
| __File__ | user.properties |

---
| __Key__ | IGNORE_SHARED_ADDRESSBOOK |
|:----------------|:--------|
| __Description__ | Determine whether to ignore 'shared addressbook' folder or not.<br>Possible values: TRUE / FALSE<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Cache.html">Cache</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | foldercache.properties |

---
| __Key__ | ENABLE_INTERNAL_USER_EDIT |
|:----------------|:--------|
| __Description__ | Define if users are allowed to edit their own contact object<br>contained in folder 'Global Address Book' aka 'Internal Users'.<br> |
| __Default__ | TRUE |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Folder.html">Folder</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a> |
| __File__ | foldercache.properties |

---
| __Key__ | com.openexchange.quota.contact |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of contacts that are allowed being created within a single context (tenant-wise scope).<br><br>The purpose of this quota is to define a rough upper limit that is unlikely being reached during normal operation.<br>Therefore it is rather supposed to prevent from excessive item creation (e.g. a synchronizing client running mad),<br>but not intended to have a fine-grained quota setting. Thus exceeding that quota limitation will cause an appropriate<br>exception being thrown, denying to further create any contact in affected context.<br> |
| __Default__ | 250000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a> |
| __File__ | quota.properties |

---
| __Key__ | com.openexchange.subscribe.google.contact.pageSize |
|:----------------|:--------|
| __Description__ | Defines the amount of contacts to fetch in a single request.<br> |
| __Default__ | 25 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | googlesubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.google.contact.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for Google contacts.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | googlesubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.linkedin |
|:----------------|:--------|
| __Description__ | Enable/disable LinkedIn subscribe service.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | linkedinsubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.linkedin.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for LinkedIn.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | linkedinsubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.mslive.contact.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for MS Live.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MSLiveConnect.html">MSLiveConnect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | mslivesubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.microformats.contacts.http |
|:----------------|:--------|
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Microformats.html">Microformats</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | microformatSubscription.properties |

---
| __Key__ | com.openexchange.subscribe.microformats.contacts.http.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for Microformats.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Microformats.html">Microformats</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | microformatSubscription.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.yahoo |
|:----------------|:--------|
| __Description__ | Enable/disable Yahoo subscribe service.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | yahoosubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.yahoo.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for Yahoo.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | yahoosubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.xing |
|:----------------|:--------|
| __Description__ | Enable/disable XING subscribe service.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/XING.html">XING</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | xingsubscribe.properties |

---
| __Key__ | com.openexchange.subscribe.socialplugin.xing.autorunInterval |
|:----------------|:--------|
| __Description__ | Defines the subscription refresh autorun interval for XING.<br> |
| __Default__ | 1d |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/XING.html">XING</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Contact.html">Contact</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Subscribe.html">Subscribe</a> |
| __File__ | xingsubscribe.properties |

---
