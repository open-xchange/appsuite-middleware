---
title: Primary Address Header
---

# Introduction
With v7.10.0 the Open-Xchange Server offers possibility to add a self-defined header carrying the primary mail address on outgoing mails. This is the primary address header.
The header can be used e.g. for user based anti-virus solutions on MTA level. The header is applied only to internal mail accounts.

The header always carries the primary mail address even if mails are send with an alias. This in mind, the feature is disabled per default due privacy concerns.  

# Configuration
Per default the primary address header isn't set and therefore the feature can be considered deactivated. No header is added to outgoing mails.

If set the primary address header needs to observe some rules. The header needs to be a string whose characters are all 7-bit, non-control, non-whitespace ASCII characters.
If not an error is logged each time a mail is send via internal mail accounts.

For further information on the configuration please visit the [configuration section](/components/middleware/config{{ site.baseurl }}/index.html#mode=features&feature=SMTP).

## Example

	com.openexchange.smtp.setPrimaryAddressHeader = X-Originating-Sender
