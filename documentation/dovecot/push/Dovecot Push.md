---
title: Dovecot Push
---

This article describes how to configure the Dovecot Push feature, which allows e.g. pushing events to mobile clients/apps.

The following picture should demonstrate how the overall communication flow between a mobile app, Open-Xchange Middleware, and the Dovecot Push plug-in takes place.

![Dovecot Push flow](mail_push_flow.png)

# Prerequisites

- At least Dovecot v2.2.19
- Either [METADATA (RFC 5464)](https://tools.ietf.org/html/rfc5464%7CIMAP) support or enabling the Middleware's connector for the Dovecot DoveAdm REST interface
- Installation of Middleware's `open-xchange-push-dovecot` package (prior to v7.10.0 the `open-xchange-rest` is required, too)
- Installation of the Dovecot `http-notify` plug-in

## Enabling METADATA support

Dovecot Push plug-in requires METADATA support on Dovecot side, but it should only be enabled for OX IMAP sessions and not for any other IMAP clients directly. Enabling can be done with e.g. the remote directive `remote 1.2.3.0/24 { imap_metadata = yes }` and specifying the IP ranges of OX.

Dovecot supports the [METADATA (RFC 5464)](https://tools.ietf.org/html/rfc5464%7CIMAP), which allows per-mailbox, per-user data to be stored and accessed via IMAP commands.

To activate metadata storage, a dictionary needs to be configured in the Dovecot configuration using the mail_attribute_dict option.

To activate the IMAP METADATA commands, the `imap_metadata` and `mail_attribute_dict` options need to be activated.

Example:

```
# Store METADATA information within user's Maildir directory
mail_attribute_dict = file:%h/Maildir/dovecot-attributes

remote 1.2.3.0/24  {
  protocol imap {
    imap_metadata = yes
  }
}
```

## Configuring connector for the Dovecot DoveAdm REST interface

As alternative to the [METADATA (RFC 5464)](https://tools.ietf.org/html/rfc5464%7CIMAP) extension, the DoveAdm REST interface can be used to register listeners on Dovecot side.

For enabling the connector for the Dovecot DoveAdm REST interface, the following configuration options need to be set (for instance in file `/opt/open-xchange/etc/doveadm.properties`):

### Enabling the connector

`com.openexchange.dovecot.doveadm.enabled` is required to be set to `true`

### Connection settings

Typically the DoveAdm REST end-point is accessible on port 8080 via API path `/doveadm/v1`.

- `com.openexchange.dovecot.doveadm.endpoints` needs to point to Dovecot's DoveAdm REST end-point; e.g. `com.openexchange.dovecot.doveadm.endpoints=http://imap.example.com:8080/doveadm/v1`. However, this setting accepts multiple commo-separated end-points, in which case end-poins are used in a round-robin fashion when an end-point becomes (temporarily) unavailable (and thus is black-listed).

Further connection settings

- `com.openexchange.dovecot.doveadm.endpoints.totalConnections` specifies the total number ofconnections held in connector's HTTP connection pool. Default is `100`.
- `com.openexchange.dovecot.doveadm.endpoints.maxConnectionsPerRoute ` specifies the number of connections per route held in HTTP connection pool; or less than/equal to `0` (zero) for auto-determining. Default is `0`.
- `com.openexchange.dovecot.doveadm.endpoints.readTimeout` defines connector's read timeout in milliseconds for HTTP requests issued against the Dovecot's DoveAdm REST end-point. Default is `10000`
- `com.openexchange.dovecot.doveadm.endpoints. connectTimeout ` defines connector's connect timeout in milliseconds when establishing a HTTP connection to the Dovecot's DoveAdm REST end-point. Default is `3000`
- `com.openexchange.dovecot.doveadm.endpoints.checkInterval` gives the time interval in milliseconds when to check if a previously black-listed end-point is re-available again. Default is `60000`

### API secret

`com.openexchange.dovecot.doveadm.apiSecret` accepts the API's secret string, which is supposed to be transferred to the DoveAdm REST end-point through the `Authorization` HTTP header for authenticating the request:

```
"X-Dovecot-API " + <base64-encoded-secret>
```

# Configuration of Dovecot "http-notify" plug-in

To use push notifications, both the "notify" and the "push_notification" plugins need to be activated. For LMTP delivery, this is required:

```
protocol lmtp {
  mail_plugins = $mail_plugins notify push_notification
}
```

If you also want push notifications to work for LDA-based delivery, you would need additional configuration:

```
protocol lda {
  mail_plugins = $mail_plugins notify push_notification
}
```

The HTTP end-point (URL + authentication information) to use is configured in the Dovecot configuration file. The appropriate configuration options will contain the HTTP URL denoting the end-point to connect to as well as the authentication information for Basic Authentication as configured by properties `com.openexchange.rest.services.basic-auth.login` and `com.openexchange.rest.services.basic-auth.password`. The URL to configure in Dovecot configuration follows this pattern.

```
<http|https> + "://" + <login> + ":" + <password> + "@" + <host> + ":" + <port> + "/preliminary/http-notify/v1/notify"
```

Please note that SSL endpoints are only supported from Dovecot >= 2.3.

Example:

```
plugin {
 push_notification_driver = ox:url=http://login:pass@node1.domain.tld:8009/preliminary/http-notify/v1/notify
}
```

Furthermore, it is also possible to specify more than one HTTP end-point to connect to if a new message delivery occurs. Thus the configuration section mentioned above may be extended by additional `push_notification_driver` entries; e.g. `push_notification_driver2`, `push_notification_driver3`, etc.

Please note that the path `"/preliminary/http-notify/v1/notify"` denotes the internal REST API of the Open-Xchange Middleware, which must not be publicly accessible. The administrator can decide whether to add that path to the Apache configuration (see also [AppSuite:Apache_Configuration](http://oxpedia.org/wiki/index.php?title=AppSuite:Apache_Configuration) and [AppSuite:Grizzly](http://oxpedia.org/wiki/index.php?title=AppSuite:Grizzly)) through a `Location`/`ProxyPass` directive:

```
<Location /preliminary>
    Order Deny,Allow
    Deny from all
    # Only allow access from servers within the network. Do not expose this
    # location outside of your network. In case you use a load balancing service in front
    # of your Apache infrastructure you should make sure that access to /preliminary will
    # be blocked from the internet / outside clients. Examples:
    # Allow from 192.168.0.1
    # Allow from 192.168.1.1 192.168.1.2
    # Allow from 192.168.0.
    ProxyPass /preliminary balancer://oxcluster/preliminary
</Location>
```

In case the `"user="` sent by OX in the `push_notification_driver` URL data does not match the IMAP login of a user, Dovecot ignores it. This can be overridden by defining `"user_from_metadata"` in the `push_notification_driver` URL, e.g.

```
push_notification_driver = ox:url=http://example.com/ user_from_metadata
```
