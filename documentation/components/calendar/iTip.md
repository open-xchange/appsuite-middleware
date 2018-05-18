---
title: iCalendar Transport-Independent Interoperability Protocol (iTIP)
---


# General

## What's this about
This article will describe how the iTIP protocol was realized, which parts of the protocol are missing and where the implementation differs from the standard.

## iTIP
iTIP is a standard. It defines "how calendaring systems use iCalendar [RFC5545][1] objects to interoperate with other calendaring systems"[1][2]. So basically it's about how to organize and maintain calendar events across multiple systems.

### Transport
The RFC does explicit not mention a specific transport mechanism for iTIP. The provider implementing the RFC is free to choose. Open-Xchange uses mail to realize the iTIP protocol, efficiently implementing [iMIP][3] too.

### Handled components
The iTIP protocol allows to synchronize following components

+ VEVENT
+ VFREEBUSY
+ VTODO
+ VJOURNAL

Currently the Open-Xchange Server will only handle the *VEVENT* component.

# Methods

## PUBLISH
The [PUBLISH][A] method isn't treated in any special way. A *VENENT* posted over PUBLISH is handled the same way as a REQUEST.

## REQUEST
With the [REQUEST][B] method initial invites, updates, rescheduling and more is realized. Many of the functionality mentioned in the RFC is build. 
Not supported is [delegating an event to another CU][B1] or [forwarding to an uninvited CU][B2].

## REPLY
A [REPLY][C] normally should only transmit the replying [attendee][C1] status. Nevertheless some calendaring systems edit other properties too. To avoid, e.g. a changed event title, the server will silently drop all changed properties.

However changed parameters on the attendee object itself will be taken over. As long as the URI of the attendee doesn't change all other parameters can be edited.


## ADD
The [ADD][D] method is implemented as recommended by the RFC.

## CANCEL
The [CANCEL][E] method is implemented as recommended by the RFC.

## REFRESH
The [REFRESH][F] method is implemented as recommended by the RFC.

## COUNTER
The [COUNTER][G] method is implemented as recommended by the RFC.

## DECLINECOUNTER
The [DECLINECOUNTER][H] method is implemented as recommended by the RFC.



# References / further reading
1. [RFC 5545][1] iCalendar
2. [RFC 5546][2] iTIP
3. [RFC 6047][3] iMIP



[//]: # (Reference links - won't be shown in converted file)
[//]: # (Inline references)
[A]: https://tools.ietf.org/html/rfc5546#section-3.2.1
[B]: https://tools.ietf.org/html/rfc5546#section-3.2.2
[B1]:https://tools.ietf.org/html/rfc5546#section-3.2.2.3
[B2]:https://tools.ietf.org/html/rfc5546#section-3.2.2.6
[C]: https://tools.ietf.org/html/rfc5546#section-3.2.3
[C1]: https://tools.ietf.org/html/rfc5545#section-3.8.4.1
[D]: https://tools.ietf.org/html/rfc5546#section-3.2.4
[E]: https://tools.ietf.org/html/rfc5546#section-3.2.5
[F]: https://tools.ietf.org/html/rfc5546#section-3.2.6
[G]: https://tools.ietf.org/html/rfc5546#section-3.2.7
[H]: https://tools.ietf.org/html/rfc5546#section-3.2.7

[//]: # (Explicit references)
[1]: https://tools.ietf.org/html/rfc5545
[2]: https://tools.ietf.org/html/rfc5546
[3]: https://tools.ietf.org/html/rfc6047