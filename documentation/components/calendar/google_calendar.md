---
title: Google Calendar
classes: toc
icon: fa-google
---

# Pre-Steps

Before proceceeding, please make sure that you have [OAuth 2.0]({{ site.baseurl }}/middleware/components/oauth.html) functionality installed and properly configured on your middleware node.

# Required Permissions

The following Google APIs are required to enable calendar synchronisation:

  * Calendar API
  * Google Cloud SQL
  * Google Cloud Storage
  * Google Cloud Storage JSON API
  * [BigQuery API](https://developers.google.com/identity/protocols/googlescopes#bigqueryv2)

The APIs can be enabled via the [Google Developers Console](https://console.developers.google.com/).
