---
title: Google Contacts
classes: toc
icon: fa-google
---

# Required Permissions

The following Google APIs are required to enable contact synchronisation:

  * Contacts API
  * Google Cloud SQL
  * Google Cloud Storage
  * Google Cloud Storage JSON API
  * [BigQuery API](https://developers.google.com/identity/protocols/googlescopes#bigqueryv2)

The APIs can be enabled via the [Google Developers Console](https://console.developers.google.com/).

# Configuration

Note that the contact synchronisation will NOT happen automatically every time a new contact is added to the third-party provider's address book. A full sync will happen once the user has created her account, and periodically once per day. The periodic update can be enabled or disabled via the `com.openexchange.subscribe.autorun` server property.

Also note that this is an one-way sync, i.e. from the third-party provider towards the AppSuite and NOT vice versa.
