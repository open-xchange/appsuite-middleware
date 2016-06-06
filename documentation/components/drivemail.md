---
title: Drive Mail
---

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [How it works](#how-it-works)
- [Installation](#installation)
- [Configuration](#configuration)
  - [Enabling](#enabling)
  - [Display name](#display-name)
  - [Threshold](#threshold)
  - [Root folder name](#root-folder-name)
  - [External recipient locale](#external-recipient-locale)
  - [Share attributes](#share-attributes)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# How it works
Starting with v7.8.2 the Open-Xchange Server supports to send one or more non-inline file attachments not as physically attached files, but as a mail containing a share link acutally providing access to the files.

Whenever such a mail is sent, an according folder is created in Drive module located under a special folder named _"My shared mail attachments"_. The name of the new folder is aligned to the mail's subject. All file attachments that are supposed to be available via the mail are put into that folder and a share link is created for that folder. That share link (along-side with other information) is prepended to the mail's text content.

# Installation
This feature is included in ``open-xchange-core`` package. Thus, no additional packages are required being installed.

# Configuration
An administrator is able to configure this feature through `/opt/open-xchange/etc/mail-compose.properties` file. All configuration options are config-cascade capable.

## Enabling
The feature is enabled via ``com.openexchange.mail.compose.share.enabled`` property, which defaults to ``true``.

If enabled and user holds sufficient capabilities (_"infostore"_ and _"share_links"_), clients are allowed to signal that a mail is supposed to be sent containing a share link, rather than actual file attachments, through enhancing the JSON request body of the ``/mail?action=new`` call:

```
 {
  "from", ["Jane Doe", "jane.doe@somewhere.com"],
  ...
  "share_attachments": {
   "enable": true,
   "expiry_date": 1465219133000, <-- Specifies when share link is about to expire
   "password": "secret"          <-- Specifies an optional password access the share link
   "autodelete": true            <-- Specifies whether share expiration should also delete shared files
  }
 }
```

## Display name

It is possible to change the display name of that feature according to customer needs through ``com.openexchange.mail.compose.share.name`` option. Default is _Drive Mail_.

## Threshold

Since file attachments are not physically attached, but stored in Drive of sending user, a user can be forced to send a share link through specifying property ``com.openexchange.mail.compose.share.threshold``. That option accepts a threshold, which is the total number of bytes of all file attachments that are allowed to be sent physically attached. Once exceeded the file attachments are made accessible via a share link.

## Root folder name
The ``com.openexchange.mail.compose.share.folderName`` setting allows to specify the name of root folder under which message-specific sub-folders are created. Default value is ``i18n-defined`` which reflects to choose the translated string of the _"My shared mail attachments"_ string literal.

However, it is possible to hard-code the name of the folder with respect to a user's locale.

 - ``com.openexchange.mail.compose.share.folderName=My shares`` sets the folder name globally to ``My shares``.
 - ``com.openexchange.mail.compose.share.folderName.de_DE=Meine Anh\u00e4nge`` sets the folder name for the de_DE locale.

When looking-up folder name, as first step the property name ``"com.openexchange.mail.compose.share.folderName" + <user-locale>`` is checked. If absent, look-up falls-back to ``"com.openexchange.mail.compose.share.folderName"``.

## External recipient locale
The option ``com.openexchange.mail.compose.share.externalRecipientsLocale`` specifies what locale to use when composing a mail containing share information for ane xternal recipient.

This option either accepts special value ``user-defined`` (that is to choose sending user's locale) or a locale identifier according to RFC 2798 and 2068; such as ``en_US``.

## Share attributes
By default a user can optionally specify whether a share link has an expiry date and if (provded a expiry date is set) the associated folder is automatically deleted from Drive module if share link is expired.

An administrator can enforce, whether an expiry date and an accompanying auto-delete flag is required.

 - ``com.openexchange.mail.compose.share.requiredExpiration`` (default id ``false``). If set to ``true``, the user is required to specifiy an expiry date for the created share link. Otherwise sending the mail is rejected.
 - ``com.openexchange.mail.compose.share.forceAutoDelete`` (default id ``false``). If set to ``true``, the files/folder associated with the share will be automatically deletes once the share link expires.
