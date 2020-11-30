---
title: Mail Compose
icon: fa-envelope-open
tags: Mail, Configuration, Installation
---

# New mail compose

Starting with v7.10.2 the App Suite Middleware ships with a refactored mail compose API. That API opens a new composition space whenever the user starts to write a new mail.

Such a composition space represents the current state of the mail that is supposed to be composed such as recipients, subject, content, etc.

Starting with 7.10.5, two different implementations of the API are available:

1. (New) Based on plain draft email messages stored in users "Drafts" mail folders
2. (Old) Based on a combination of Database and Filestore

The new approach is subject to fully replace the former database and filestore driven one. The former approach is considered as **deprecated** starting with 7.10.5 and might be removed in a future release.

