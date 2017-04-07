---
title: Share
---

This page shows all properties with the tag: Share

| __Key__ | com.openexchange.capability.share_links |
|:----------------|:--------|
| __Description__ | Allows users to create share links to share files or folders.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | permissions.properties |

---
| __Key__ | com.openexchange.capability.invite_guests |
|:----------------|:--------|
| __Description__ | Allows users to share files or folder with guest users.<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Permission.html">Permission</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Capability.html">Capability</a> |
| __File__ | permissions.properties |

---
| __Key__ | com.openexchange.share.notification.usePersonalEmailAddress |
|:----------------|:--------|
| __Description__ | Specifies whether the user's personal E-Mail address (true) or the configured no-reply address (false) is supposed to be used in case a user<br>without mail permission sends out a sharing invitation<br> |
| __Default__ | false |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.userAgentBlacklist.enabled |
|:----------------|:--------|
| __Description__ | Enables (default) or disables black-listing of User-Agents that are about to access a share link.<br>If enabled the "com.openexchange.share.userAgentBlacklist.values" configuration option specifies what User-Agents to black-list.<br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.userAgentBlacklist.values |
|:----------------|:--------|
| __Description__ | Specifies a comma-separated list (ignore-case) of such User-Agents that are supposed to receive a "404 Not Found" when trying to<br>access a share link. White-card notation is supported; e.g. "\*aolbuild\*".<br>This configuration option is only effective if "com.openexchange.share.userAgentBlacklist.enabled" is set to "true"<br> |
| __Default__ | \*aolbuild\*, \*baiduspider\*, \*baidu\*search\*, \*bingbot\*, \*bingpreview\*, \*msnbot\*, \*duckduckgo\*, \*adsbot-google\*, \*googlebot\*, \*mediapartners-google\*, \*teoma\*, \*slurp\*, \*yandex\*bot\* |
| __Version__ | 7.8.4 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.share.userAgentBlacklist.enabled |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.guestHostname |
|:----------------|:--------|
| __Description__ | Configures a separate hostname to use for guest users. This hostname is used <br>when generating external share links, as well as at other locations where <br>hyperlinks are constructed in the context of guest users.<br>Usually, the guest hostname refers to a separate subdomain of the <br>installation like "share.example.com", and is defined as an additional named<br>virtual host pointing to the web client's document root in the webserver's <br>configuration.<br>This property may defined statically here, overridden via config cascade, or<br>be provided through an additionally installed hostname service.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.cleanup.guestExpiry |
|:----------------|:--------|
| __Description__ | Defines the timespan after which an unused named guest user should be <br>removed from the system. "Unused" time starts after the last share to the <br>guest user has been revoked. This setting only affects "named" guest users,<br>i.e. users that were invited explicitly and authenticate using their <br>e-mail-address.<br>The value can be defined using units of measurement: "D" (=days), <br>"W" (=weeks) and "H" (=hours). Defaults to "2W" (two weeks), with a minimum <br>of "1D" (one day). A value of "0" disables the delayed guest user deletion, <br>so that guest users are deleted right after the last share to them was <br>removed.  <br> |
| __Default__ | 2W |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.cleanup.periodicCleanerInterval |
|:----------------|:--------|
| __Description__ | Defines the interval of a periodic background task that performs <br>sharing-related cleanup operations like removal of expired shares or final<br>deletion of no longer used guest users. The task is executed only once per <br>interval in the cluster, so this value should be equally defined on each<br>node.<br>The value can be defined using units of measurement: "D" (=days), <br>"W" (=weeks) and "H" (=hours). Defaults to "1D" (one day), with a minimum <br>of "1H" (one hour). A value of "0" disables the periodic background task.<br> |
| __Default__ | 1D |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.quota.share_links |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of share links that are allowed being <br>created by one user. A value < 0 means unlimited.<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.quota.invite_guests |
|:----------------|:--------|
| __Description__ | Specifies the quota for the number of guest users that are allowed being <br>created by one user. A value < 0 means unlimited.<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Quota.html">Quota</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.modulemapping |
|:----------------|:--------|
| __Description__ | Mapping from arbitrary module names to module identifiers and vice versa. <br>The value must be a comma-separated list of module names and its identifier <br>linked with an equals sign.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.cryptKey |
|:----------------|:--------|
| __Description__ | Defines a key that is used to encrypt the password/pin of anonymously <br>accessible shares in the database.  <br>Defaults to "erE2e8OhAo71", and should be changed before the creation of the<br>first share on the system.<br> |
| __Default__ | erE2e8OhAo71 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encryption.html">Encryption</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Security.html">Security</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.handler.iCal.futureInterval |
|:----------------|:--------|
| __Description__ | Defines up to which time in the future appointments and tasks are included <br>when accessing an iCal-serializable share with a client requesting the <br>"text/calendar" representation of the resource.<br>Possible values are "one_month", "six_months", "one_year" and "two_years".<br> |
| __Default__ | one_year |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Ical.html">Ical</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.handler.iCal.pastInterval |
|:----------------|:--------|
| __Description__ | Defines up to which time in the past appointments and tasks are included <br>when accessing an iCal-serializable share with a client requesting the <br>"text/calendar" representation of the resource.<br>Possible values are "two_weeks", "one_month", "six_months" and "one_year".<br> |
| __Default__ | two_weeks |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Ical.html">Ical</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Appointment.html">Appointment</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.loginLink |
|:----------------|:--------|
| __Description__ | Specifies the location to redirect to when accessing a share link that <br>requires authentication. This is usually the default login page that will <br>then perform the "anonymous" or "guest" login action.<br>[uiwebpath] is replaced with the value of "com.openexchange.UIWebPath" as <br>defined in "server.properties", trimmed by leading and trailing '/' <br>characters.<br> |
| __Default__ | /[uiwebpath]/ui |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.crossContextGuests |
|:----------------|:--------|
| __Description__ | Configures whether attributes of named guest users should be synchronized <br>across context boundaries or not, i.e. if their password or other related<br>metadata should be kept equally in each context of the same context group<br>the guest user has shares from. This requires a configured global / cross-<br>context database (see configuration file "globaldb.yml" for details). <br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.notifyInternal |
|:----------------|:--------|
| __Description__ | Configures if internal users should receive a notification email whenever<br>a folder or item has been shared to them. <br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.guestCapabilityMode |
|:----------------|:--------|
| __Description__ | As guests are plain user entities internally, they carry a set of capabilities<br>which define the enabled feature set. Basically guest users contain a set<br>of capabilities that allows them to read or write the items that have been<br>shared with them. Additionally it is possible to define further capabilities<br>to let guests have some more of the installed features enabled. This setting<br>can also be changed via config cascade for certain contexts and context sets.<br>One of three different modes can be chosen:<br>  - deny_all: guest users have no additional capabilities applied<br>  - static:   every guest user obtains a statically configured set of<br>        capabilities (com.openexchange.share.staticGuestCapabilities)<br>  - inherit:  guest users inherit the capabilities of the user who created<br>        the (initial) according share<br> |
| __Default__ | static |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.staticGuestCapabilities |
|:----------------|:--------|
| __Description__ | Defines the static set of capabilities that shall be given to guests with<br>capability mode 'static'. Capabilities must be specified as a comma-separated<br>string (e.g. "drive, document_preview"; without the double quotes).<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.transientSessions |
|:----------------|:--------|
| __Description__ | Specifies whether guest sessions are treated as transient or not. Transient<br>sessions are only held in the short-term session containers, and are not put<br>into the distributed session storage. <br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Session.html">Session</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.autoLogin |
|:----------------|:--------|
| __Description__ | Enables/disables the auto-login functionality for guest sessions. A value of <br>"true" will instruct the client to store an additional cookie, which is then <br>considered to lookup an existing session when the client is reloaded. <br>Possible values are "true", "false", or an empty value (the default) to <br>inherit from "com.openexchange.sessiond.autologin".<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

---
| __Key__ | com.openexchange.share.cookieTTL |
|:----------------|:--------|
| __Description__ | Configures the target time-to-live value for the session, secret and share <br>cookies used within guest sessions. The value can be defined using units of <br>measurement: "D" (=days), "W" (=weeks) and "H" (=hours). Additionally, to <br>enforce a "session" cookie lifetime (i.e. cookies are deleted once the client<br>is quit), the value "-1" or "web-browser" may be specified. An empty value <br>will let this setting inherit from "com.openexchange.cookie.ttl". <br> |
| __Default__ | -1 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | share.properties |

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
| __Key__ | com.openexchange.download.limit.enabled |
|:----------------|:--------|
| __Description__ | If the feature is disabled (in general or for guests/links) no downloads will be tracked which means after<br>activation each guest/link starts with used counts/size 0.<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.timeFrame.guests |
|:----------------|:--------|
| __Description__ | Specify the limit (in milliseconds) time window in which to track (and possibly <br>deny) incoming download requests for known (guests) guest users.<br>That rate limit acts like a sliding window time frame; meaning that it considers only<br>requests that fit into time windows specified through "com.openexchange.download.limit.guests.timeFrame" <br>from current time stamp:<br>window-end := $now<br>window-start := $window-end - $timeFrame<br>If you only want to specify only one limit (size or count) you have to set a time frame and specify the desired<br> |
| __Default__ | 3600000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.timeFrame.links |
|:----------------|:--------|
| __Description__ | Specify the limit (in milliseconds) time window in which to track (and possibly <br>deny) incoming download requests for anonymous (links) guest users.<br>That rate limit acts like a sliding window time frame; meaning that it considers only<br>requests that fit into time windows specified through "com.openexchange.download.limit.links.timeFrame" <br>from current time stamp:<br>window-end := $now<br>window-start := $window-end - $timeFrame<br>If you only want to specify only one limit (size or count) you have to set a time frame and specify the desired<br> |
| __Default__ | 3600000 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.size.guests |
|:----------------|:--------|
| __Description__ | Specify the download size limit<br>A guest (link or known) that exceeds that limit will receive an error<br>Default is 1073741824 (1 GB) bytes per $timeFrame.<br>To disable the size check set value to 0<br> |
| __Default__ | 1073741824 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.size.links |
|:----------------|:--------|
| __Description__ | Specify the download size limit<br>A guest (link or known) that exceeds that limit will receive an error<br>Default is 1073741824 (1 GB) bytes per $timeFrame.<br>To disable the size check set value to 0<br> |
| __Default__ | 1073741824 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.count.guests |
|:----------------|:--------|
| __Description__ | Default is 100 downloads per $timeFrame.<br>A guest (link or known)  that exceeds that limit will receive an error<br>To disable the count check set value to 0<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
| __Key__ | com.openexchange.download.limit.count.links |
|:----------------|:--------|
| __Description__ | Default is 100 downloads per $timeFrame.<br>A guest (link or known)  that exceeds that limit will receive an error<br>To disable the count check set value to 0<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Download.html">Download</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Limit.html">Limit</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Share.html">Share</a> |
| __File__ | download.properties |

---
