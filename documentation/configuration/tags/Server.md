---
title: Server
---

This page shows all properties with the tag: Server

| __Key__ | com.openexchange.servlet.useRobotsMetaTag |
|:----------------|:--------|
| __Description__ | Specifies whether the special "X-Robots-Tag" HTTP response header is set<br> |
| __Default__ | true |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | server.properties |

---
| __Key__ | com.openexchange.servlet.robotsMetaTag |
|:----------------|:--------|
| __Description__ | Specifies the value for the "X-Robots-Tag" HTTP response. Default value is "none".<br>See https://developers.google.com/webmasters/control-crawl-index/docs/robots_meta_tag<br> |
| __Default__ | none |
| __Version__ | 7.8.4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Related__ | com.openexchange.servlet.useRobotsMetaTag |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | server.properties |

---
| __Key__ | PrefetchEnabled |
|:----------------|:--------|
| __Description__ | "Enable or disable SearchIterator prefetch. If prefetch is enabled the underlying<br>ResultSet data is completely stored and all related resources are released<br>immediately when creating a SearchIterator. Possible values: TRUE / FALSE"<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | UPLOAD_DIRECTORY |
|:----------------|:--------|
| __Description__ | Upload Parameter. All uploads that are done by a client are temporarily saved<br>in that directory.<br> |
| __Default__ | /var/spool/open-xchange/uploads |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | MAX_UPLOAD_SIZE |
|:----------------|:--------|
| __Description__ | If the sum of all uploaded files in one request is greater than this value, the upload will be rejected.<br>This value can be overridden on a per-module basis by setting the MAX_UPLOAD_SIZE parameter in the modules<br>config file (attachment.properties or infostore.properties). Or it can be overriden on a per-user<br>basis by setting the upload_quota in the mail configuration of this user<br>If this value is set to 0 and not overridden by the module config or user config uploads will be unrestricted.<br>The size is in bytes.<br> |
| __Default__ | 104857600 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | MAX_BODY_SIZE |
|:----------------|:--------|
| __Description__ | The max. HTTP body size<br>Zero or less means infinite.<br>/!\   "Deprecated:" Use "com.openexchange.servlet.maxBodySize" instead   /!\<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.defaultMaxConcurrentAJAXRequests |
|:----------------|:--------|
| __Description__ | The default max. number of allowed concurrent requests per user<br>This property only has effect if no individual value has been specified for active user<br>A value less than or equal to zero means infinite<br> |
| __Default__ | 100 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | DefaultEncoding |
|:----------------|:--------|
| __Description__ | DEFAULT ENCODING FOR INCOMING HTTP REQUESTS<br>This value MUST be equal to web server's default encoding<br> |
| __Default__ | UTF-8 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.MinimumSearchCharacters |
|:----------------|:--------|
| __Description__ | Minimum number of characters a search pattern must contain. 0 means no minimum.<br>This should prevent slow searches for contacts or big responses in large<br>contexts.<br> |
| __Default__ | 0 |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | PUBLISH_REVOKE |
|:----------------|:--------|
| __Description__ | e-mail address of a person that can be called if data has been published<br>and the actual owner of the data objects to the publication. If removed or<br>left empty, the e-maill address of the context admin is used.<br> |
| __Default__ | empty |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.IPCheck |
|:----------------|:--------|
| __Description__ | On session validation of every request the client IP address is compared with the client IP address used for the login request. If this<br>configuration parameter is set to true and the client IP addresses do not match the request will be denied and the denied request is<br>logged with level info. Setting this parameter to false will only log the different client IP addresses with debug level.<br><br>WARNING! This should be only set to false if you know what you are doing and if all requests are secure - requests are always encrypted<br>by using HTTPS.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.IPMaskV4 |
|:----------------|:--------|
| __Description__ | Subnet mask for accepting IP-ranges.<br>Using CIDR-Notation for v4 and v6 or dotted decimal only for v4.<br>"Examples:"<br>com.openexchange.IPMaskV4=255.255.255.0<br>com.openexchange.IPMaskV4=/24<br>com.openexchange.IPMaskV6=/60<br> |
| __Default__ | empty |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.IPMaskV6 |
|:----------------|:--------|
| __Description__ | Subnet mask for accepting IP-ranges.<br>Using CIDR-Notation for v4 and v6 or dotted decimal only for v4.<br>"Examples:"<br>com.openexchange.IPMaskV4=255.255.255.0<br>com.openexchange.IPMaskV4=/24<br>com.openexchange.IPMaskV6=/60<br> |
| __Default__ | empty |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.IPCheckWhitelist |
|:----------------|:--------|
| __Description__ | Specify a comma-separated list of client patterns that do bypass IP check<br>E.g. com.openexchange.IPCheckWhitelist="Mobile App\*", "Foo\*"<br> |
| __Default__ | open-xchange-mailapp, open-xchange-mobile-api-facade |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/White_List.html">White List</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.UIWebPath |
|:----------------|:--------|
| __Description__ | Configures the path on the web server where the UI is located. This path is used to generate links directly into the UI. The default<br>conforms to the path where the UI is installed by the standard packages on the web server. This path is used for the [uiwebpath].<br>For the Open-Xchange 6 frontend the path needs to be configured to "/ox6/index.html".<br>For the App Suite frontend the path needs to be configured to "/appsuite/" which is the default.<br> |
| __Default__ | /appsuite/ |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.dispatcher.prefix |
|:----------------|:--------|
| __Description__ | Specify the prefix for Central Dispatcher framework (the Open-Xchange AJAX interface)<br> |
| __Default__ | /ajax/ |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.cookie.ttl |
|:----------------|:--------|
| __Description__ | Special identifier "web-browser" to let the Cookie(s) be deleted when the Web browser exits<br> |
| __Default__ | 1W |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.cookie.httpOnly |
|:----------------|:--------|
| __Description__ | Whether the "; HttpOnly" should be appended to server cookies exits<br> |
| __Default__ | true |
| __Reloadable__ | true |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.cookie.hash |
|:----------------|:--------|
| __Description__ | Whether the hash value for the cookie name should be calculated or remembered from the session for each request.<br>"Possible values are:"<br>calculate (default) - Calculate hash from client login parameter and HTTP header User-Agent<br>(may be modified through c.o.cookie.hash.fields)<br>remember - remember once calculated hash in session and use it for the whole session life time (less secure)<br> |
| __Default__ | calculate |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.cookie.hash.salt |
|:----------------|:--------|
| __Description__ | Cookie hash salt to avoid a potential brute force attack to cookie hashes.<br>This value should be replaced by any random String with at least 16 Characters.<br> |
| __Default__ | replaceMe1234567890 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.forceHTTPS |
|:----------------|:--------|
| __Description__ | If an HTTPS connection is detected the cookie is automatically marked as secure. This tells the browser only to send the cookie over<br>encrypted connections. If HTTPS is terminated in front by some load balancer only HTTP is detected. Then this parameter can force to set<br>the secure flag for cookies. Additionally all links generated inside the groupware to point at itself will use the https:// protocol prefix<br>when this is set. Use this flag to indicate that HTTPS termination happens elsewhere.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxActiveSessions |
|:----------------|:--------|
| __Description__ | The maximum number of active sessions that will be created by this Manager, or -1 for no limit.<br>Default is 250.000 HTTP sessions<br> |
| __Default__ | 250000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxInactiveInterval |
|:----------------|:--------|
| __Description__ | The initial maximum time interval, in seconds, between client requests before a HTTP session is invalidated.<br>This only applies to the HTTP session controlled by special JSESSIONID cookie, and does therefore not influence life-time of Groupware session.<br>A negative value will result in sessions never timing out.<br>If the attribute is not provided, a default of 1800 seconds is used.<br> |
| __Default__ | 1800 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxBodySize |
|:----------------|:--------|
| __Description__ | Specify the maximum body size allowed being transferred via PUT or POST method<br>A request exceeding that limit will be responded with a 500 error and that request is discarded<br>Equal or less than zero means no restriction concerning body size<br>By default limit is set to 100MB (104857600 bytes).<br> |
| __Default__ | 104857600 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.echoHeaderName |
|:----------------|:--------|
| __Description__ | Specify the name of the echo header whose value is echoed for each request providing that header<br> |
| __Default__ | X-Echo-Header |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.contentSecurityPolicy |
|:----------------|:--------|
| __Description__ | The value of Content-Security-Policy header<br>Please refer to An Introduction to Content Security Policy (http://www.html5rocks.com/en/tutorials/security/content-security-policy/)<br>Default value is empty; meaning no Content-Security-Policy header<br> |
| __Default__ | empty |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxRateTimeWindow |
|:----------------|:--------|
| __Description__ | Specify the rate limit' time window in which to track incoming HTTP requests<br>Default value is 300000 (5 minutes).<br> |
| __Default__ | 300000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxRate |
|:----------------|:--------|
| __Description__ | Specify the rate limit that applies to incoming HTTP requests<br>A client that exceeds that limit will receive a "429 Too Many Requests" HTTP error code<br>That rate limit acts like a sliding window time frame; meaning that it considers only<br>requests that fit into time windows specified through "com.openexchange.servlet.maxRateTimeWindow" from current time "stamp:"<br>window-end := $now<br>window-start := $window-end - $maxRateTimeWindow<br>Default value is 1500 requests per $maxRateTimeWindow (default: 5 minutes).<br> |
| __Default__ | 1500 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxRateLenientModules |
|:----------------|:--------|
| __Description__ | Specify those AJAX-accessible modules which are excluded from the rate limit checks<br> |
| __Default__ | rt, system |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxRateLenientClients |
|:----------------|:--------|
| __Description__ | Specify (wildcard notation supported) those User-Agents which are excluded from the rate limit checks<br>"Default value:" "Open-Xchange .NET HTTP Client\*", "Open-Xchange USM HTTP Client\*", "Jakarta Commons-HttpClient\*"<br> |
| __Default__ | Open-Xchange .NET HTTP Client\*, Open-Xchange USM HTTP Client\*, Jakarta Commons-HttpClient\* |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxRateLenientRemoteAddresses |
|:----------------|:--------|
| __Description__ | Specify (wildcard notation supported) those remote addresses/IPs which are excluded from the rate limit checks<br> |
| __Default__ | empty |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxRateLenientRemoteAddresses |
|:----------------|:--------|
| __Description__ | Specify how to associate a rate limit with a HTTP request<br>By default a rate limit is associated with that tuple of { remote-address, user-agent }<br>This does not apply to all scenarios. Therefore it is possible to specify more parts in a comma-separated list that build up the key.<br>"Possible values are:"<br>- "http-session"                   := The identifier of the request-associated HTTP session aka JSESSIONID cookie<br>- "cookie-" + <cookie-name>        := Specifies the (ignore-case) name of the arbitrary cookie whose value is considered. If missing it is ignored.<br>- "header-" + <header-name>        := Specifies the (ignore-case) name of the arbitrary header whose value is considered. If missing it is ignored.<br>- "parameter-" + <parameter-name>  := Specifies the (ignore-case) name of the arbitrary parameter whose value is considered. If missing it is ignored.<br><br>E.g. com.openexchange.servlet.maxRateKeyPartProviders=cookie-My-Secret-Cookie, parameter-request_num<br> |
| __Default__ | empty |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.servlet.maxRateKeyPartProviders |
|:----------------|:--------|
| __Description__ | Default is empty; meaning only remote-address and user-agent are considered<br> |
| __Default__ | empty |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.log.maxMessageLength |
|:----------------|:--------|
| __Description__ | Specifies max. message length to log. Messages that exceed that limit are split.<br>A value of less than 1 means unlimited.<br>Default is -1 (unlimited)<br> |
| __Default__ | -1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.json.maxSize |
|:----------------|:--------|
| __Description__ | Specify the max. number of allowed attributes for a JSON object<br> |
| __Default__ | 2500 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.webdav.disabled |
|:----------------|:--------|
| __Description__ | Setting this to true means that the WebDav XML servlets for attachments, calendar, contacts and tasks will be disabled.<br>On requests to these servlets a service-not-available (503) response will be sent. See "webdav-disabled-message.txt"<br>to customize the detailed error message that will be sent.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.ajax.response.includeStackTraceOnError |
|:----------------|:--------|
| __Description__ | Whether the JSON response object should provide the stack trace of the associated exception<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.ajax.response.includeArguments |
|:----------------|:--------|
| __Description__ | Whether the JSON response object in case of an error should include the exception arguments or not<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.connector.networkListenerHost |
|:----------------|:--------|
| __Description__ | The host for the connector's http network listener. Set to "\*" if you<br>want to listen on all available interfaces.<br>"Default value:" 127.0.0.1, bind to localhost only.<br> |
| __Default__ | 127.0.0.1 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Host.html">Host</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.connector.networkListenerPort |
|:----------------|:--------|
| __Description__ | The default port for the connector's http network listener<br> |
| __Default__ | 8009 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.connector.networkSslListenerPort |
|:----------------|:--------|
| __Description__ | The default port for the connector's https network listener<br> |
| __Default__ | 8010 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Port.html">Port</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.connector.maxRequestParameters |
|:----------------|:--------|
| __Description__ | Specify the max. number of allowed request parameters for the connector http<br> |
| __Default__ | 1000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.connector.shutdownFast |
|:----------------|:--------|
| __Description__ | Controls the shut-down behavior of the HTTP connector.<br><br>If fast shut-down is enabled, the HTTP connector is attempted to be stopped as fast as possible.<br>This could lead to currently in-progress requests to quit with errors (due to absent services); e.g. NullPointerExceptions.<br><br>If set to "false" (default) an orderly shut-down is initiated; waiting for currently running requests to terminate. This may slow down<br>the shut-down sequence.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.connector.awaitShutDownSeconds |
|:----------------|:--------|
| __Description__ | Specify the number of seconds to await an orderly shut-down<br>(only effective if "com.openexchange.connector.shutdownFast" is set to "false")<br><br>A value of less than or equal to 0 (zero) will cause to wait forever; otherwise a hard shut-down is initiated if the wait time elapsed.<br>the shut-down sequence.<br> |
| __Default__ | 90 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.server.backendRoute |
|:----------------|:--------|
| __Description__ | To enable proper load balancing and request routing from {client1, client2 ..<br>.} --> balancer --> {backend1, backend2 ...} we have to append a backend route<br>to the JSESSIONID cookies separated by a '.'. It's important that this backend<br>route is unique for every single backend behind the load balancer.<br>The string has to be a sequence of characters excluding semi-colon, comma and<br>white space so the JSESSIONID cookie stays in accordance with the cookie<br>specification after we append the backendroute to it.<br> |
| __Default__ | OX0 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.server.considerXForwards |
|:----------------|:--------|
| __Description__ | Decides if we should consider X-Forward-Headers that reach the backend.<br>Those can be spoofed by clients so we have to make sure to consider the headers only if the proxy/proxies reliably override those<br>headers for incoming requests.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.server.forHeader |
|:----------------|:--------|
| __Description__ | The name of the protocolHeader used to identify the originating IP address of<br>a client connecting to a web server through an HTTP proxy or load balancer.<br>This is needed for grizzly based setups that make use of http proxying.<br>If the header isn't found the first proxy in front of grizzly will be used<br>as originating IP/remote address.<br> |
| __Default__ | X-Forwarded-For |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.server.knownProxies |
|:----------------|:--------|
| __Description__ | A list of know proxies in front of our httpserver/balancer as comma separated IPs e.g: 192.168.1.50, 192.168.1.51<br> |
| __Default__ | empty |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.html.css.parse.timeout |
|:----------------|:--------|
| __Description__ | Specify the amount of seconds to wait for a CSS content being parsed.<br>This property influences parsing of HTML messages. If CSS could not be parsed in time, CSS is stripped from message's content.<br> |
| __Default__ | 4 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Timeout.html">Timeout</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.log.suppressedCategories |
|:----------------|:--------|
| __Description__ | Specify the OXException categories (comma separated) that shall not be logged.<br>The Exception itself will still be logged as configured, but the StackTraces are omitted.<br>Valid categories are ERROR, TRY_AGAIN, USER_INPUT, PERMISSION_DENIED, CONFIGURATION, CONNECTIVITY, SERVICE_DOWN, TRUNCATED, CONFLICT, CAPACITY, WARNING<br> |
| __Default__ | USER_INPUT |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.rest.services.basic-auth.login |
|:----------------|:--------|
| __Description__ | Specify the user name used for HTTP basic auth by internal REST servlet<br>Both settings need to be set in order to have basic auth enabled - "com.openexchange.rest.services.basic-auth.login" and "com.openexchange.rest.services.basic-auth.password"<br>Default is empty. Please change!<br> |
| __Default__ | empty |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.rest.services.basic-auth.password |
|:----------------|:--------|
| __Description__ | Specify the password used for HTTP basic auth by internal REST servlet<br>Both settings need to be set in order to have basic auth enabled - "com.openexchange.rest.services.basic-auth.login" and "com.openexchange.rest.services.basic-auth.password"<br>Default is empty. Please change!<br> |
| __Default__ | USER_INPUT |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.preview.thumbnail.blockingWorker |
|:----------------|:--------|
| __Description__ | The backend either delivers a thumbnail when it's available from cache or fails fast while initiating the thumbnail creation/caching in<br>the background. The advantage is that clients aren't blocked and can simply retry later when the thumbnail can be delivered from cache.<br><br>If there is no cache configured for the user/context:<br>  - we either have to generate the thumbnail on the fly which has the potential to block the whole client by occupying the max number of<br>    allowed connections to the domain<br>  - or simply fail fast which leaves the client responsive but without thumbnails<br><br>Default value is "false" to keep the client responsive.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.webdav.recursiveMarshallingLimit |
|:----------------|:--------|
| __Description__ | Defines a hard limit when marshalling elements in WebDAV responses<br>recursively. This applies to all WebDAV responses with a depth >= 1,<br>including CardDAV and CalDAV. This setting is not meant as a quota<br>restriction, instead it is meant to protect against possibly very large<br>responses. Defaults to 250000, a value of 0 or smaller disables the<br>limitation.<br> |
| __Default__ | 250000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.tools.images.transformations.maxSize |
|:----------------|:--------|
| __Description__ | Specifies the max. size (in bytes) for an image that is allowed to be transformed<br>If exceeded image transformation is rejected<br>Default is 10485760 (10MB)<br> |
| __Default__ | 10485760 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.tools.images.transformations.maxResolution |
|:----------------|:--------|
| __Description__ | Specifies the max. resolution (in pixels) for an image that is allowed to be transformed<br>If exceeded image transformation is rejected      <br>Default is 26824090 (~ 6048x4032 (24 megapixels) + 10%)<br> |
| __Default__ | 26824090 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.tools.images.transformations.waitTimeoutSeconds |
|:----------------|:--------|
| __Description__ | Specifies the max. time (in seconds) to await an image transformation computation to complete.<br>If exceeded image transformation is cancelled.<br> |
| __Default__ | 10 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.tools.images.transformations.preferThumbnailThreshold |
|:----------------|:--------|
| __Description__ | Configures up to which factor of the requested image's size an embedded <br>thumbnail may be used, even if this thumbnail's size does not strictly <br>fulfill the requested resolution. <br>For example, if a 200x150 preview is requested, and an embedded thumbnail<br>is available with 160x120, and the factor is configured to 0.8, this<br>thumbnail image will be used - while smaller ones won't. <br>A value of 1 only allows thumbnails if the requested preview size can be <br>fulfilled entirely; a negative value disables usage of embedded thumbnails.<br> |
| __Default__ | 0.8 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
| __Key__ | com.openexchange.ical.updateTimezones |
|:----------------|:--------|
| __Description__ | Configures whether timezone definitions for interpreting iCalendar files <br>should be updated automatically from the internet or not. If disabled, a <br>static list of timezone defintions is used, otherwise, possibly updated <br>timezones are retrieved through tzurl.org upon first usage. <br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Server.html">Server</a> |
| __File__ | Server.properties |

---
