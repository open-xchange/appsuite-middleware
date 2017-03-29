---
title: Download
---

This page shows all properties with the tag: Download

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
