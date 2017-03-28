---
title: Twitter
---

This page shows all properties with the tag: Twitter

| __Key__ | com.openexchange.oauth.[serviceId] |
|:----------------|:--------|
| __Description__ | Enables or disables the oauth service with the service id [serviceId]. <br><br>Currently known service ids:<br>  \* boxcom<br>  \* dropbox<br>  \* google<br>  \* linkedin<br>  \* msliveconnect<br>  \* twitter<br>  \* xing<br>  \* yahoo<br> |
| __Default__ | false |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Boxcom.html">Boxcom</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MSLiveConnect.html">MSLiveConnect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a> |

---
| __Key__ | com.openexchange.oauth.[serviceId].apiKey |
|:----------------|:--------|
| __Description__ | The api key of your [serviceId] application.<br><br>See com.openexchange.oauth.[serviceId] for a list of currently known service ids.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Boxcom.html">Boxcom</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MSLiveConnect.html">MSLiveConnect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a> |

---
| __Key__ | com.openexchange.oauth.[serviceId].apiSecret |
|:----------------|:--------|
| __Description__ | The api secret of your [serviceId] application.<br><br>See com.openexchange.oauth.[serviceId] for a list of currently known service ids.<br> |
| __Reloadable__ | true |
| __Configcascade Aware__ | true |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/OAuth.html">OAuth</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Boxcom.html">Boxcom</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Dropbox.html">Dropbox</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Google.html">Google</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/LinkedIn.html">LinkedIn</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/MSLiveConnect.html">MSLiveConnect</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Xing.html">Xing</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Yahoo.html">Yahoo</a> |

---
| __Key__ | com.openexchange.twitter.clientVersion |
|:----------------|:--------|
| __Description__ | The client version string<br> |
| __Default__ | 2.2.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a> |
| __File__ | twitter.properties |

---
| __Key__ | com.openexchange.twitter.http.proxyHost |
|:----------------|:--------|
| __Description__ | The HTTP proxy host.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.twitter.http.proxyPort |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | twitter.properties |

---
| __Key__ | com.openexchange.twitter.http.proxyPort |
|:----------------|:--------|
| __Description__ | The HTTP proxy port.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.twitter.http.proxyHost |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | twitter.properties |

---
| __Key__ | com.openexchange.twitter.http.connectionTimeout |
|:----------------|:--------|
| __Description__ | Connection time out<br> |
| __Default__ | 20000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a> |
| __File__ | twitter.properties |

---
| __Key__ | com.openexchange.twitter.http.readTimeout |
|:----------------|:--------|
| __Description__ | Read time out<br> |
| __Default__ | 120000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a> |
| __File__ | twitter.properties |

---
| __Key__ | com.openexchange.twitter.http.retryCount |
|:----------------|:--------|
| __Description__ | Retry count<br> |
| __Default__ | 3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a> |
| __File__ | twitter.properties |

---
| __Key__ | com.openexchange.twitter.http.retryIntervalSecs |
|:----------------|:--------|
| __Description__ | Retry interval seconds<br> |
| __Default__ | 10 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Twitter.html">Twitter</a> |
| __File__ | twitter.properties |

---
