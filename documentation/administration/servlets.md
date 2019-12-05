---
title: Servlets
icon: fa-puzzle-piece
tags: Administration, Security, Configuration, Servlet
---

This pages contains an overview about which servlets are registered by the middleware core from which packages, and what their purpose is. Also there is an recommendation to whom to expose the servlets to.
Please note that this is not an complete overview about about all registered servlets. Additional components like e.g. documents will register their own serlvets. Please read the corresponding documentation, too.

# Overview


| Servlet             | Path                        | Package                           | Expose to    | Property to configure                     |
|---------------------|:---------------------------:|:---------------------------------:|:------------:|------------------------------------------:|
| DAV                 | /servlet/dav                | open-xchange-dav                  | Clients      | [com.openexchange.dav.prefixPath](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.dav.prefixPath)   |
| Dispatcher          | /ajax                       | open-xchange-core                 | Clients      | [com.openexchange.dispatcher.prefixPath](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.dispatcher.prefix)   |
| EAS                 | /serlvet/mobileconfig       | open-xchange-eas-provisioning     | Clients      |                                           |
| Guard               | /oxguard                    | open-xchange-guard                | Clients      |                                           |
| Infostore           | /infostore, /files, /drive, /servlet/webdav.infostore, /servlet/webdav.drive"   | open-xchange-core     | Clients    |     |
| Jolokia             | /monitoring/jolokia         | open-xchange-core                 | Admins       |                                           |
| Proxy               | /servlet/proxy              | open-xchange-core                 | Clients      |                                           |
| REST                | /                           | open-xchange-core                 | See below    |                                           |
| SOAP CXF            | /webservices, /servlet/axis2/services   | open-xchange-soap-cxf   | Admins     | [com.openexchange.soap.cxf.baseAddress](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.soap.cxf.baseAddress)   |
| websockets          | /socket.io                  | open-xchange-core                 | Clients      |                                           |


# Details

## DAV

Enables the DAV related functionality for the MW, in particular support for CalDAV and CardDAV. See [CalDAV and CardDAV](https://documentation.open-xchange.com{{ site.baseurl }}/middleware/miscellaneous/caldav_carddav.html)

## Dispatcher

The dispatcher framework handles any client request to the middleware. 
Note: Per default the dispatcher prefix is ``/ajax``. However the UI will query the middleware via ``/appsuite/api``. Therefore the used proxy needs to redirect those calls to ``/ajax``. For example the apache configuration should contain the lines

```
  ProxyPass /ajax balancer://oxcluster/ajax
  ProxyPass /appsuite/api balancer://oxcluster/ajax
```

## EAS

The endpoint for EAS clients to request their configuration from.

## Guard

The Open Xchange Guard feature. As this feature provides additional API for clients, it registers new servlets, too.
Note: The serlvelts registers by Guard will begin with ``/oxguard``. However clients may query ``/appsuite/api/oxguard``. Therefore likewise for the dispatcher, an proxy before the middleware should redirect calls. For example

```
  ProxyPass /appsuite/api/oxguard balancer://oxcluster/oxguard
```
## Infostore

The infostore aka drive module for the middleware. Provides files for clients.

## Jolokia

Jolokia is an HTTP/JSON bridge for remote JMX access. Designed to be used by administrators

## Proxy

A module for the middleware in which it will act as a proxy.

## REST

REST API endpoints created by the middleware have multiple different path. Therefore a general statement is not possible. The list below will show all registered REST endpoints and outline by whom the APIs are accessed.


| Feature                   | Path                                                                  | Package                     | Accessible by      |
|---------------------------|:---------------------------------------------------------------------:|:---------------------------:|-------------------:|
| Session closer            | /admin/v1/close-sessions                                              | open-xchange-core           | Master Admins      |
| Password change history   | /admin/v1/contexts/{context-id}/users/{user-id}/passwd-changes        | open-xchange-admin          | [Individual](https://documentation.open-xchange.com{{ site.baseurl }}/middleware/security_and_encryption/password_change_history.html)   |
| Multifactor               | /admin/v1/contexts/{context-id}/users/{user-id}/multifactor/devices   | open-xchange-multifactor    | Basic Auth login   |
| Adverstiment              | /advertisement/v1                                                     | open-xchange-advertisment   | Basic Auth login   |
| MW Healtch checks         | /health                                                               | open-xchange-core           | [Health check login](http://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.health.username)   |
| Guard guest share link    | /preliminary/guard/guest/v1                                           | open-xchange-guard          | Basic Auth login   |
| Dovecot Push              | /preliminary/http-notify/v1/                                          | open-xchange-push-dovecot   | Basic Auth login   |
| Authentication service    | /preliminary/adminproc/v1                                             | open-xchange-rest           | Basic Auth login   |
| Capability service        | /preliminary/capabilities/v1/                                         | open-xchange-rest           | Basic Auth login   |
| ConfigView service        | /preliminary/configuration/v1/                                        | open-xchange-rest           | Basic Auth login   |
| HTML sanitizer service    | /preliminary/htmlproc/v1/                                             | open-xchange-rest           | Basic Auth login   |
| SessionD service          | /preliminary/session/v1/                                              | open-xchange-rest           | Basic Auth login   |
| Mail resolver             | /preliminary/utilities/mailResolver/v1                                | open-xchange-rest           | Basic Auth login   |
| User Feedback             | /userfeedback/v1                                                      | open-xchange-userfeedback   | Basic Auth login   |

Note: The "Basic Auth login" can be adjusted via [com.openexchange.rest.services.basic-auth.login](https://documentation.open-xchange.com/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.rest.services.basic-auth.login)

### Further reading

The REST API is described [here](https://documentation.open-xchange.com/components/middleware/rest{{ site.baseurl }}/index.html)


## SOAP CFX

SOAP admin interfaces.

## Websockets

The endpoint for creating and using websockets between the UI and the middleware.