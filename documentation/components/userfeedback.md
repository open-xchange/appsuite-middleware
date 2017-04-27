---
title: User feedback services
---

# Introduction

For software improvement purposes, it is necessary to collect qualified feedback from the users of OX software and services. Therefore a set of tools is provided to store this feedback on server side, export it in a CSV-File, encrypt and send it via email to a set of recipients. If feedback data is no longer needed, the stored data can also be deleted by a qualified administrator.

The feedback feature needs a correct configured **global database (globaldb)**, because all data is stored there. You can find detailed informations on this topic [here](https://oxpedia.org/wiki/index.php?title=AppSuite:CrossContextDatabase). Since one of various filters is the **context-group**, the user of the provided tools should also know what context groups are. Further information can be found [here](https://oxpedia.org/wiki/index.php?title=AppSuite:CrossContextDatabase).

As dealing with stored feedback is based on REST calls the open-xchange-rest package is a required dependency and will also be installed when open-xchange-userfeedback should be installed.

# Feedback storage
The feedback is collected from the user by showing a dedicated dialog within the web UI. An administrator can enable this feature by adding the **feedback** capability via ConfigCascade on user, context, context set or server level. This enables a user to give feedback in one of the following modes:

* Stars mode is the old mode with a comment field and stars from 1 to 5.
* Module based mode has a selectbox to select a module. The current module is preselected. Otherwise it's the same as the stars mode.
* Nps mode uses the product name and has a scale from 0 to 10.

The mode is set by the <code>io.ox/core//feedback/mode</code> setting (default: <code>star-rating-v1</code>). The user is able to give his feedback, as often as he desires. Every given feedback is stored inside the <code>globaldb</code> database in the <code>feedback</code> table. The middleware provides a HTTP API for those purposes which is used by the UI and documented [here](/components/middleware/http/{{ site.baseurl }}/index.html?version={{ site.baseurl }}#/StoreRequest).

Since 7.8.4 the Open-Xchange server provides a default implementation for <code>star-rating-v1</code>. It is easy to create a custom feedback type by implementing related interfaces and register the custom feedback type for a defined user group (see above).

# Feedback export
To review the collected feedback, a service provider can trigger the export of the collected data. 

## star-rating-v1 export

For feedback type <code>star-rating-v1</code> the export as comma separated values in a file and raw type as JSON is provided. This can be done by using the provided REST API, [documented here](/components/middleware/http/{{ site.baseurl }}/index.html?version={{ site.baseurl }}#/Userfeedback/ExportCSVRequest) or a CLT (CSV only), [documented here]({{ site.baseurl }}/middleware/components/commandlinetools/ExportUserFeedback.html). 

To ensure privacy the user and context id are delivered in a hashed form. This way multiple entries can be matched to one user without revealing their identity. Generally the following parameters are supported to filter the stored data:

* context group to export feedback for (optional, default: 'default')
* type of feedback to export (optional, default: 'star-rating-v1')
* time range to export feedback for (optional, default: export all feedback); use separate parameters for start and end time to support "since X" and "up to X" use cases

### CSV 

The export as file will be stored on client side and contain all the desired data available. If no data is available, an empty CSV-file will be provided, containing only the headers.

### RAW 

Raw export will provide a JSON array containing the requested feedback (based on possible filters). If no data is available, an empty array will be returned.


# Delete feedback
After a certain amount of time, it can be necessary to delete the collected feedback data to preserve database space. For those purposes a REST service, [documented here](/components/middleware/http/{{ site.baseurl }}/index.html?version={{ site.baseurl }}#/Userfeedback/DeleteRequest) and a CLT, [documented here]({{ site.baseurl }}/middleware/components/commandlinetools/DeleteUserFeedback.html) are available. The data can be deleted for a context group and one feedback type at a time. If no information is provided, the used default values are:

* context group : 'default'
* feedback type : 'star-rating-v1'

The user can also provide a timeframe, that should be considered. Only the data inside this timeframe will be deleted.

# Send Feedback via mail
In some cases the feedback has to be provided for a set of interested parties to evaluate the result. For this purpose the server provides a service to send the feedback file via email. There are two ways to trigger this, by using the REST service, [documented here](/components/middleware/http/{{ site.baseurl }}/index.html?version={{ site.baseurl }}#/Userfeedback/SendMailRequest) and a CLT, [documented here]({{ site.baseurl }}/middleware/components/commandlinetools/SendFeedbackViaMail.html). Since feedback providing and sending is an administrative task, the service provider must also configure the SMTP server to be used, by setting the properties [described here](https://documentation.open-xchange.com/latest/middleware/configuration/properties.html#userfeedback) in a <code>.properties</code> file, accessible by the server.

## Send secure

Mails containing user feedback can optionally be signed and encrypted using PGP. If a PGP secret key is provided, all feedback emails are signed with this key. To encrypt mails using PGP, a PGP public key must be provided for every recipient that should recieve an encrypted mail. Recipients without a PGP public key will recieve an unencrypted mail. When using the CLT, the path to recipients' PGP public keys must be provided in the CSV file (pattern &lt;mail address&gt;,&lt;display name&gt;,&lt;path to public key&gt;). When using REST service, the key must be sent in request body. All PGP secret and public keys have to be in ascii-armored format.

All filter options, that are available for exporting a feedback file, are also available here. Except the fact, that you don't need to provide a location for the result.