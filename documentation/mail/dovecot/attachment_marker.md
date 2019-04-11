---
title: Attachment marker
---

# Introduction

This feature is desired to have a fast and reliable way for the visual indication if a mail has one or more attachments. This feature is disabled per default and requires explicit configuration on the Open-Xchange middleware and the underlying Dovecot IMAP server (see below).

This feature is available for the primary mail account only!

## Detailed description

After the feature has been enabled successfully (for Dovecot) all incoming mails will be indexed by the IMAP server which means user flags (`$HasAttachment`/`$HasNoAttachment`) will be added to the mails.  
**Note:** Dovecot only indexes mails after the feature has been enabled. There will be no re-indexing of existing mails.

Based on the Dovecot configuration there might be additional mails that will be indexed for instance mails stored as draft (have a look at Dovecots dedicated documentation).
A new search facet and sort option will be available within the UI (announced via folder capability named `ATTACHMENT_MARKER`) after the feature is enabled for the middleware.  
**Hint:** search and sort will only consider indexed mails. Older not indexed mails will neither be found nor sorted correctly.
  
To guarantee correct indication of the attachment marker the previously middleware implementation will be used as a fallback if no attachment marker user flag is available.

# Dovecot configuration

Following you can find an exemplary one line configuration for Dovecot. Please have a look at Dovecots dedicated documentation for more configuration options of this feature.

```
mail_attachment_detection_options=add-flags-on-save
```

# Middleware configuration

To generally enable this feature for the middleware you have to add/set the property as follows:

```
com.openexchange.imap.attachmentMarker.enabled=true
```

You can change this setting via config cascade on an user/context/contextSet base.
