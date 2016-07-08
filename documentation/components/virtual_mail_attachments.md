---
title: Virtual Mail Attachments Connector
---

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [How it works](#how-it-works)
  - [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
  - [Enable/disable](#enable-disable)
  - [Folder paths](#folder-paths)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# How it works

The Open-Xchange Virtual Mail Attachments Connector requires the Dovecot Pro support for attachment view being available for primary mail account. The file attachments of mails are then accessible via a virtual read-only account labeled "My attachments" in Drive module.

The folder hierarchy is as follows:

 - My attachments<br>
(listing all file attachments from a virtual Dovecot folder aggregating all mails of an account)
   - In Inbox<br>
(listing all file attachments from all mails held in INBOX of the primary mail account)
   - In Sent objects<br>
(listing all file attachments from all mails held in standard sent folder of the primary mail account)

## Prerequisites

The ``dovecot-ee-virtual-attachments-plugin`` package needs to be installed. Availability starts with Dovecot Pro 2.2.25.1. Additionally Dovecot needs some according configuration:

 - Configure a virtual folder that aggregates all mails of a user
 - Configure the virtual attachments plugin
 - Set the special IMAP capability `XDOVECOT`

### Configure virtual "all mails" folder

Add the following to your Dovecot configuration:

```
mail_plugins = $mail_plugins virtual
namespace Virtual {
  prefix = Virtual/
  separator = /
  hidden = yes
  list = no
  subscriptions = no
  location = virtual:/etc/dovecot/virtual:INDEX=/var/vmail/%u/virtual
}
```

This makes use of the dovecot virtual folders plugin. A new hidden namespace `Virtual` is created, which will not be contained in IMAP `LIST` responses and not accept subscriptions. However, folders below that namespace can be selected and examined. In our case we define a global configuration for virtual folders below `/etc/dovecot/virtual`, which makes configured folders appear in every users account. However, indexes for such folders need to be created per-mailbox of course, which we expect to be located under `/var/vmail/`.

To create a virtual folder, a file system folder carrying the target name needs to be created below the denoted path. In our case we create a directory `/etc/dovecot/virtual/All`. Folder owner of the `virtual`and `virtual/All` folders needs to be the system user running the `dovecot` process. In our case it's `vmail`.

```
mkdir -p /etc/dovecot/virtual/All
chown -R vmail:vmail /etc/dovecot/virtual
```

Now we need to create the virtual folders configuration. Create a new file `/etc/dovecot/virtual/All/dovecot-virtual` and open it in your favorite editor. E.g. you might decide to include all mails from all folders, but Trash and Spam.

```
*
-Trash
-Trash/*
-Spam
-Spam/*
  all
```

The file can be owned by `root` but must be readable by the user running the `dovecot` process.

As a result every mail account will contain a selectable mailbox `Virtual/All` which pretends to contain all messages from all other mailboxes but Trash and Spam.

### Configure the virtual attachments plugin

Add the following to your Dovecot configuration:

```
mail_plugins = $mail_plugins virtual_attachments
namespace VirtualAttachments {
  prefix = VirtualAttachments/
  separator = /
  hidden = yes
  list = no
  subscriptions = no
  location = attachments:/var/vmail/%u/virtual-attachments

  mailbox INBOX {
    auto = create
  }

  mailbox "INBOX/Sent Items" {
    auto = create
  }

  mailbox Virtual/All {
    auto = create
  }
}
```

Again we create a new hidden namespace. Its indexes will be located under `virtual-attachments` in the mail accounts home directory. For every mailbox that shall get its own attachments folder, an according folder below that namespace needs to be created. We decide to use Dovecots auto-create functionality here, otherwise dedicated `CREATE` commands per mailbox would be necessary. The mailbox names need to match existing real or virtual mailboxes. Those will be mirrored below the `VirtualAttachments` namespace then and contain virtual messages - one per attachment - based on the referenced original mailbox.

### Configure special IMAP capability

Messages in virtual folders and virtual attachment messages reference their according real mails. In Dovecot the original mailbox name and mail UID can be fetched via two special fetch items `X-MAILBOX` and `X-REAL-UID`. OX App Suites fetches these automatically, if the server announces the capability `XDOVECOT`. Therefore the following needs to be added to Dovecots configuration:

```
imap_capability = +XDOVECOT
```

With Dovecot Pro 2.2.25.1 there is also **experimental** support to search mails by attachment file names. This can be announced by another capability and enables this feature in App Suite. You can play around with it by setting the following instead, which announces both capabilities:

```
imap_capability = +SEARCH=X-MIMEPART XDOVECOT
```

### Complete Dovecot configuration example

```
# 2.2.25.1 (f9daebb): /etc/dovecot/dovecot.conf
# Pigeonhole version 0.4.15.rc1 (b9dc09d)
# OS: Linux 3.16.0-4-amd64 x86_64 Debian 8.5 ext3

info_log_path = /var/log/dovecot-info.log
log_path = /var/log/dovecot.log
mail_location = maildir:/var/vmail/%u

protocols = imap lmtp
ssl = no

mail_plugins = virtual virtual_attachments

namespace INBOX {
  prefix = INBOX/
  separator = /
  inbox = yes
  
  mailbox Drafts {
    auto = subscribe
    special_use = \Drafts
  }
  mailbox "Sent Items" {
    auto = subscribe
    special_use = \Sent
  }
  mailbox Spam {
    auto = subscribe
    special_use = \Junk
  }
  mailbox Trash {
    auto = subscribe
    special_use = \Trash
  }
}

namespace Virtual {
  prefix = Virtual/
  separator = /
  hidden = yes
  list = no
  subscriptions = no
  location = virtual:/etc/dovecot/virtual:INDEX=/var/vmail/%u/virtual
}

namespace VirtualAttachments {
  prefix = VirtualAttachments/
  separator = /
  hidden = yes
  list = no
  subscriptions = no
  location = attachments:/var/vmail/%u/virtual-attachments
  
  mailbox INBOX {
    auto = create
  }  
  mailbox "INBOX/Sent Items" {
    auto = create
  }  
  mailbox Virtual/All {
    auto = create
  }
}

imap_capability = +SEARCH=X-MIMEPART XDOVECOT

passdb {
  args = scheme=SHA1 /etc/dovecot/passwd
  driver = passwd-file
}

userdb {
  args = uid=vmail gid=vmail home=/var/vmail/%u
  driver = static
}
```

# Installation
Deploying the Open-Xchange Virtual Mail Attachments Connector simply requires to install the `open-xchange-file-storage-mail` package.

# Configuration
After the package is deployed, an administrator is able to configure the Open-Xchange Virtual Mail Attachments Connector through `/opt/open-xchange/etc/filestorage-maildrive.properties` file. All configuration options are config-cascade capable.

## Enable/disable
Enabling or disabling the Open-Xchange Virtual Mail Attachments Connector is managed by property ``com.openexchange.file.storage.mail.enabled``

## Folder paths
As last step, the paths to the folders need to be set:

 - ``com.openexchange.file.storage.mail.fullNameAll`` Specifies the path to the virtual folder containing all messages, which is used to list the file attachment of all messages in primary account
 - ``com.openexchange.file.storage.mail.fullNameReceived`` Specifies the path to the INBOX folder, which is used to list the file attachment of received messages in primary account
 - ``com.openexchange.file.storage.mail.fullNameSent`` Specifies the path to the standard sent folder, which is used to list the file attachment of sent messages in primary account

## Example

According to the above Dovecot configuration the correct values would be:


```
com.openexchange.file.storage.mail.enabled=true
com.openexchange.file.storage.mail.fullNameAll=VirtualAttachments/Virtual/All
com.openexchange.file.storage.mail.fullNameReceived=VirtualAttachments/INBOX
com.openexchange.file.storage.mail.fullNameSent=VirtualAttachments/Sent Items
```
