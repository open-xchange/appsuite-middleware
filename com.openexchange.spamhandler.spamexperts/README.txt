### OPEN-XCHANGE SPAMEXPERTS PLUGIN ###

Description:

This plugin adds functionality to the Open-Xchange system to be logged into the external Spamexperts.com WEB UI without
authenticating a second time against the Spamexperts.com WEB UI.

Components within this plugin: 

OX UI-Plugin:

The UI plugin extends the OX settings tree with a new subtree to access the "Antispam" management WEB UI of Spamexperts.com

OX Server-Plugin:

The Server plugin consists of 1 Servlet which can create an authticket for the currently active OX user by using the 
Spamexperts.com API and then sends the ticket back to the UI plugin which will do the redirect to the external Spamexperts.com management WEB UI.

Configuration file(s):

/opt/open-xchange/etc/groupware/spamexperts.properties

Defines:

- Spamexperts API URL, 
- Spamexperts API Adminuser/Password,
- Spamexperts WEB UI Base-URL, 
- Servlet Mountpoint,
- Authentication attribute of OX user for use against the Spamexperts API