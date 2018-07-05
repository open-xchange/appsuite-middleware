---
title: Ignore deleted Mails
---

With 7.10.0 it is possible to ignore mails marked as deleted for the primary mail account. To activate this mode it is only necessary to set the property 
`com.openexchange.imap.ignoreDeleted` to `true`. If this property is set, then mails marked as deleted are removed from mail requests if not specifically requested. 
Additionally the unread counter also ignores those mails. The mode is advertised to the clients via the jslob entry `io.ox/mail//features/ignoreDeleted`. 

**Important**: Please note that the middleware needs to use a select + search IMAP command instead of a single status command to determine the unread count. Naturally this takes significantly longer
and causes more load on the IMAP server. Therefore we advise to only activate this mode in case the IMAP server is able to handle it.


