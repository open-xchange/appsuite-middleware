---
title: Internet Free Busy Servlet
icon: fa-business-time
tags: API, Calendar, FreeBusy
---

This article will describe how use the internet free busy servlet.

# Overview
Clients like Microsoft Outlook are able to discover the availability of a user or resource by querying a specific endpoint on a server. This endpoint is the free busy servlet. The free busy data of a user or resource can be requested via a HTTP request and is returned in iCalendar format.

The servlet is disabled by default and must be enabled by the administrator. The free busy data for a context is published only when activated in the configuration.

The following sections describe how the servlet can be configured and how it can be used with Microsoft Outlook or other clients.

# Configuration

To use the free busy servlet configurations must be made. Furthermore the maximum time range, that can be requested, is configurable. This section provides an overview of those config parameters.

First of all the servlet must be enabled generally, otherwise it won't be registered during server startup:

    com.openexchange.calendar.enableInternetFreeBusy=true

After changing the property a restart is necessary.

To get free busy data for a user in a context, the property for publishing must be enabled explicitly. This can either be done globally, or using the config-cascade down to the "context" level:

    com.openexchange.calendar.publishInternetFreeBusy=true

Furthermore it is possible to configure the maximum time range that is allowed to be requested. By default this maximum time range is 12 weeks into the past and 26 weeks into the future. The time range is given in weeks.

    com.openexchange.calendar.internetFreeBusyMaximumTimerangePast=13
    com.openexchange.calendar.internetFreeBusyMaximumTimerangeFuture=27

# Requesting free busy data from servlet
This section provides information about how to use the servlet.

## Parameters
The request to the servlet has required and optional parameters. The context id, the user name and the server name are necessary for requesting free busy data. Optionally, the time range to be queried can be specified. Set parameter simple to true, if the resulting iCalendar data should not contain the free busy type and free information (necessary to get readable data for Microsoft Outlook).

Required parameters:

* **contextId** (Integer): The context id of the context in which the requested user is located.

* **userName** (String): The name of the user. Typically the local part of the email address.

* **server** (String): The name of the server. Typically the domain part of the email address.


Optional parameters:

* **weeksIntoPast** (Integer): The requested time range into the past in weeks.
If this value is greater than the configured maximum (see property above), the free busy times are only requested to configured maximum.
Default value is 1 week into the past.

* **weeksIntoFuture** (Integer): The requested time range into the future in weeks.
If this value is greater than the configured maximum (see property above), the free busy times are only requested to configured maximum.
Default value is 4 week into the future.

* **simple** (Boolean): true, if the VFREEBUSY data should not contain free busy type and free information, false otherwise.
Default value is false.


For further information see OX REST API documentation: <https://documentation.open-xchange.com/components/middleware/rest/latest/index.html>.

## URI
The servlet can be reached via the following path:

	http://[server]:[port]/servlet/webdav.freebusy?

[server] and [port] are placeholder and must be replaced by the information in the App Suite installation. The parameters are concatenated with '&' and appended on the URI.

## Example for Microsoft Outlook
After activating the servlet for a speicifc context as outlined above, the following URI can be configured in the free/busy options of Microsoft Outlook, the free busy data of users and resources of the specified context can be requested. [server], [port] and [cid] are placeholder and must be replaced by the information from App Suite installation and the context id. Outlook substitutes %NAME% and %SERVER% in the template with the email address, splitted at the '@' character.

	http://[server]:[port]/servlet/webdav.freebusy?contextId=[cid]&userName=%NAME%&server=%SERVER%&simple=true
For further information about how to request free busy data in Microsoft Outlook see: <https://support.microsoft.com/en-us/help/291621/how-to-use-the-internet-free-busy-feature-in-outlook>.
