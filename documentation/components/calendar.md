---
title: Calendar 
icon: fa-calendar
---

Release 7.10.0 brings a major renovation of the Calendar module - the internal calendaring and scheduling logic is rewritten from scratch to be more aligned with the commonly used iCalendar model in the future (and move a bit away from MS Exchange/Outlook, accordingly). 

The original calendar implementation at Open-Xchange served well for the last two decades. Various clients have come and gone during that time and were connected via different interfaces, with each of them having their own characteristics and API dialects to deal with. The commonly seen standard we needed to adopt and emulate has always been the calendaring and scheduling features of Microsoft Exchange, with its data- and collaboration model on the one hand, and the widely used Outlook client on the other hand. Nowadays, the open iCalendar format and related protocols like CalDAV or iMIP have evolved and are widely implemented in many popular clients and servers, with a usually acceptable interoperability. For future releases, Open-Xchange will also try and shift the focus away from the Microsoft world towards those open standards, especially to improve the support and interoperability for 3rd party clients like eM Client or the OX Sync App for Android. 

While our server does have a CalDAV interface for some time now, there are still some conceptual differences between our internal data model (that is mostly based on MS Exchange / Outlook) and iCalendar via CalDAV. There are dozens of workarounds in place to mitigate the discrepancies, and in many aspects, we cannot do more than converting back and forth in a lossy "best effort" way. We only support a limited set of recurrence rules, only have one "reminder minutes" property to store a user's alarms for an event, we do not honor the organizer and attendee roles during scheduling, we discard everything else we cannot store in the database, and so on, just to name a few. 

In order to accomplish our goals of having a more standards compliant calendar and scheduling server, some decent changes on the data model and the calendaring logic are required. However, the existing architecture and grown codebase of the existing calendar implementation did not seem to be a suitable basis for the required amount of necessary refactorings, given the fact that it grew and evolved for so many years now, went through the hands of various developers, is full of workarounds for Outlook/USM quirks and is not really maintainable or extendable anymore. So after all, we decided to take the chance and begun a major rewrite of the calendar module (aka Calendar v2 aka Chronos). 

For further details, please choose a subtopic on the left.
