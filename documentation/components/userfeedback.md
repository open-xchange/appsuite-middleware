---
title: User feedback
---

# Introduction

For software improvement purposes, it is necessary to collect qualified feedback from the users of OX software and services. Therefore a set of tools is provided to store this feedback on server side, export it in a CSV-File, encrypt and send it via email to a set of recipients. If feedback data is no longer needed, the stored data can also be deleted by a qualified administrator.

The feedback feature needs a correct configured **global database (globaldb)**, because all data is stored there. Until 7.8.4 the globaldb has only be used to store small data pieces related to sharing and OAuth. With user feedback there might be a high amount of data that becomes persisted. Make sure that your global database is dimensioned appropriately. You can find detailed informations on this topic [here](https://oxpedia.org/wiki/index.php?title=AppSuite:CrossContextDatabase). 

Since one of various filters is the **context-group**, the user of the provided tools should also know what context groups are. Further information can be found [here](https://oxpedia.org/wiki/index.php?title=AppSuite:CrossContextDatabase).

As dealing with stored feedback is based on REST calls the open-xchange-rest package is a required dependency and will also be installed when open-xchange-userfeedback should be installed.

# Feedback storage
The feedback is collected from the user by showing a dedicated dialog within the web UI. An administrator can enable this feature by setting the property **com.openexchange.userfeedback.enabled** via ConfigCascade on user, context, context set or server level. This enables a user to give feedback in the following way:

* Stars mode (per module) with a comment field and stars from 1 to 5.

The user is able to give his feedback, as often as he desires. Every given feedback is stored inside the <code>globaldb</code> database in the <code>feedback</code> table. The middleware provides a HTTP API for those purposes which is used by the UI and documented [here](/components/middleware/http{{ site.baseurl }}/index.html#!/userfeedback/store).

Since 7.8.4 the Open-Xchange server provides a default implementation for <code>star-rating-v1</code>. It is easy to create a custom feedback type by implementing related interfaces and register the custom feedback type for a defined user group (see above).

# Configuration guide

To set up and run user feedback the administrator has to do the following configurations:

* as already mentioned it is required to have a successfully installed global database.
* install the package 'open-xchange-userfeedback'. Per default the feedback option will be enabled. 
* to configure the user feedback feature please have a look at [the general property page](/components/middleware/config{{ site.baseurl }}/index.html#mode=features&feature=Feedback) to enable and disable the feature globally. Use the ConfigCascade to enable the feature only for dedicated groups. 
* please also take a look at [the SMTP configuration page](/components/middleware/config{{ site.baseurl }}/index.html#mode=features&feature=Feedback Mail SMTP-Service) to configure an endpoint to be able send the collected user feedback to one or more recipients.

If everything is set up as described you can continue with the following steps.

# Feedback export
To review the collected feedback, a service provider can trigger the export of the collected data. 

## star-rating-v1 export

For feedback type <code>star-rating-v1</code> the export as comma separated values in a file is provided. This can be done by using the provided REST API or a CLT (CSV only), [documented here]({{ site.baseurl }}/middleware/components/commandlinetools/ExportUserFeedback.html). 

To ensure privacy the user and context id are delivered in a hashed form. This way multiple entries can be matched to one user without revealing their identity. Generally the following parameters are supported to filter the stored data:

* context group to export feedback for (optional, default: 'default')
* type of feedback to export (optional, default: 'star-rating-v1')
* time range to export feedback for (optional, default: export all feedback); use separate parameters for start and end time to support "since X" and "up to X" use cases

The handling of export data (and the date contained within the export) is in Coordinated Universal Time (UTC). 

### CSV 

The export as file will be stored on client side and contain all the desired data available (based on start/end date filter). If no data is available, an empty CSV-file will be provided, containing only the headers. 

By using the provided command line tool 'exportuserfeedback' located in /opt/open-xchange/sbin you can define above mentioned filter and additionally the column delimiter used within the export file. After the file has been created try to open the file by double clicking it. Sometimes importing it won't work (because of different processing of the calc application). 

### RAW 

Raw export will provide a JSON array containing the requested feedback (based on possible filters). If no data is available, an empty array will be returned.


# Delete feedback
After a certain amount of time, it can be necessary to delete the collected feedback data to preserve database space. For those purposes a REST service or a CLT, [documented here]({{ site.baseurl }}/middleware/components/commandlinetools/DeleteUserFeedback.html) are available. The data can be deleted for a context group and one feedback type at a time. If no information is provided, the used default values are:

* context group : 'default'
* feedback type : 'star-rating-v1'

The user can also provide a timeframe, that should be considered. Only the data inside this timeframe will be deleted.

# Send Feedback via mail
In some cases the feedback has to be provided for a set of interested parties to evaluate the result. For this purpose the server provides a service to send the feedback file via email. There are two ways to trigger this, by using the REST service or a CLT, [documented here]({{ site.baseurl }}/middleware/components/commandlinetools/SendFeedbackViaMail.html). Since feedback providing and sending is an administrative task, the service provider must also configure the SMTP server to be used, by setting the properties [described here](https://documentation.open-xchange.com/latest/middleware/configuration/properties.html#userfeedback) in a <code>.properties</code> file, accessible by the server.

## Send secure

Mails containing user feedback can optionally be signed and encrypted using PGP. If a PGP secret key is provided, all feedback emails are signed with this key. To encrypt mails using PGP, a PGP public key must be provided for every recipient that should recieve an encrypted mail. Recipients without a PGP public key will receive an unencrypted mail. When using the CLT, the path to recipients' PGP public keys must be provided in the CSV file (pattern &lt;mail address&gt;,&lt;display name&gt;,&lt;path to public key&gt;). When using REST service, the key must be sent in request body. All PGP secret and public keys have to be in ASCII-armored format.

All filter options, that are available for exporting a feedback file, are also available here. Except the fact, that you don't need to provide a location for the result.

