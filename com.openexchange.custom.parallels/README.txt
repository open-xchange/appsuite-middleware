### OPEN-XCHANGE PARALLELS POA PLUGIN ###

Description:

This plugin adds following functionality to the Open-Xchange system. Special authentication plugin instead of default Database authentication, 
Marking mails as spam will also teach SPAMD of POA the mail, deactivates the "Theme" selector in the OX WEB UI, Management of POA Black/Whitelist via OpenAPI

Components within this plugin: 

OX UI-Plugin:

The UI plugin deactivates the OX "theme" selection to restrict access to other themes. 

OX Server-Plugin:

The Server plugin consists of:

+ HTTP Servlet which is a wrapper for POA OpenAPI to manage black/white list.
+ Authentication Plugin which uses the whole login string and checks if this login string exists in a context login mapping like test@test.com||test@test.com.
  This maps the login test@test.com to the user test@test.com in the context where the login mapping was found. This works since OX context login mappings are unique over all contexts.
+ OX Hostname-Service Implementation to modify the hostnames which are used in directlinks of appointment mails and infostore objects.
+ Spamhandler Implementation to pass the marked mail to the SPAMD system which runs on the same server as the users IMAP server. Communication is done via XML-RPC.

Configuration file(s):

/opt/open-xchange/etc/groupware/parallels.properties

Defines:

- OpenAPI http basic auth credentials auth password
- OpenAPI http basic auth credentials auth id
- if OpenAPI calls should be made with http basic auth
- URL to the HTTP OpenAPI interface of POA
- Port which will be used to communicate with the POA XML-RPC Service
- Fallback URL which should be used if no branding-URL was specified for a context
- Mount point for the http servlets