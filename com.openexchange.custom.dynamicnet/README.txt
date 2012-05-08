### OPEN-XCHANGE DYNAMIC NET PLUGIN ###

Description:

This plugin adds functionality to the Open-Xchange system to be logged into the external Confixx WEB UI without
authenticating a second time. Also provides a hostname plugin to modify directlinks and logout URL. It also provides an authentication plugin 
which uses the dynamic net IMAP server to check authenication. 

Components within this plugin: 

OX UI-Plugin:

The UI plugin extends the OX settings tree with a new subtree to access the "Confixx" management WEB UI. 

OX Server-Plugin:

The Server plugin consists of 1 authentication plugin which checks users against dynamic net IMAP server. If a user login with URL 
http://gamma.ibone.ch he will use <username_gamma> as his login name. The plugin takes the <_gamma> part of the login and uses this part 
to build the IMAP auth hostname which would then be gamma.ibone.ch since all users are distributed over more than 40 IMAP servers.

Configuration file(s):
