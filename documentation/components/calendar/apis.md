---
title: APIs 
---

Calendar features are exposed to clients via different APIs, most prominently our HTTP API. 

# HTTP API

The new calendar implementation comes with a whole new *module* in the HTTP API: ``chronos``. However, the previous API (module ``calendar``) is still available for legacy clients like OX6 or USM. Internally, calls to the "old" module are converted appropriately, then routed to the new services, and the results are converted back to the previous format. In particular, the following parts of the HTTP API are affected by the new calendar implementation: 

## Legacy module ``calendar``

All actions in the ``calendar`` module will continue to work in v7.10.0, however, the functionality is limited to the previous data model. For example, only one reminder can be accessed using the legacy API, and no calendars from external calendar sources are available. Therefore, the whole ``calendar`` module in the HTTP API should be considered as **deprecated** with the new release.

## New module ``chronos``

To interact with the new calendar using the new data model, a new JSON module named ``chronos`` has been introduced. See the API documentation for details. 

## Attachments & Reminder

The functionality to work with attachments and reminders has been integrated into the module ``chronos`` (see the new API documentation for details). 

For legacy clients, the previous requests in the modules ``attachments`` and ``reminder`` are still in place, however, possibly with a reduced functionality, especially towards the handling of alarms. So, all *appointment*-related functionality within the ``attachments`` and ``reminder`` module is deprecated with v7.10.0.

## Folders

For the ``folders`` module, a new content type has been introduced in order to provide backwards-compatibility on the one hand, and support for the newly introduced provider/account architecture on the other hand. Doing so, any existing requests to the API will return the same responses as before (i.e. list the same folders with the same identifiers). All "new" folders will be returned when requesting folders of the content type ``event``, which includes both the previously used calendar *groupware* folders, as well as calendar folders representing external accounts. Differentiation is performed based on *composite* identifiers, which carry the information about the underlying calendar account.

The calendar folders are now driven by a dedicated folder storage, and there's no folder hierarchy anymore, i.e. there are no subfolders. 
 
## Import/Export

Calendar-related actions in the import/export module of the HTTP API will continue to work transparently. For the import action, the returned folder identifiers in the responses will be the ones suitable for the targeted folder (i.e. composite identifiers when importing into a folder with content type ``event``, and relative numerical identifiers when importing to the previously used folders).

## iTIP

The functionality to process incoming iTIP-messages in the new data model is exposed via the newly introduced module ``chronos/itip``. See the new API documentation for details.

The previous endpoint for legacy clients is still available (module ``calendar/itip``), with appropriate conversion routines for the new stack.

## Find

The calendar-related functionality provided within the ``find`` module has been re-implemented using the new calendar stack. The format of the resulting documents has been adjusted to be compatible with the new data model, so that "event" results are returned now in favor of the previous "appointment" modules. 

