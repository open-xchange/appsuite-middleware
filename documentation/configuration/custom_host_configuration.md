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

The second part holds all custom host configuration and addresses each host by the <code>host</code> property. If this property is missing, the following custom configurations will not be applied. This parameter also accepts wildcards, to apply certain features by default to a group of hosts. For more information see the [Wildcards](#Wildcards) chapter for more information on this topic.

### Autologin
If the admin wants a custom autologin handling for each tenant on the system, **the server has to enable this feature per default**. This way the admin can disable it via custom configuration inside the <code>as-config.yml</code>. The <code>com.openexchange.sessiond.autologin</code> property should therefore be set to false, for this host. 

**example**

	example.host.com:
	    host: example.host.com
	    com.openexchange.sessiond.autologin: false

### Sharding subdomains

For request distribution purposes, the administrator can configure an array of sharding subdomains. If set up correctly, those subdomains will primarily be used to load image previews in drive. This way, requests needed to keep the UI under full functionality are detached from those requests. Those subdomains have to be registered inside the apache configuration. Example:

	<VirtualHost *:80>
	    ServerName sharding.appsuite.com
	</VirtualHost>

Those subdomains can point to the same server, which handles all other requests or another distinct server. This has to be configured inside the <code>hosts</code> file. Inside the <code>as-congig.yml</code> a host has to provide the following parameter to enable sharding:

	shardingSubdomains: 
		- sharding.appsuite.com/appsuite/api
	
Please note, that the full API path has to be provided. This way the administrator is capable of configuring a custom api location. The same cookies will be used for all requests on the main domain and all subdomains provided. Attention, even a single sharding subdomain has to be provided as an array.

**Attention**
When using sharding subdomains for custom hosts, cookie domains should be disabled to make sure that cookies are used correctly. 
	
	com.openexchange.cookie.domain.enabled=false
	

###External login page
The login mask can reside on an external server i. e., not directly located in the domain of the Open-Xchange machines. If an administrator wants to use an external login page, the following steps have to be applied.
####HTML
The Open-Xchange HTTP API (Form login) has a function to create a session from external. The full detailed explanation how the HTML form is used and errors can be handled can be found here: [OX Session Formlogin](http://oxpedia.org/wiki/index.php?title=OXSessionFormLogin)

####Redirect to custom login
Users can still access the original product login site. If this is not wanted the following Apache configuration for your VirtualHost can be used to redirect all requests to your custom login page:

**before 7.8.0:**

	RewriteEngine On
	RewriteRule ^/appsuite/signin /custom-login.html [R]
**Since 7.8.0:** It can be configured directly in /opt/open-xchange/etc/as-config.yml

	# Override certain settings
	default:
    	host: all
    	loginLocation: 'http://example.com/appsuite/mycustomlogin.html'
Note: you need to have the right syntax (like leading spaces) in the .yml file.

It is possible to have different login pages for different domains, just add another section with a different host: <mydomain.com> line.

####Setting custom login as logout location
If users should be directed to the custom login form after logout from App Suite the following property can be set globally somewhere in /opt/open-xchange/etc/settings/. Either create a new properties file or add the option in any existing one. For more complex setups e.g. with different brands please check out how to set this property in context or user scope which is explained [here](http://oxpedia.org/wiki/index.php?title=ConfigCascade).

	io.ox/core//customLocations/logout=https://login.example.com

For cases where only one custom login page exists for all users it's also recommended to set

	logoutLocation: 'https://login.example.com/'
	
in ``/opt/open-xchange/etc/as-config.yml.`` This setting takes effect for example if an autologin session is expired. as-config.yml itself defines settings in dependence of the hostname configured for the Open-Xchange access.

###Theming the login page
A customized theme for the default login page for an appsuite installation and also the configuration of different ones for different hostnames is possible by referencing those themes inside the as-config.yml.

####style.less
To apply a theme to the login page you just add the relevant snippets to the style.less file like for a normal theme and include the logos and artifacts in the theme directory.

Here are some examples of CSS selectors which can be addressed on the login page:

	#io-ox-login-username
	#io-ox-login-screen .btn-primary
	#io-ox-login-screen .btn-primary:hover
	#io-ox-login-header-prefix
	#io-ox-login-header-label
	#io-ox-login-container
	.wallpaper
	body.down #io-ox-login-container .alert.alert-info
	.language-delimiter
	#io-ox-copyright

**as-config property**

To actually apply the above definition the theme needs to be specified in the file /opt/open-xchange/etc/as-config.yml:

	signinTheme: MYTHEME

As ``as-config.yml` can have different configuration based on different hostnames a multi branded configuration can be applied as well.

####Other login page properties
This list provides some other properties, that influence the login page itself or its behavior.

|Name   |Type   |Description   |Example   |
|---|---|---|---|
| buildDate   | String  | Bottom right of the login screen in brackets.  | buildDate: 01.01.1970  |
| copyright   | String  | Bottom of the login screen and also inside the about dialog  | copyright: Customs ltd.  |
| forgotPassword  | String  | URL path that should be used to direct the user to the <br>forgot password path. Will be appended to the current API path.  | forgotPassword: forgot/Password.html  |
| languages  | Map  | List of languages that are supposed to be displayed to the user  |languages: <br>de_DE: German <br>en_US: American english <br>en_EN: Britain english |
| pageHeader  | String  | The header of the login mask.  | pageHeader: Company  |
| pageHeaderPrefix  | String  | A Text written before the page header.  | pageHeader: My  |
| pageTitle  | String  | Sets the page title, visible as a prefix in the tab name.  | pageTitle: My Company  |
| serverVersion  | String  | Bottom of the login screen, next to the copyright information <br>also inside the about dialog.  | serverVersion: 1.1.0  |
| staySignedIn  | Boolean  | Sets wether the stay signed in checkbox is selected or not on initial view of the login page  | staySignedIn: true  |

###Theming the about dialog
This dialog can be accessed from inside the Appsuite and displays some general information about the product along side with some contact informations.

|Name   |Type   |Description   |Example   |
|---|---|---|---|
| productName   | String  | The product name as the title of the about dialog.  | productName : My Company  |
| serverVersion   | String  | Bottom of the login screen, next to the copyright information also inside the about dialog.  | serverVersion: 1.1.0  |
| copyright  | String  | Bottom of the login screen and also inside the about dialog  | copyright: Customs ltd.  |
| contact  | String  | Contact information displayed in the about dialog.  | contact: Contact us under support@mycompany.com |

###Other properties

|Name   |Type   |Description   |Example   |
|---|---|---|---|
| guestLocationLogout   | String  | Where should the user be directed after logout.  | guestLocationLogout: guest/Logout.html  |
| prefix   | String  | Prefix to be used before each HTTP-API call  | prefix: custom/Path/  |
| version  | String  | Used to drop the current JS FileCache, whenever a new version is installed.  | version: 0.5.2  |
| forceHTTPS  | Boolean  | When set to true, HTTPS is used for each HTTP request  | forceHTTPS: true |
| productNameMail  | String | Custom naming for the mail application | productNameMail: MyMail |

###Notification mails
It is possible to define the basic style of all notification mails for a host. When you do so, you have to override all mandatory settings. Example 

	notificationMails:
		#mandatory
        button:
            textColor: '#ffffff'
            backgroundColor: '#3c73aa'
            borderColor: '#356697'
        #Optional    
        footer:
        	image: 'ox_logo_claim_blue_small.png'
            text: 'Footer text'

|Name   |Type   |Description   |Example   |
|---|---|---|---|
| button   | String  | Where should the user be directed after logout.  | button:<br>textColor: '#ffffff'<br>backgroundColor: '#3c73aa'<br>borderColor: '#356697' |
| footer   | String  | Notification mails can contain a footer section consisting of <br> a text and logo/image. To omit footers at all, you can omit this  | footer:<br> image: 'ox_logo_claim_blue_small.png' <br>text: 'Footer text'  |
| image  | String  | Images are referenced via their file name below <br> '/opt/open-xchange/templates'. If you don't want any image <br> to be included, omit this key.  | image: ox_logo_claim_blue_small.png  |
| text  | Boolean  | The footer text can be customized. If no text shall <br>be displayed, omit this key. | text: 'Footer text' |

## Wildcards
Whenever the administrator needs to define a set of features for a set of hosts with similar domain names, he can use Regex and '*' wildcards as part of a host parameter and set properties for this set of hosts.
```
    host*.mycloud.net:
	    hostRegex: host.*\.mycloud\.net
	    someRegexHostKey: someRegexHostValue
```
In the example under the [Content](#Content) chapter, the value of someRegexHostKey would apply to all three following hosts.