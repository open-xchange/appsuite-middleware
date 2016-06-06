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

The Open-Xchange Virtual Mail Attachments Connector requires the Dovecot Pro support for attachment view being available for primary mail account. The file attachments of mails are then accessible via a virtual read-only account labeled "Mail Drive" in Drive module.

The root folder consists of three sub-folders:

 - All attachments<br>
(listing all file attachments from all mails in primary mail account)
 - Received attachment<br>
(listing all file attachments from all mails held in INBOX of the primary mail account)
 - Sent attachment<br>
(listing all file attachments from all mails held in standard sent folder of the primary mail account)

## Prerequisites

 - ``dovecot-ee-virtual-attachments-plugin`` installed on Dovecot Pro
 - Adjusting Dovecot configuration file ``/etc/dovecot/dovecot.conf``
  - Add ``virtual`` namespace:
 
 ```
     # mail
     ...
     namespace virtual {
         prefix = virtual/
         separator = /
         hidden = no
         list = yes
         subscriptions = no
         location = virtual:/etc/dovecot/virtual:INDEX=/var/vmail/%u/virtual
     
         mailbox all {
             special_use = \All
         }
     }
     ...
 ```
 
  - Add ``VirtualAttachments`` namespace:
 
 ```
     # mail
     ...
     namespace VirtualAttachments {
         prefix = VirtualAttachments/
         separator = /
         hidden = no
         list = yes
         subscriptions = no
         location = attachments:/var/vmail/%u/virtual-attachments
     }
     ...
 ```
 
  - Enabling plug-ins:
 
 ```
     ...
     mail_plugins = virtual virtual_attachments
     ...
 ```
 
  - Special ``XDOVECOT`` capability:

 ```
     # general
     ...
     imap_capability = +XDOVECOT
     ... 
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

Example:

```
com.openexchange.file.storage.mail.enabled=true
com.openexchange.file.storage.mail.fullNameAll=VirtualAttachments/virtual/All
com.openexchange.file.storage.mail.fullNameReceived=VirtualAttachments/INBOX
com.openexchange.file.storage.mail.fullNameSent=VirtualAttachments/Sent
```
