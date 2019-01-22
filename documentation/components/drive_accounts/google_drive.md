---
title: Google Drive
classes: toc
icon: fa-google
---

To setup the Google Drive file store, first make sure you follow the steps [here]({{ site.baseurl }}/middleware/components/oauth/google.html). 

Then you will have to enable the Google Drive specific scopes via the [Google Developer Console](https://console.developers.google.com):

* Sign in to [Google Developers Console](https://console.developers.google.com/) using your Google account
* Select your project
* Navigate to "ENABLE APIS AND SERVICES"
* Enable the following APIs:
  * [Drive API](https://developers.google.com/identity/protocols/googlescopes#drivev3)
  * Drive SDK
  * Google Cloud SQL
  * Google Cloud Storage
  * Google Cloud Storage JSON API
  * [BigQuery API](https://developers.google.com/identity/protocols/googlescopes#bigqueryv2)

Last, install the package `open-xchange-file-storage-googledrive` and restart the middleware node.