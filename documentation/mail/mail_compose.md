---
title: Mail Compose
icon: fa-envelope-open
tags: Mail, Configuration, Installation
---

# New mail compose
Starting with v7.10.2 the App Suite Middleware ships with a refactored mail compose API. That API opens a new composition space whenever the user starts to write a new mail.

Such a composition space represents the current state of the mail that is supposed to be composed such as recipients, subject, content, etc.

Starting with 7.10.5, two different implementations of the API are available:

1. The original one, based on a combination of Database and Filestore
2. One based on plain draft email messages stored in a users' "Drafts" mail folder
