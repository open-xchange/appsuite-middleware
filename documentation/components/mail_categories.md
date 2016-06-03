---
title: Mail categories
---

# Mail categories documentation

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Quickstart guide](#quickstart-guide)
	- [1. Install the open-xchange-mail-categories package.](#1-install-the-open-xchange-mail-categories-package)
	- [2. Add 'mail_categories' capability](#2-add-mailcategories-capability)
	- [3. Add category identifiers](#3-add-category-identifiers)
	- [4. Add category configurations](#4-add-category-configurations)
	- [5. Setup the initiale system category rules (optional)](#5-setup-the-initiale-system-category-rules-optional)
	- [6. Check properties and restart the server](#6-check-properties-and-restart-the-server)
- [Introduction](#introduction)
- [Requirements](#requirements)
- [Configuration](#configuration)
	- [Server](#server)
		- [com.openexchange.mail.categories](#comopenexchangemailcategories)
		- [com.openexchange.mail.categories.enabled](#comopenexchangemailcategoriesenabled)
		- [com.openexchange.mail.categories.forced](#comopenexchangemailcategoriesforced)
		- [com.openexchange.mail.categories.general.name.fallback](#comopenexchangemailcategoriesgeneralnamefallback)
		- [com.openexchange.mail.categories.general.name.[locale]](#comopenexchangemailcategoriesgeneralnamelocale)
		- [com.openexchange.mail.categories.identifiers](#comopenexchangemailcategoriesidentifiers)
		- [com.openexchange.mail.categories.identifiers](#comopenexchangemailcategoriesidentifiers)
		- [com.openexchange.mail.categories.[category].flag](#comopenexchangemailcategoriescategoryflag)
		- [com.openexchange.mail.categories.[category].active](#comopenexchangemailcategoriescategoryactive)
		- [com.openexchange.mail.categories.[category].force](#comopenexchangemailcategoriescategoryforce)
		- [com.openexchange.mail.categories.[category].name.fallback](#comopenexchangemailcategoriescategorynamefallback)
		- [com.openexchange.mail.categories.[category].name.[locale]](#comopenexchangemailcategoriescategorynamelocale)
		- [com.openexchange.mail.categories.apply.ox.rules](#comopenexchangemailcategoriesapplyoxrules)
		- [com.openexchange.mail.categories.rules.[category]](#comopenexchangemailcategoriesrulescategory)
	- [Client](#client)

<!-- /TOC -->


## Quickstart guide

This guide gives a quick overview on how to get the mail categories feature running. If you instead want to know more about the feature and its configuration details take a look at the other chapters below.

This guide provides the quickest way to get the feature running. It will activate the feature for every user on the system and it will create five categories (3 system and 2 user categories) in total. Therefore it will not scope all configuration details. But it can be used as a guideline to adapt the configuration to ones individual needs. This guide also assumes that mails will not already be flagged by another system (e.g. system wide sieve rules).

This feature requires an imap server with support of the the imap4flags extension.

In order to get the mail categories feature running you have to do the following steps:

#### 1. Install the open-xchange-mail-categories package.

First of all you have to install the "open-xchange-mail-categories" package.

#### 2. Add 'mail_categories' capability

Open /opt/open-xchange/etc/mail-categories.properties and set ```com.openexchange.mail.categories``` to 'true'.

#### 3. Add category identifiers

Change following properties:

    com.openexchange.mail.categories.identifiers=promotion,social,purchases
    com.openexchange.mail.user.categories.identifiers=uc1,uc2

#### 4. Add category configurations

Add following properties:

    com.openexchange.mail.categories.promotion.flag=$promotion
    com.openexchange.mail.categories.promotion.force=false
    com.openexchange.mail.categories.promotion.active=true
    com.openexchange.mail.categories.promotion.fallback=Promotions
    com.openexchange.mail.categories.promotion.name.de_DE=Angebote

    com.openexchange.mail.categories.social.flag=$social
    com.openexchange.mail.categories.social.force=false
    com.openexchange.mail.categories.social.active=true
    com.openexchange.mail.categories.social.fallback=Social
    com.openexchange.mail.categories.social.name.de_DE=Social

    com.openexchange.mail.categories.purchases.flag=$purchases
    com.openexchange.mail.categories.purchases.force=false
    com.openexchange.mail.categories.purchases.active=true
    com.openexchange.mail.categories.purchases.fallback=Purchases
    com.openexchange.mail.categories.purchases.name.de_DE=Eink\u00E4ufe

    com.openexchange.mail.categories.uc1.flag=$uc1
    com.openexchange.mail.categories.uc1.active=true
    com.openexchange.mail.categories.uc1.fallback=Friends
    com.openexchange.mail.categories.uc1.name.de_DE=Freunde

    com.openexchange.mail.categories.uc2.flag=$uc2
    com.openexchange.mail.categories.uc2.active=true
    com.openexchange.mail.categories.uc2.fallback=Work
    com.openexchange.mail.categories.uc2.name.de_DE=Arbeit


#### 5. Setup the initiale system category rules (optional)

Since the mails are currently not flagged at all, all categories except the general category will be empty. In order to prevent such a bad user experience you have the possibility to define predefined rules, which will be added to each user individually. But in order to improve the imap performance it is strongly recommended to use system wide rules instead! This also makes changes/improvements of the rules much easier.

For example if you use dovecot with pigeonhole you can configure sieve scripts which will be run before the user scripts (see 'sieve_before' parameter: [Pigeonhole Configuration](http://wiki2.dovecot.org/Pigeonhole/Sieve/Configuration)). The sieve script for users in germany could look like this:

    require "imap4flags";

    if header :contains "from" ["facebook.com","twitter.com","youtube.com","plus.google.com","vimeo.com","tumblr.com","pinterest.com","instagram.com","flickr.com","xing.com","linkedin.com"]
    {
      addflag "$social";
    }

    if header :contains "from" ["amazon.de","paypal.de","ebay.de","ebay-kleinanzeigen.de","zalando.de","immobilienscout24.de","autoscout24.de","notebooksbilliger.de","otto.de","tchibo.de","lidl.de","aldi.de","aliexpress.com","bonprix.de","conrad.de","reichelt.de","orders.apple.com","euro.apple.com","cyberport.de","alternate.de","audible.de","steampowered.com","mytoys.de","ikea.com","hm.com","mediamarkt.de","saturn.de"]
    {
      addflag "$purchases";
    }

    if header :contains "from" ["netflix.com","dhl.de","unitymedia.de","finanztip.de"]
    {
      addflag "$promotion";
    }

In case you use a different imap server or dont want to use system wide rules you can define user rules by doing the following:

Set com.openexchange.mail.categories.apply.ox.rules to true

and uncomment or add the example mail address lists:

    com.openexchange.mail.categories.rules.social=facebook.com,twitter.com,youtube.com,plus.google.com,vimeo.com,tumblr.com,pinterest.com,instagram.com,flickr.com,xing.com,linkedin.com
    com.openexchange.mail.categories.rules.purchases=amazon.de,paypal.de,ebay.de,ebay-kleinanzeigen.de,zalando.de,immobilienscout24.de,autoscout24.de,notebooksbilliger.de,otto.de,tchibo.de,lidl.de,aldi.de,aliexpress.com,bonprix.de,conrad.de,reichelt.de,orders.apple.com,euro.apple.com,cyberport.de,alternate.de,audible.de,steampowered.com,mytoys.de,ikea.com,hm.com,mediamarkt.de,saturn.de
    com.openexchange.mail.categories.rules.promotion=netflix.com,dhl.de,unitymedia.de,finanztip.de

The used lists in both variants are not complete and are only applicable for users in germany. Therefore you should always adapt this lists to you individual needs.


#### 6. Check properties and restart the server

Finally your ```mail-categories.properties``` file should look similar to this:


    # The config-cascade aware properties for mail categories

    # General capability to enable/disable mail categories for primary inbox
    #
    # Default is "false"
    com.openexchange.mail.categories=true

    # Switch to show or hide mail categories feature during the first start.
    # Notice that this property only influence the starting value. Changing this value will have no effect on users with "com.openexchange.mail.categories" set to true.
    #
    # Default is "true"
    com.openexchange.mail.categories.enabled=true

    # Switch to force showing the mail categories feature
    #
    # Default is "false"
    com.openexchange.mail.categories.forced=false

    # The fallback name of the default general category.
    #
    # Defaults to General
    com.openexchange.mail.categories.general.name.fallback=General

    # Specifies a comma separated list of system category identifiers ([category]).
    #
    # System categories can be forced but not renamed.
    # Please note that the use of "general" is prohibited!
    #
    # No default value
    com.openexchange.mail.categories.identifiers=promotion,social,purchases

    # Specifies a comma separated list of user category identifiers ([category]). E.g.: "uc1,uc2,uc3"
    #
    # User categories can be renamed but not be forced.
    #
    # Please note that the use of "general" is prohibited!
    #
    # No default value
    com.openexchange.mail.user.categories.identifiers=uc1,uc2

    com.openexchange.mail.categories.promotion.flag=$promotion
    com.openexchange.mail.categories.promotion.force=false
    com.openexchange.mail.categories.promotion.active=true
    com.openexchange.mail.categories.promotion.fallback=Promotions
    com.openexchange.mail.categories.promotion.name.de_DE=Angebote

    com.openexchange.mail.categories.social.flag=$social
    com.openexchange.mail.categories.social.force=false
    com.openexchange.mail.categories.social.active=true
    com.openexchange.mail.categories.social.fallback=Social
    com.openexchange.mail.categories.social.name.de_DE=Social

    com.openexchange.mail.categories.purchases.flag=$purchases
    com.openexchange.mail.categories.purchases.force=false
    com.openexchange.mail.categories.purchases.active=true
    com.openexchange.mail.categories.purchases.fallback=Purchases
    com.openexchange.mail.categories.purchases.name.de_DE=Eink\u00E4ufe

    com.openexchange.mail.categories.uc1.flag=$uc1
    com.openexchange.mail.categories.uc1.active=true
    com.openexchange.mail.categories.uc1.fallback=Friends
    com.openexchange.mail.categories.uc1.name.de_DE=Freunde

    com.openexchange.mail.categories.uc2.flag=$uc2
    com.openexchange.mail.categories.uc2.active=true
    com.openexchange.mail.categories.uc2.fallback=Work
    com.openexchange.mail.categories.uc2.name.de_DE=Arbeit

    # A flag indicating whether the rules should be applied or not
    #
    # Defaults to 'false'
    com.openexchange.mail.categories.apply.ox.rules=true

    com.openexchange.mail.categories.rules.social=facebook.com,twitter.com,youtube.com,plus.google.com,vimeo.com,tumblr.com,pinterest.com,instagram.com,flickr.com,xing.com,linkedin.com

    com.openexchange.mail.categories.rules.purchases=amazon.de,paypal.de,ebay.de,ebay-kleinanzeigen.de,zalando.de,immobilienscout24.de,autoscout24.de,notebooksbilliger.de,otto.de,tchibo.de,lidl.de,aldi.de,aliexpress.com,bonprix.de,conrad.de,reichelt.de,orders.apple.com,euro.apple.com,cyberport.de,alternate.de,audible.de,steampowered.com,mytoys.de,ikea.com,hm.com,mediamarkt.de,saturn.de

    com.openexchange.mail.categories.rules.promotion=netflix.com,dhl.de,unitymedia.de,finanztip.de

If everything is allright you only have to restart the server.


## Introduction

With OX Middleware version 7.8.2 a feature called mail categories (aka tabbed inbox) is introduced.
This features divides the inbox of the primary email account into categories. Each category acts like a container for emails.
The emails will be categorized into this containers based on email flags. Whereby each mail can only be in one category at a time.
All mails which does not belong to any active category will be categorized in a 'general' category.

The mail categories feature currently allows the hoster/admin to:
* Enable the feature for users via config cascade
* Define and configure categories:
  * Define localized names for each category
  * Activate or deactivate specific categories at first start
  * Force specific categories to be shown
  * Reuse flags of another mail flagging system (e.g. system wide sieve rules)
* Define whether the mail categories feature should be enabled as default
* Define basic rules for system categories which will be added to each user which enables the mail categories feature for the first time


The mail categories feature currently allows the user to:
* Activate or deactivate the feature
* Move mails from one category to another
* Train a category with one ore more mail addresses
* Apply a rule to all old mails (reorganize)
* Rename a category
* Activate/Deactivate a category


There are two sets of categories. Each set contains a different type of categories which allows different operations. The two types are system categories and user categories.

**System categories**

System categories are predefined by the hoster. They include a specific set of rules, which determines if a mail should be put into a category or not.
They also have a fixed name and optional translations. Its possible for the hoster to prevent the deactivation of system categories.


**User categories**

In addition to the system categories the hoster is able to define user categories.
User categories are very similar to system categories. In difference to the system categories they do not include any rules at the beginning and
they can be renamed by the user. It is not possible to prevent deactivation of user categories.


**Customize categories**

Both system and user categories can be customized. The user has three options to do that.

1. Move one or more mails from one category to another
2. Train a category with one or more email addresses
3. Reorganize old mails

With the first option the user is able to move mails between the different categories manually.
The second option allows the user to train a category with one or more given email addresses.
Once trained all new emails which contains the full email address in the from header are categorized in the trained category.
If another category is trained with the same address the old rule will be removed. This means that all emails are only be flagged with one category flag at a time.
In addition to option two the user is also able to reorganize all existing mails within the inbox.
That means that all mails which match the rules of the category will be categorized in this category.

## Requirements

The mail categories feature requires an imap server with support of the the imap4flags extension. See https://tools.ietf.org/html/rfc5232 for further informations.

## Configuration

### Server

For this feature the new configuration file mail-categories.properties is introduced.
All configurations are config cascade aware and can therefore be overwritten on context or user level.


| Property                                                                                                        | Type / Values                   | Default   |
|:----------------------------------------------------------------------------------------------------------------|:--------------------------------|:----------|
| [com.openexchange.mail.categories](#comopenexchangemailcategories)                         | 'true', 'false'                 | 'false'   |
| [com.openexchange.mail.categories.enabled](#comopenexchangemailcategoriesenabled)                               | 'true', 'false'                 | 'true'    |
| [com.openexchange.mail.categories.forced](#comopenexchangemailcategoriesforced)                                 | 'true', 'false'                 | 'false'   |
| [com.openexchange.mail.categories.general.name.fallback](#comopenexchangemailcategoriesgeneralnamefallback)     | String                          | 'General' |
| [com.openexchange.mail.categories.general.name.[locale]](#comopenexchangemailcategoriesgeneralnamelocale)       | String                          |           |
| [com.openexchange.mail.categories.identifiers](#comopenexchangemailcategoriesidentifiers)                       | Comma separated list of strings |           |
| [com.openexchange.mail.categories.identifiers](#comopenexchangemailcategoriesidentifiers)                       | Comma separated list of strings |           |
| [com.openexchange.mail.categories.[category].flag](#comopenexchangemailcategoriescategoryflag)                  | String                          |           |
| [com.openexchange.mail.categories.[category].active](#comopenexchangemailcategoriescategoryactive)              | 'true', 'false'                 | 'true'    |
| [com.openexchange.mail.categories.[category].force](#comopenexchangemailcategoriescategoryforce)                | 'true', 'false'                 | 'false'   |
| [com.openexchange.mail.categories.[category].name.fallback](#comopenexchangemailcategoriescategorynamefallback) | String                          |           |
| [com.openexchange.mail.categories.[category].name.[locale]](#comopenexchangemailcategoriescategorynamelocale)   | String                          |           |
| [com.openexchange.mail.categories.apply.ox.rules](#comopenexchangemailcategoriesapplyoxrules)                   | 'true', 'false'                 | 'false'   |
| [com.openexchange.mail.categories.rules.[category]](#comopenexchangemailcategoriesrulescategory)                | Comma separated list of strings | |         |


#### com.openexchange.mail.categories

This property defines whether the user capability is granted to use the mail categories feature. This is the main switch for an administrator to enable/disable that feature.

**Note** The current mail categories implementatiuon also requires that the associated Sieve service advertises the `"imap4flags"` capability. Only if both conditions are met

1.  `com.openexchange.mail.categories` is set to `true`
2.  `"imap4flags"` capability announced by Sieve service

the mail categories feature becomes effectively available for a user.

#### com.openexchange.mail.categories.enabled

This property defines whether the mail categories feature should be enabled or not. This property only influence the starting value.
For example set this property to 'true' if you want your users to see the mail features after their first login. If you rather want the same ui experience like before set this value to 'false' instead.
Anyway users are able to show or hide the feature via configuration.

#### com.openexchange.mail.categories.forced

If this property is set to true it overwrites the com.openexchange.mail.categories.enabled property. Then users will not be able to hide the feature via configuration.

#### com.openexchange.mail.categories.general.name.fallback

Defines the default name of the general category.
Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe".

#### com.openexchange.mail.categories.general.name.[locale]

Each entry defines a localized name of the general category. For each entry [locale] must be replaced with a ISO-639-2 language code followed by a underscore followed by a ISO-3166 country code (e.g. de_DE or en_US).
Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe".

For example: com.openexchange.mail.categories.general.name.de_DE=Allgemein

#### com.openexchange.mail.categories.identifiers

Specifies a comma separated list of system category identifiers. This identifiers will be used to identify category specific configurations.
It is not necessary but recommended to use meaningful names like "promotion" or "social". Please also notice that the categories will be displayed in the same order they are listed here, whereby the system categories will be placed before the user categories.
It is also possible to define only one type of categories, either system or user categories.

#### com.openexchange.mail.categories.identifiers

Specifies a comma separated list of user category identifiers. This identifiers will be used to identify category specific configurations.
Since the purpose of these categories can change over time it is not necessary to use meaningful names here. Please also notice that the categories will be displayed in the same order they are listed here, whereby the system categories will be placed before the user categories.
It is also possible to define only one type of categories, either system or user categories.

#### com.openexchange.mail.categories.[category].flag

A category specific attribute which defines the used flag string for this category. Never use the same flag for different categories, since this would cause mails to be present in more than one category at the same time.
If your mails are already flagged by any mechanism you can reuse the written flags here to categorize these mails.


#### com.openexchange.mail.categories.[category].active

A category specific attribute which defines whether the category is active as default. If the value is set to false the mails with the flag of this category will be shown under the genereal tab until the user manually activates this particular category.


#### com.openexchange.mail.categories.[category].force

A system category specific attribute which defines whether the category should always be shown. If set to true the user isn't able to deactivate this specific category any more.


#### com.openexchange.mail.categories.[category].name.fallback

Each entry defines a category specific default name for the specific category.
Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe".

For example: com.openexchange.mail.categories.uc1.name.fallback=Work


#### com.openexchange.mail.categories.[category].name.[locale]

Each entry defines a category specific localized name for the specific category. For each entry [locale] must be replaced with a ISO-639-2 language code followed by a underscore followed by a ISO-3166 country code (e.g. de_DE or en_US).
Please use unicode notation for non-ascii characters; e.g. "Entw\u00fcrfe".

For example: com.openexchange.mail.categories.uc1.name.de_DE=Arbeit


#### com.openexchange.mail.categories.apply.ox.rules

Defines whether user rules should be created or not. It is strongly recommended to set this value to false and use system wide rules instead to increase the performance of the imap server.


#### com.openexchange.mail.categories.rules.[category]

A system category specific attribute which Defines a comma separated list of email addresses. This addresses will be used to create a starting rule for the given system category.
It is also possible to use mail address parts here. For example "@amazon.com".



### Client

The config tree is extended with additional entries. First of all the capability mail_categories is introduced, which defines whether the mail_categories module is available for the user.
In addition the io.ox/mail tree is extended with an entry 'categories':

    "categories": {
         "enabled": true,
         "forced": false,
         "initialized": finished
         "list": [
           {
             "id": "offers",
             "name": "Offers",
             "active": true,
             "permissions": [
               "teach"
             ]
           },
           {
             "id": "other",
             "name": "other",
             "active": true,
             "permissions": [
               "disable",
               "teach"
             ]
           },
           {
             "id": "uc1",
             "name": "Friends",
             "active": true,
             "permissions": [
               "rename",
               "teach"
             ]
           }
         ]
       }

The categories entry contains the fields: 'enabled', 'forced', 'initialized' and 'list'. The 'enabled' field is a boolean flag indicating whether the categories should be shown or not. It is writeable and can be overwritten by the forced field, which itself is read only. The 'initialized' field is a string field and can contain one of the following values: 'notyetstarted', 'running', 'finished' or 'error'. It is indicating whether the init process hasn't started, is still running or has already finished for the current user. In case the init process failed, initialized is set to 'error' and the init process will be restarted with the next login.
The 'list' field contains an array of single category configurations and always contains the 'general' category. Each category config has four fields: 'id', 'name', 'active' and 'permissions'.

| Fieldname   |                                                                 Description |
|:------------|----------------------------------------------------------------------------:|
| id          |                                                         category identifier |
| name        |                       The current possible translated name of the category. |
| active      |            A boolean flag indicating whether the category is active or not. |
| Permissions | A list of permissions. Possible values are: 'rename', 'train' and 'disable' |

The permissions _rename_ and _disable_ indicating whether the fields _name_ and _active_ are writable or not. All other fields are read only and must not be changed by the client.


## Summary and recommendations

This chapter contains a list of recommendations, which should be considered in order to provide the best performance and user experience.

* Use system wide rules (e.g. dovecot sieve_before sieve scripts).
* Define an own set of rules for your region
* Define meaningful names for every category
* Define translations for all supported languages
* Define at least two user categories
* Define 4 - 6 categories in total. If you only want to use one kind of categories you should define 2~3 categories instead.
