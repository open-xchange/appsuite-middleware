---
title: User feedback services
---

# Introduction

For software improvement purposes, it is necessary to collect qualified feedback from the users of OX software and services. Therefore a set of tools is provided to store this feedback on server side, export it in a CSV-File, encrypt and send it via email to a set of recipients. If feedback data is no longer needed, the stored data can also be deleted by a qualified administrator.

The feedback feature needs a correct configured **global database (globaldb)**, because all data is stored there. You can find detailed informations on this topic **here**. Since one of various filters is the **context-group**, the user of the provided tools should also know what context groups are. Further information can be found **here**.

# Feedback storage
The feedback is collected from the user over a distinct dialog, the selection button is located at the lower right of the screen. An administrator can enable this feature by adding the **feedback** capability for a tenant. This enables a user to give feedback in one of the following modes:

* Stars mode is the old mode with a comment field and stars from 1 to 5.
* Module based mode has a selectbox to select a module. The current module is preselected. Otherwise it's the same as the stars mode.
* Nps mode uses the product name and has a scale from 0 to 10.

The mode is set by the <code>io.ox/core//feedback/mode</code> setting. The user is able to give his feedback, as often as he desires. Every given feedback is stored inside the <code>globaldb</code> database in the <code>feedback</code> table. The middleware provides a REST API for those purposes which is used by the UI and documented **here**.


# Feedback export
To review the collected userdata, a service provider can trigger the export of the collected data in a CSV file. This can be done by using the provided REST API, **documented here** or a CLT, **documented here**. The exported file will be stored on client side and contain all the desired data available. If no data is available, an empty CSV-file will be provided, containing only the headers.

To ensure privacy the login names of users are delivered in a hashed form. This way multiple entries can be matched to users without revealing their identity. Generally the following parameters are supported to filter the stored data:

* context group to export feedback for (optional, default: 'default')
* type of feedback to export (optional, default: 'star-rating-v1')
* time range to export feedback for (optional, default: export all feedback); use separate parameters for start and end time to support "since X" and "up to X" use cases

# Delete feedback
After a certain amount of time, it can be necessary to delete the collected feedback data to preserve database space. For those purposes a REST service, **documented here** and a CLT, **documented here** are available. The data can be deleted for a context group and one feedback type at a time. If no information is provided, the used default values are:

* context group : 'default'
* feedback type : 'star-rating-v1'

The user can also provide a timeframe, that should be considered. Only the data inside this timeframe will be deleted.

# Send Feedback via mail
In some cases the feedback has to be provided for a set of interested parties to evaluate the result. For this purpose the server provides a service to send the feedback file via email. There are two ways to trigger this, by using the REST service, **documented here** and a CLT, **documented here**. Since feedback providing and sending is an administrative task, the service provider must also configure the SMTP server to be used, by setting the properties [described here](https://documentation.open-xchange.com/latest/middleware/configuration/properties.html#userfeedback) in a <code>.properties</code> file, accessible by the server.

All filter options, that are available for exporting a feedback file, are also available here. Except the fact, that you don't need to provide a location for the result.