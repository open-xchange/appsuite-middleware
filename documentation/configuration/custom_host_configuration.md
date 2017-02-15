---
title: Custom host configuration
---

# Introduction

In a multi tenant environment custom features have to be enabled for certain tenants that should not be available for others or simply different from the server settings. To enable this behavior without providing a distinct environment for each tenant, the server admin has the ability to provide a configuration file, the **as-config.yml**.

This file is loaded during server start and provides the following features.

# Content

Example content:

	# Override certain settings
	default:
	    host: all
	    contact: ''
	
	host*.mycloud.net:
	    hostRegex: host.*\.mycloud\.net
	    someRegexHostKey: someRegexHostValue
	
	# Override certain settings for certain hosts
	host1.mycloud.net:
	    host: host1.mycloud.net
	    pageTitle: 'Professional Webmail OX '
	    productName: 'Professional Webmail OX'
	    pageHeaderPrefix: ''
	    pageHeader: ''
	    signinTheme: 'host1.mycloud.net'
	    contact: 'E-Mail: cloudservice@host1-support.de'
	    languages:
	        de_DE: Deutsch
	    notificationMails:
	        button:
	            textColor: '#ffffff'
	            backgroundColor: '#3c73aa'
	            borderColor: '#356697'
	        footer:
	            text: 'Footer text'
	
	host2.mycloud.net:
	    host: host2.mycloud.net
	    pageTitle: 'OXaaS'
	    productName: 'OX as a Service'
	    pageHeaderPrefix: 'OX'
	    pageHeader: 'Cloud Service'
	    forgotPassword: false
	    languages: all
	    notificationMails:
	        button:
	            textColor: '#ffffff'
	            backgroundColor: '#3c73aa'
	            borderColor: '#356697'
	
	host3.mycloud.net:
	    host: host3.mycloud.net
	    productName: 'OX Cloud'
	    pageTitle: 'OX Cloud'
	    pageHeaderPrefix: 'OX Cloud'
	    pageHeader: ''
	    contact: 'Please contact your service provider'
	    signinTheme: 'host3.mycloud.net'
	    notificationMails:
	        button:
	            textColor: '#000000'
	            backgroundColor: '#ffffff'
	            borderColor: '#000000'
	        footer:
	            image: 'custom_logo.png'
	            text: 'Footer text'

## Default features
In the first part of the file the admin can set default values for certain properties. To apply them to all hosts on the system the <code>host: all</code> value has to be set.

## Custom host features

The second part holds all custom host configuration and adresses each host by the <code>host</code> property. If this property is missing, the following custom configurations will not be applied. This parameter also accepts wildcards, to apply certain features by default to a group of hosts. For more information see the [Wildcards](#Wildcards) chapter for more information on this topic.

### Autologin
If the admin wants a custom autologin handling for each tenant on the system, **the server has to enable this feature per default**. This way the admin can disable it via custom configuration inside the <code>as-config.yml</code>. The <code>com.openexchange.sessiond.autologin</code> property should therefore be set to false, for this host. 

**example**

	example.host.com:
	    host: example.host.com
	    com.openexchange.sessiond.autologin: false

### Sharding subdomains

For request distribution purposes, the administrator can configure an array of sharding subdomains. If set up correctly, those subdomains will primarly be used to load image previews in drive. This way, requests needed to keep the UI under full functionality are detached from those requests. Those subdomains have to be registered inside the apache configuration. Example:

	<VirtualHost *:80>
	    ServerName sharding.local-appsuite.com
	</VirtualHost>

Those subdomains can point to the same server, which handles all other requests or another distinct server. This has to be configured inside the <code>hosts</code> file. Inside the <code>as-congig.yml</code> a host has to provide the following parameter to enable sharding:

	shardingSubdomains: 
		- "sharding.local-appsuite.com/appsuite/api"
	
Please note, that the full API path has to be provided. This way the administrator is capable of configuring a custom api location. The same cookies will be used for all requests on the main domian and all subdomains provided. Attention, even a single sharding subdomain has to be provided as an array.

**Attention**
When using sharding subdomains for custom hosts, cookie domains should be disabled to make sure that cookies are used correctly. 
	
	com.openexchange.cookie.domain.enabled=false

## Wildcards