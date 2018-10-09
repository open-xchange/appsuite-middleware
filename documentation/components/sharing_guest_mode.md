---
title: Sharing and Guest Mode
---

##Introduction

Starting with v7.8.0, the Open-Xchange server comes with a whole new concept to share contents with external people, allowing guest users to interact with the shared data in the same way as regular groupware users do. This article describes the underlying technical implications and outlines the different use cases.

The main idea behind the new sharing concept is that guest users, i.e. external users without a regular account on the server, should be able to access the shared contents using the existing interfaces, especially the App Suite web interface. On the one hand, this includes consuming the shared data using the App Suite's advanced media viewing capabilities. On the other hand, this enables guests to edit existing as well as to create or upload new content in the groupware. Even real-time collaboration between internal users and guests in OX Documents is possible.

The following chapters cover different topics regarding sharing and guest users and try to describe some technical background and impact where hosters, administrators or integrators might be interested in.

##Creating Shares

Basically, creating a share means adding an additional permission entity to the shared folder or item. Previously, this was only possible for &quot;internal&quot; entities, i.e. regular users or user groups. Now, the underlying permission system has been extended to support external entities, which can be either invited guest users, or special &quot;anonymous&quot; guest users who access a shared folder or item via a secret link. Anonymous and invited guest users are explained in more detail below.

Sharing is available for the groupware modules Calendar, Contacts, Tasks and Drive (a.k.a. Infostore/Files). While the latter one also allows "writable" access for invited guest users, folders from the Calendar, Contacts and Tasks module may only be published in "read-only" mode to external guests.

###Invite Guests

To share something to a guest user, it's possible to just add the e-mail address of the invitee as new permission entity for files and folders. The middleware then takes care to provision a new or reuse an existing account for the guest user, and equips him with the required permissions for accessing the contents. So, from a client's point of view, sharing something to a guest user is mostly the same process as sharing something to an internal user or group.

###Share Links

Besides explicitly inviting a guest user to a share, it's also possible to just get a secret link for a folder or item. This will result in an additional &quot;anonymous&quot; guest entity in the permissions of the shared object, and will grant any user with the corresponding share link access the shared contents. To simplify the creation of share links, the clients will offer an additional &quot;wizard&quot; to quickly get a share link for a folder or item. Unlike invited guests which behave much like internal users, anonymous guest entities are strictly bound to the underlying folder or item, i.e. there is at most one anonymous permission entity per file or folder, as well as an anonymous permission entity can only be used for only once.

###Required Permissions and Capabilities

Whether a user is allowed to create share links, invite external guests, or internal groups or users, depends on the following module access permissions and capabilities. Please note that share links no longer require <code>read_create_shared_folders</code> since Open-Xchange v7.8.1; this restriction was removed in order to allow simple publications also for non-groupware accounts, e.g. as defined by the <code>pim</code> or <code>pim_infostore</code> module access combinations.

####v7.8.0
* Create, update & remove share links: <code>read_create_shared_folders</code>, <code>share_links</code>
* Add, update or remove internal users and group permissions: <code>read_create_shared_folders</code>
* Add, update or remove external guest permissions: <code>read_create_shared_folders</code>, <code>invite_guests</code>

####v7.8.1
* Create, update & remove share links: <code>share_links</code>
* Add, update or remove internal users and group permissions in modules Calendar, Contacts, Tasks: <code>read_create_shared_folders</code> for personal / <code>edit_public_folders</code> for public folders
* Add, update or remove internal users and group permissions in module Drive: none
* Add, update or remove external guest permissions in modules Calendar, Contacts, Tasks: <code>invite_guests</code>, and <code>read_create_shared_folders</code>, <code>read_create_shared_folders</code> for personal / <code>edit_public_folders</code> for public folders
* Add, update or remove external guest permissions in module Drive: <code>invite_guests</code>

#####Administrator Notes:

* Existing shares for a guest user or context may be listed using the commandline utility <code>listshares</code>
* The ability to create share links may be controlled via <code>com.openexchange.capability.share_links</code>, either globally in the configuration file <code>permissions.properties</code>, or on a more fine-granular level through the [Config Cascade](https://oxpedia.org/wiki/index.php?title=ConfigCascade)
* The ability to invite guest users may be controlled via <code>com.openexchange.capability.invite_guests</code>, either globally in the configuration file <code>permissions.properties</code>, or on a more fine-granular level through the [Config Cascade](https://oxpedia.org/wiki/index.php?title=ConfigCascade)
* The number of allowed share links per user may be specified via <code>com.openexchange.quota.share_links</code>, either globally in the configuration file <code>share.properties</code>, or on a more fine-granular level through the [Config Cascade](https://oxpedia.org/wiki/index.php?title=ConfigCascade)
* The number of allowed guest invitations per user may be specified via <code>com.openexchange.quota.invite_guests</code>, either globally in the configuration file <code>share.properties</code>, or on a more fine-granular level through the [Config Cascade](https://oxpedia.org/wiki/index.php?title=ConfigCascade)
* Both quotas can also be set on a per-context basis via a 'changecontext' call and the <code>quota-module</code> and <code>quota-value</code> options, see [change context](http://oxpedia.org/wiki/index.php?title=AppSuite:Context_management#changecontext). The module IDs are accordingly <code>share_links</code> and <code>invite_guests</code>.
* Quotas are always checked per-user, per default both are set to 100. You'll probably want to increase the quota for links when enabling the link mail feature available with OX App Suite 7.8.2.

##Removing Shares

The lifetime of shares is implicitly bound to the lifetime of the associated permission of the guest user entity. So, once a permission entity pointing to a (named or anonymous) guest user account is removed from the parent folder or item, this also leads to the removal of the associated share itself. Afterwards, the contents are no longer accessible for the guest user. For shares that were created with a specific expiry date, it is ensured that they can no longer be accessed via their share link after expiring. Additionally, expired shares are cleaned up periodically within a background task.

#####Administrator Notes:

* Shares may be revoked manually using the commandline utility <code>removeshares</code>
* The interval of the periodic cleanup task can be controlled via <code>com.openexchange.share.cleanup.periodicCleanerInterval</code> in <code>share.properties</code>

##Share Links &amp; Tokens

Shares are accessed with a hyperlink that contains the so-called share &quot;token&quot;. This 24-byte token uniquely identifies the associated guest account on the system, and carries enough randomness that it can't be guessed. Explicitly invited guest users receive this hyperlink in the invitation mail to a share, while in case of an &quot;anonymous&quot; share where just the link itself was generated, it's up to the sharing user to distribute the link on his own. Besides the token, a share link may contain an additional path that points to the concrete folder and item, which just aids to jump to the shared item in the web interface directly. The following shows an example of a share link:

<code>https://share.example.com/ajax/share/48b2b6190151f1bd8b4b610151f0405d9fc8cb89a087f14e/1/2/ODAxMDY</code>

If a guest user has been invited to more than one share in a context (based on his e-mail address), his individual share token remains equal, so that he will have access to all shared contents in the web interface after following any of the share links he received. However, the additional &quot;path&quot; still points to the concrete item. When inviting more than one guest user to the same share, each recipient will get his own individual share link.

Once the share URL is requested from the server, the associated guest account is looked up and, depending of the guest type, the request is redirected to a specific login screen or directly into the App Suite web interface. More details regarding the different login modes are described at [Guest_Login_&_Session_Handling](#Guest Login & Session Handling).

After a share has been revoked (either explicitly, by removing the permission, or if the share is expired), share links can't be accessed any longer, and, after the last share for the guest user was removed, the guest account is removed from the system automatically.

#####Administrator Notes:

* The share token is stored as user attribute <code>com.openexchange.shareBaseToken</code> in the corresponding guest user account
* The target database schema for a share and the associated guest account is extracted from the context identifier encoded in the share token

##Guest Users

As outlined above, guest users are created on demand once something is being shared. We basically distinguish between two types of guest users: Those that were invited explicitly by the sharing user, or &quot;anonymous&quot; guest users that are able to access by visiting the share link. Access for the latter one may optionally be secured with a fixed PIN code.

For both kinds of guest users, a corresponding user account is provisioned dynamically on the system once a new share is created. Such a guest account is handled much similar as an account for a regular user, with the following main exceptions:

* No access to the &quot;Mail&quot; module
* No personal folders
* No access to the &quot;Portal&quot;
* No access to the global address book
* Module access is restricted to only include modules from the actual shares

All those restrictions are configured and enforced using the built-in mechanisms of the Open-Xchange Server, i.e. by a reduced set of capabilities (i.e. module permissions), or by selectively set permission bits in the folder tree for the virtual guest group. This ensures that guest users are only able to access things they explicitly have been invited to, as well as a transparent handling of guest accounts within all subsystems.

#####Administrator Notes:

* Guest users are stored much similar as regular users in the database (tables <code>user</code>, <code>prg_contacts</code>, <code>user_attribute</code>, <code>user_configuration</code>)
* Additionally, the identifier of the user who (initially) created the guest account is stored in <code>user.guestCreatedBy</code>, i.e. if this column is not <code>0</code>, this entry refers to a guest user
* All service calls and APIs that list or search users have been adjusted to be &quot;guest-aware&quot;, i.e. by default, guests users are not included in the output, yet may be included explicitly with additional parameters (namely <code>includeGuests</code> and <code>excludeUsers</code>)
* Service calls and APIs that request data explicitly based on an entity's identifier are also working with guest users, i.e. if a specific idnetifier points to a guest, then the referenced guest data is returned

###Capabilities

Guest users always have the <code>guest</code> capability set. Besides they are generally configured with a limited permission set, that allows them just to work with their shared items. This permission set includes:

|Permission|Capability|Details|
|----------|----------|-------|
|<code>deniedportal</code>| |No <code>portal</code> capability
|<code>editpublicfolders</code>|<code>edit_public_folders</code>||
|<code>readcreatesharedfolders</code>|<code>read_create_shared_folders</code>||
|<code>editpassword</code>|<code>edit_password</code>|Only for invited guests, not links|

Additionally, for every module the guest is having shared items in, the according module permission is granted, e.g. a shared drive folder results in permission <code>infostore</code> and the according capability. Guest users are never allowed to share folders or items on their own, i.e. the capabilities <code>share_links</code> and <code>invite_guests</code> can never be set.

#####Administrator Notes:

This limited capability set can be extended by configuration. Currently three modes are supported:

|Mode|Description|
|-----|------------|
|deny_all|No further capabilities are applied to guest users, except ones that have been explicitly set for the guest user via <code>changeuser --capabilities-to-add</code>.
|static|A static list of capabilities is applied to guest users via the <code>com.openexchange.share.staticGuestCapabilities</code> property. Additionally capabilities that have been explicitly set for the guest user via <code>changeuser --capabilities-to-add</code> are applied.
|inherit|All capabilities of the user who &quot;created&quot; the guest, i.e. created the link or initially invited somebody, are applied to the guest user. Additionally capabilities that have been explicitly set for the guest user via <code>changeuser --capabilities-to-add</code> are applied.|

The mode can be configured via the <code>com.openexchange.share.guestCapabilityMode</code> property in <code>share.properties</code>. This property is config-cascade capable, so it can for example be overridden for certain sets of contexts. The same applies to the <code>com.openexchange.share.staticGuestCapabilities</code> property.

Due to this configuration mechanism it is possible to increase the user experience for guests and even allow some real collaboration. As an '''example''' one could apply the following configuration to allow guests to see preview images of files and edit shared documents with OX Documents:

<pre>com.openexchange.share.guestCapabilityMode = static
com.openexchange.share.staticGuestCapabilities = document_preview, text, spreadsheet, presentation</pre>

###Anonymous Guest Users

If a &quot;share link&quot; is created, this results in an implicit creation of an anonymous guest user account on the server. The &quot;secret&quot; to access the shared contents is the share token itself that is encoded in the generated share link, so that everybody that knows the share link is able to access the shared contents. Optionally, such an anonymous share link may be secured with an additional PIN code. Guest users will be prompted to enter this PIN code when attempting to access the share.

To have a strict separation between different shared contents, each time a folder or item is shared using the &quot;Get a link&quot; method, a designated anonymous guest account for this share is used. Consequently, each time such an anonymous share is revoked, this guest account is terminated again with no further delay. Additionally, such an anonymous guest entity can only be applied to the permission set of the folder or item the original link was created for, i.e. it's not possible to add more shared contents to an anonymous guest - in contrast to an invited, named guest user.

The displayed folder hirarchy is also modified to hide all folder information that the anonymous user should not have access to. The parent folder of the shared folder will always be the next highest default or system folder in the folder tree. This way any folders in between are hidden.

Besides the common restrictions for guest accounts outlined above, the following applies for anonymous guest user accounts:

* No e-mail address or display name
* No password, if no PIN was assigned by the sharing user
* A password that may only be changed by editing the link, if a PIN code was set
* Anonymous guest users may only receive &quot;read-only&quot; access permissions to the shared item
* Optionally, an expiry date can be applied for an anonymous guest user after which the share link is no longer accessible

#####Administrator Notes:

* The PIN code for anonymous guest users is stored using symmetrical encryption in the database, therefore, an encryption key needs to be specified via the property <code>com.openexchange.share.cryptKey</code> in <code>share.properties</code>
* The folders with modified parent and subfolder ids are not stored in any cache

###Named Guest Users

Internal users are able to invite a guest user to a folder or item explicitly by specifying the e-mail address of the recipient. Such &quot;named&quot; guest users are internally stored as individual guest users, identified by their e-mail address.

If data is shared for the first time to the recipient in the context, a new guest user account is provisioned and an initial set of user permissions and capabilities is assigned. In case there are already shares in different contexts to the same recipient (based on his e-mail address), some existing user data like a display name or an assigned password is copied over if a cross-context database is available on the system.

If the recipient has already been invited from the same or another internal user in the context to another share before, the new share is added to the guest user in a way that the underlying folder- and object permissions are taken over, and the user capabilities getting expanded as needed to cover all modules the shares are located in. Similarly, if a share to a named guest user is revoked and the underlying folder- and object-permissions are removed, the guest user capabilities are updated implicitly to reflect the modules of the remaining shares.

After the last share to a named guest user has been revoked, the user has no longer access to any data. The account itself gets removed from the context automatically after a configurable expiry time. Additionally, any data that is stored for the guest user in the cross-context database is removed once the guest user has been deleted from all contexts in the system.

In contrast to an &quot;anonymous&quot; guest user, a named guest user has access to all shared items from a context after logging in, since the permissions get added to an existing guest user account automatically. For entering the web interface, he may use any of the share links that were sent to him in the different notification messages. Those links usually point to an individual share target like a folder or file, but the guest user may navigate to the other shared contents using the folder tree of the web interface in the same way as regular groupware users do. Similarly, if the guest user has access to shares from different modules, the modules can be switched in the web interface as usual.

#####Administrator Notes:

* The timespan after which an unused named guest user should be removed from the system can be configured via <code>com.openexchange.share.cleanup.guestExpiry</code> in <code>share.properties</code> - this value may also be set to <code>0</code> to force an immediate removal
* For the removal of no longer needed guest user accounts, a periodical cleanup task is scheduled based on the interval of <code>com.openexchange.share.cleanup.periodicCleanerInterval</code>
* Whether a cross-context database is considered for guest users may be configured via <code>com.openexchange.share.crossContextGuests</code>

##Guest Login & Session Handling<a name="Guest Login & Session Handling"></a>

Based on the underlying guest user account, different login operations with different authentication workflows are possible.

###Authentication

We have basically three different authentication options for guest users accessing a share, each of them having their own characteristics.

####Anonymous

* Access is granted without providing additional authentication information, the knowledge of the link is sufficient
* When accessing the share link, a guest session is spawned implicitly
* Initially supplied cookies are considered to recycle an existing session
* The login screen is skipped, we'll redirect to the module/folder/item directly (using appropriate URL fragments)

####Anonymous with PIN

* Access is granted for anonymous guest users providing a password / PIN code
* When accessing the share link, the client is redirected to the login screen of the webinterface, using <code>login_type=anonymous</code>
* User can then enter his PIN code, client executes the <code>anonymous_login</code> method, server authenticates, sends back a login response containing the target in the app suite webinterface (module/folder/item)
* Password can't be changed by an anonymous user
* Password can be re-constructed / changed by sharing user

####Guest without Password

* Access is granted without providing additional authentication information, the knowledge of the guest's individual link is sufficient
* When accessing the share link, a guest session is spawned implicitly
* Exiting cookies are considered to recycle an existing session
* The login screen is skipped, we'll redirect to the module/folder/item directly (using appropriate URL fragments)
* Guest user may choose an individual password at a later stage

####Guest with Password

* Access is granted for guest users providing a user name and password.
* Much similar to a regular groupware user
* When accessing the share link, the client is redirected to the login screen of the webinterface, using <code>login_type=guest</code> and <code>login_name=&lt;NAME&gt;</code>
* The login name is used to pre-fill the username input
* User can then enter his password, client executes the <code>guest_login</code> method, server authenticates, sends back a login response containing the target in the app suite webinterface (module/folder/item)
* Password can be changed by guest user
* Guest user may reset his password if he can't remember

###Guest Hostname

For serving shares, a separate guest hostname needs to be configured. This is mainly required to prevent guest- and regular user sessions using the same cookie container when logged in in the same client (otherwise, the cookie holding the alternative session identifier as well as other cookies would get overwritten concurrently). Additionally, this allows to have separate entry points to the web client for guest- and regular users. 

The hostname for guests is used when generating external share links, as well as at other locations where hyperlinks are constructed in the context of guest users. Usually, the guest hostname refers to a separate subdomain of the installation like <code>share.example.com</code>, and is defined as an additional named virtual host pointing to the web client's document root in the webserver's configuration. 

Once the webserver configuration is done and the web client is accessible using the guest hostname, this hostname needs to be specified in the backend configuration, too. In simple scenarios, where a fixed guest hostname should be used for the installation, this can be done statically in a configuration file. This setting may also be overridden per context via the Config Cascade. In case a dedicated hostname service is installed (for example <code>open-xchange-hostname-ldap</code>), this hostname service is also supposed to supply the guest hostname. 

#####Administrator Notes:

* The guest hostname may be specified via <code>com.openexchange.share.guestHostname</code>, either globally in the configuration file <code>share.properties</code>, or on a more fine-granular level through the [Config Cascade](https://oxpedia.org/wiki/index.php?title=ConfigCascade)
* The guest hostname may also be supplied via dedicated hostname services like <code>open-xchange-hostname-config-cascade</code> or <code>open-xchange-hostname-ldap</code>
* For test purposes, guests may also access the web interface using the same host as regular users do, however, this might lead to unexpected results (missing images, sessions timing out, auto-login malfunction...)

###Cookies

Guest sessions basically make use of the same cookies as regular user sessions do. This includes the JSESSONID cookie for the JVM route, as well as the <code>open-xchange-secret-&lt;hash&gt;</code> and <code>open-xchange-public-session-&lt;hash&gt;</code> cookies. Additionally, if configured, the client may also issue a <code>store</code> request to persist the open-xchange-session-<hash> cookie. This cookie may then be used to auto-login the guest client into the previously used session if it is still valid.

Besides the common cookies, another special cookie is set: <code>open-xchange-share-&lt;hash&gt;</code>. The value contains the unique share token bound to the guest user accessing the share. here, the cookie hash is calculated as it's done for ordinary sessions, so that there can only be one <code>open-xchange-share-&lt;hash&gt;</code> cookie in a client at the same time. Whenever an auto-login request is issued by the client, the server checks for the existence of this &quot;share&quot; cookie, and, once recognized and checked for validity, it will try to perform the auto-login for an existing guest session first, i.e. using the session cookie based on the special guest hash calculation outlined above. Otherwise, the common auto-login process takes place. The &quot;share&quot; cookie is removed once the guest session terminates, i.e. the guest user logs out.

Since guest users access the web interface on a separate (sub)domain (see [[#Guest_Hostname|Guest Hostname]] above for details), guest session cookies won't interfere with cookies of a regular session on the same client. This allows to use the regular user session as well as one or more guest sessions in parallel - e.g. if the sharing user quickly wants to check how the contents appear for the guest user after generating a share link.

#####Administrator Notes:

* Whether guest sessions are enabled for auto-login is configurable via the property <code>com.openexchange.share.autoLogin</code> in <code>share.properties</code>
* By default, the cookie TTL for guest sessions is inherited from the TTL for cookies of regular sessions as defined by <code>com.openexchange.cookie.ttl</code> - this default may be overridden by defining a timespan at <code>com.openexchange.share.cookieTTL</code>

###Login Modes

When accessing a share link, one of the following login modes is triggered to acquire a session and forward the client to the share target. The executed login operation and redirect depends on the authentication mode of underlying guest account, the share target iteself, and the client accessing the share.

####Redirect to Target

In case a share is accessible without providing credentials, the client is redirected to the share target directly, i.e. without prompting for a username or password. By default, the client is redirected to the target in the App Suite web interface by responding the <code>GET</code> request to the share link with <code>HTTP 302</code>, and a location header like the following:

<code>Location: /appsuite/ui#!&amp;session=80c711019d6f48b5bec9cd82758e3308&amp;store=true&amp;user=&amp;user_id=642&amp;context_id=1&amp;m=files&amp;f=41042</code>

The session for the guest user is created implicitly in the backend after checking the share link's validity, and the client is instructed to store appropriate cookies in the redirect response, including the &quot;share&quot; cookie:

<code>Set-Cookie: open-xchange-secret-aNobP2G9wLHJ6sMr7vtTA=38ee770d6e4f42ab8366d91db3279931; Expires=Thu, 13-Aug-2015 06:16:26 GMT; Path=/; Secure; HttpOnly</code><br />
<code>Set-Cookie: open-xchange-public-session-d0759656127fb7cee6e0fe8bb5fe19f9=cae6a3e712ac429e9da9194abd389cb3; Expires=Thu, 13-Aug-2015 06:16:26 GMT; Path=/; Secure; HttpOnly</code><br />
<code>Set-Cookie: open-xchange-share-b7gDSqJpnh9gS3Fs52I65Q=0ad50ac00418fbcdad50ac1418f94fb181d51b8fa7b2bde3; Expires=Thu, 13-Aug-2015 06:16:26 GMT; Path=/; Secure; HttpOnly</code>

####Redirect to Login Screen

If additional credentials, i.e. an additional PIN code or username/password combination, are required to access a share target, and no &quot;special client&quot; like an iCal consumer is detected by the backend, the client is redirected to the login screen of the app suite webinterface. The GET request to the share link is answered with statuscode HTTP 302, and a location header depending on the required credentials to access the share.

If the share ought to be accessed anonymously, but protected by a PIN code, a location like the following is added to the response header:

<code>Location: /appsuite/ui#!&amp;share=08b4b6110151f1bd7d4b610151f0405d9fc8bb89a887f04e&amp;login_type=anonymous&amp;message_type=INFO&amp;message=Tony%20Parker%20has%20shared%20the%20folder%20%22Pictures%22%20with%20you.%20Please%20log%20in%20to%20view%20it.%20&amp;target=151ebb38</code>

For shares to dedicated guest users identified by their e-mail address, the redirect location looks like follows:

<code>Location: /appsuite/ui#!&amp;share=4ac9eb590f9ca4d2ac9eb58f9ca611ec9b4f4638d288c8c0&amp;login_type=guest&amp;message_type=INFO&amp;message=Tony%20Parker%20has%20shared%20the%20file%20%22Agenda.pdf%22%20with%20you.%20Please%20log%20in%20to%20view%20it.%20&amp;login_name=ray%40example.com&amp;target=4444cbc7</code>

The redirect response already contains the <code>Set-Cookie</code> header for the JVM route. On the redirect target, the client should request the PIN code or password from the user, and then issue a special login request, supplying the share token and optional target from the URL parameters, and the password as URL encoded form data in the request body, similar to the usual login request via POST. After successful authentication, the login response includes, along with the common login response properties like the session identifier, information about the share target being accessed:

<code>{&quot;session&quot;:&quot;b89af2c2ce494ce4b4573c0632b48e89&quot;,&quot;user&quot;:&quot;ray@example.com&quot;,&quot;user_id&quot;:660,&quot;context_id&quot;:1,&quot;locale&quot;:&quot;en_US&quot;,&quot;module&quot;:&quot;files&quot;,&quot;folder&quot;:&quot;10&quot;,&quot;item&quot;:&quot;10/456398&quot;, ... }</code>

Additionally, the client is instructed to store the secret cookies:

<code>Set-Cookie: open-xchange-share-b7gDSqJpnh9gS3Fs52I65Q=0ac9eb590f9ca4d5ac9eb58f9ca641ec9b4f4638d288c8a0; Expires=Thu, 13-Aug-2015 06:31:21 GMT; Path=/; Secure; HttpOnly</code><br />
<code>Set-Cookie: open-xchange-secret-MBIRg9bJBLduCcosqQBCw=70187de16f844be6880c18be373b953d; Expires=Thu, 13-Aug-2015 06:31:21 GMT; Path=/; Secure; HttpOnly</code><br />
<code>Set-Cookie: open-xchange-public-session-d0759656127fb7cee6e0fe8bb5fe19f9=4e797a59758a4dd7b763912472ccf26d; Expires=Thu, 13-Aug-2015 06:31:21 GMT; Path=/; Secure; HttpOnly</code>

Afterwards, the client is able to use the session to access the share target as usual.

###Session Lifecycle

Generally, guest sessions on the server are treated just like the sessions of ordinary users. Especially, guest sessions are also held in the local session containers of the backend host they're associated with. However, by default guest sessions are marked as <code>transient</code>, i.e. they are not moved to the long-term session containers, nor they are put into the distributed session storage.

If OX Documents functionality is used for guest users in a cluster of application servers, this setting needs to be adjusted to <code>false</code> in order to also have guest sessions available in the distributed storage.

#####Administrator Notes:

* Guest sessions are also accounted in the monitoring outputs (e.g. in the sessions per container graphs)
* The <code>transient</code> handling of guest sessions may be changed via the property <code>com.openexchange.share.transientSessions</code> in <code>share.properties</code>

###Logout

Guest sessions are terminated once a logout request is issued by the client, i.e. the user clicks the &quot;Logout&quot; button in the web interface, just like it is done for regular sessions. Additionally, guest sessions expire in the backend when not being used for a while, the actual timeout depends on the configured default session lifetime and whether they are treated as &quot;transient&quot; or not, as explained above.

Since guest users are not able to use the default login page for regular users, a custom logout location for guest users should be specified where guest users are taken to after clicking logout explicitly, or if their session expired.

If a share is consumed &quot;directly&quot;, e.g. by downloading the binary contents of a file share directly (see [Consuming Shares](#consuming shares) for details), the guest sessions is terminated instantly after serving the request.

#####Administrator Notes:

* The logout location for guest accounts can be customized via <code>guestLogoutLocation</code> in the file <code>as-config.yml</code> (see file <code>as-config-default.yml</code> for an example)

##Share Notifications

With the new sharing concept, notification mails can be sent out to the permission entities (i.e. internal or guest users) of folders or items. Mechanisms exist to send out such mails implicitly or explicitly. Notifications are sent out implicitly, if externals are invited as guests and can also be sent out for internal invitations, if configured so. The client (e.g. App Suite UI) decides on its own whether implicit notifications shall be sent when updating a folders or items permissions. Besides there are separate API calls for sending out notification messages explicitly. Its on the client to provide this functionality to its users. This makes it possible to re-send a link to a folder or item to an existing permission entity.

Sending out links to shared folders and items is not the only case for notification messages, it can also be necessary to send out system notifications to guest users. Currently this is the case when a guest user secured his account with a password and needs to reset that password, because he cannot remember.

#####Administrator Notes:

* A special transport must be configured for system notifications and cases where the sharing user has no configured webmail account. This transport is configured in <code>noreply.properties</code>. All properties therein are config-cascade capable, so their values can be sensitive to the current user or context.
* It is possible to disable the implicit notification of internal users about shared folders or items at all by setting <code>com.openexchange.share.notifyInternal</code> in <code>share.properties</code> to <code>false</code>.
* The layout of notifications mails can be changed via <code>as-config.yml</code>. All available properties are defined and explained in <code>as-config-defaults.yml</code>.

##API Access

From a client's point of view, guest users basically don't differ from regular users, although they usually have limited capabilities, for example no mail access or no personal folders. However, all those differences are reflected within the regular permission- and capability-concepts, so that existing clients, once the guest user is authenticated and has a valid session, continue to work transparently, and use the same API calls as with a regular groupware user.

To create or manage shares and guest users, the HTTP API has been extended at various locations. The following list gives an overview about the changes, derived from the corresponding software change requests.

###Format change for object identifiers of the default &quot;infostore&quot; account

As preparation for individual object permissions where a file can be accessed from different folder &quot;views&quot;, the object IDs for documents in the default &quot;infostore&quot; file storage account will get enhanced with the prefixing folder ID.

The identifiers will now be of format <code>&lt;some numbers&gt;/&lt;more numbers&gt;</code>. Object identifiers are already of type <code>String</code>, so this change should usually be transparent to clients. However, there may be some clever clients out there that for example tried to interpret the string of numerical characters as number, so client developers should double-check their implementation for compatibility. They most likely would run into trouble when coping with non-infostore file storages anyway.

###Object permissions for files

In order to define permissions on object-level, a new property <code>object_permissions</code> for objects of type <code>infoitem</code> is introduced. Each time the underlying folder permissions are not sufficient to access an item, those object permissions are taken into account. Object permissions are stored as an array of Object Permission objects as defined below within the detailed infoitem data, the column ID is <code>108</code>.

Details about the JSON structure are available at:

* [Detailed Infoitem Data](https://documentation.open-xchange.com/components/middleware/http/latest/index.html#detailed-infoitem-data)
* [Object Permission Object](https://documentation.open-xchange.com/components/middleware/http/7.10.0/index.html#object-permission-object)
* [http://oxpedia.org/index.php?title=HTTP_API#ObjectPermissionFlags HTTP API: Object Permission Flags](http://oxpedia.org/index.php?title=HTTP_API#ObjectPermissionFlags)

###New field for &quot;user&quot; data: &quot;guest_created_by&quot;

A new property has been introduced for users that needs to be exposed in our HTTP API, too. The following property is added to the detailed user data object:

* ID: 616
* Name: guest_created_by
* Type: Number
* Value: Contains the ID of the user who has created this guest in case this user represents a guest user; it is 0 for regular users

The property is read-only and can't be removed or set by clients.

See also:

* [Detailed User Data](https://documentation.open-xchange.com/components/middleware/http/7.10.0/index.html#detailed-user-data)

###Extend folder- and object permissions for addressing external guests

For sharing files- or folders to external guests, the folder- and object permission objects are extended with additional properties. Those extended properties can be set during creation or update of the parent folder or file. The underlying shares and guest user entities for the referenced recipients are created automatically along with folder/file creation/update. Afterwards, the external recipients appear as regular &quot;user&quot; entities in the permission arrays in subsequent &quot;get&quot; requests.

Details about the extended JSON structure are available at:

* [Permission Object](https://documentation.open-xchange.com/components/middleware/http/7.10.0/index.html#permission-object)
* [Object Permission Object](https://documentation.open-xchange.com/components/middleware/http/7.10.0/index.html#object-permission-object)

###New Ajax module: share/management

To work with shares, a new Ajax module is introduced.

The available actions in the module are described at:

* [Share Management](https://documentation.open-xchange.com/components/middleware/http/latest/index.html#/Share/Management)

###New column &quot;shareable&quot; in detailed infoitem data

Clients want to know quickly if an infostore item is shareable or not. A new (read-only) property named <code>shareable</code> of type Boolean with column identifier <code>109</code> is introduced for &quot;detailed infoitem data&quot;. If &quot;true&quot;, the can be considered as shareable, i.e. the item's object permissions may be adjusted by the user.

Further details are available at:

* [Detailed Infoitem Data](https://documentation.open-xchange.com/components/middleware/http/latest/index.html#detailed-infoitem-data)

###New action &quot;shares&quot; in module folder

To provide an overview of all folders of a certain modules that are shared to others, a new <code>shares</code> action is added to the Ajax module <code>folders</code>. It returns all personal folders of a certain module that are shared to other entities.

Further details are available at:

* [Get shared folders](https://documentation.open-xchange.com/components/middleware/http/latest/index.html#!/Folders/getSharedFolders)

###New action &quot;shares&quot; in module infostore

To provide an overview of all files that are shared to others, a new <code>shares</code> action is added to the Ajax module <code>infostore</code>. It returns all personal files that are shared to other entities.

Further details are available at:

* [Get shared infoitems](https://documentation.open-xchange.com/components/middleware/http/latest/index.html#!/Infostore/getSharedInfoItems)

###New fields to retrieve extended permissions of files and folders

Clients would like to have more details about permission entities folders directly. A new read-only property named <code>com.openexchange.share.extendedPermissions</code> is introduced for &quot;Detailed folder data&quot;, with column identifier <code>3060</code>. It basically contains the same as the regular <code>permissions</code> array, yet enhanced by resolved information about the user, group or guest entities as well as additional, sharing-related properties.

Similarly, a new read-only property named <code>com.openexchange.share.extendedObjectPermissions</code> is introduced for &quot;Detailed infoitem data&quot;, with column identifier <code>7010</code>. It basically contains the same as the regular <code>object_permissions</code> array, yet enhanced by resolved information about the user, group or guest entities as well as additional, sharing-related properties.

Further information about the JSON structure is available at:

* [Extended Permission Object](https://documentation.open-xchange.com/components/middleware/http/latest/index.html#extended-permission-object)
* [Extended Object Permission Object](https://documentation.open-xchange.com/components/middleware/http/latest/index.html#extended-object-permission-object)

##Consuming Shares<a name="consuming shares"></a>


Depending on the shared contents and the requesting user agent, shares may be consumed in a couple of different ways. The concrete response to a request to the share URL is evaluated by the share servlet in the backend.

###App Suite

The default handling for all shares is forwarding them to the App Suite web interface, where the shared contents are made available through the existing client. Based on the underlying guest account, the client is either forwarded to the login prompt, or taken directly to the share target if no credentials need to be provided. This process is described in more detail at [Guest_Login_&_Session_Handling](#Guest Login & Session Handling).

###Direct Download

Shares to a single file may also be downloaded directly by clients, without opening them in the web interface first. This is indicated by an additional parameter appended to the plain share link, and can be specified in the following ways:

* Append <code>dl</code> parameter:<br />
 <code>https://ox.example.com/ajax/share/48b2b6190151f1bd8b4b610151f0405d9fc8cb89a087f14e/151eab38?dl=true</code>
* Specify <code>delivery</code> parameter:<br />
 <code>https://ox.example.com/ajax/share/48b2b6190151f1bd8b4b610151f0405d9fc8cb89a087f14e/151eab38?delivery=download</code>

If accessing the item requires authentication, an unauthenticated request is responded with <code>HTTP 401 Unauthorized</code>. The client then has to provide the correct credentials to access the share via basic authentication. If there's no dedicated username for the underlying guest account - i.e. an &quot;anonymous&quot; share link protected with a PIN code is accessed - only the password is checked, i.e. the client may then supply an arbitrary username in the basic authentication header like &quot;Guest&quot;.

###Get iCal

Shares to a single calendar- or task-folder may also be downloaded directly by clients as iCal files, without opening them in the web interface first. This standard format allows to consume event data directly using various calendaring clients, which often can be configured to subscribe an external calendar source.

Once a share link to a calendar- or task-folder is requested by the client, the <code>Accept</code>- and <code>User-Agent</code> headers of the request are evaluated. If the <code>Accept</code> header is either set to <code>text/calendar</code> or <code>text/iCal</code>, or if the <code>User-Agent</code> header denotes a well-known client like Microsoft Outlook or Mozilla Thunderbird w/ Lightning, the contents of the shared folder are converted to an iCal file that is directly written back in the response.

To force the iCal output, an additional parameter may be appended to the plain share link:

* Append <code>ical</code> parameter:<br />
 <code>https://ox.example.com/ajax/share/48b2b6190151f1bd8b4b610151f0405d9fc8cb89a087f14e/151eab38?ical=true</code>

If accessing the item requires authentication, an unauthenticated request is responded with <code>HTTP 401 Unauthorized</code>. The client then has to provide the correct credentials to access the share via basic authentication. If there's no dedicated username for the underlying guest account - i.e. an &quot;anonymous&quot; share link protected with a PIN code is accessed - only the password is checked, i.e. the client may then supply an arbitrary username in the basic authentication header like &quot;Guest&quot;.

#####Administrator Notes:

* The interval of task- and appointment data considered for conversion to iCal can be adjusted via <code>com.openexchange.share.handler.iCal.futureInterval</code> and <code>com.openexchange.share.handler.iCal.pastInterval</code> in configuration file <code>share.properties</code>

##Cross-context functionality

As already mentioned in previous sections the administrator is able to configure if guests should be handled per context (default) or server wide by using the configuration parameter <code>com.openexchange.share.crossContextGuests</code>.

If set to <code>true</code> the guests email address is used to recognize if there is already a registered user with the given address and aligns the stored password to the already existing guest user. In addition to the password (which is the most important parameter this feature is about) even the users contact data gets synchronized.

#####Administrator Notes:

* To handle user and contact data across contexts boundaries the feature has to be enabled before a guest receives the first share. Guests that receive shares before the activation cannot be considered within the alignment process. Only latter shares will be considered.
* At the moment this feature does only sync user and contact related data (no shared content). If the user got two shares from different contexts he will only see shares related to the given link.

##Publish/Subscribe vs. Sharing

The upcoming sharing features are going to replace the previously used OXMF &quot;publications&quot;, allowing guest users to interact with the shared data in the same way as regular groupware users do. However, since the underlying concepts and their technical realization are completely different, a seamless migration between publications and shares is not possible without some drawbacks.

The following list gives an overview of the main discrepancies:

* Custom templates for OXMF publication targets<br />An adminsitrator/admin may have defined some custom publication targets that are using the published data in a special way. While shares would still make all the data available (mainly via the web interface), this would only be a drop-in replacement for the ordinary &quot;view the publication in a browser&quot; use case, but not for anything beyond that scope.
* Subscribe of publications<br />Publications from one user can be added to another user's groupware using the &quot;subscribe&quot; functionality, making use of the embedded microformat data of publications (OXMF). For sharing, we will not have a similar feature in the first iteration, so migrating an existing publication to a share would also stop it from being subscribable.
* Deep links to download files of publications<br />Files behind an infostore publication were accessible behind a static URL, which would theoretically allow them to be requested independently of the parent publication (e.g. images linked from an external website). While the entry URL to a publication would be mappable to a corresponding share URL, converting existing publications to shares would at least break such deep links.

Because of the above points and the whole different concept, we do not migrate existing publications to shares. Instead, the default behavior will be:

* No new OXMF publications or subscriptions can be created by default
* The web client does no longer give the option to publish or subscribe in the OXMF format
* Existing OXMF publications / subscriptions can't be updated
* Existing OXMF publications continue to work as is, including associated subscriptions
* Yet it's still possible to delete existing publications and subscriptions
* Therefore, the menu section &quot;Publications and Subscriptions&quot; will still be available (if there's at least one publication or subscription)

Exceptions to these rules cover special internal subscriptions to 3rd party services like addressbooks from LinkedIn (removed since 7.10.0) or Xing, as well as the auto-publish feature of mail attachments exceeding a specific size.

####Administrator Notes:

* The possibility to create/update OXMF publications via HTTP-API may be configured via <code>com.openexchange.publish.createModifyEnabled</code> in file <code>publications.properties</code>
* The possibility to create/update OXMF subscriptions via HTTP-API may be configured via <code>com.openexchange.subscribe.microformats.createModifyEnabled</code> in file <code>microformatSubscription.properties</code>

##Limit file accesses for named/anonymous guests (since 7.8.2)

As links to shares might be shared without knowing the audience we introduced a mechanism to prevent abuse.

Therefor the possibility to define size and/or count limits for named/anonymous guests has been introduced. As internal users can be considered as reliable it is not possible to define limits for them. 

For named and anonymous guests there are two kinds of limits which apply to a defined time frame. The time frame acts as a sliding time window which means that based on the current request all earlier requests matching the time frame are cumulated and evaluated if one of the two limits is exceeded. If so, the request will be answered with an exception.

The two limits mentioned above are:

* size limits: how many content (in bytes) should the guest be allowed to download (within the defined time frame).
* count limits: how often should the guest be allowed to download a file or folder (within the defined time frame). 

Named and anonymos guests can have different limits. Limits are valid for downloads of files and folders. Preview of images is not considered within the limit.


#####Administrator Notes:

* The anti-abuse mechanism will be available after updating the package open-xchange-core to release 7.8.2
* Additional parameters are located within the configuration file <code>share.properties</code>
* Per default the feature is disabled
* To enable the feature you have to set <code>com.openexchange.share.servlet.limit.enabled</code> to <code>true</code>. If not enabled all additional checks will be skipped. If you would like to set limits via config cascade those will only be checked if the feature itself is enabled.
* To be more flexible the administrator is able to overwrite limits by using the config cascade. As guests do not have fixed user identifiers only the 'context' level scope is supported.

After the feature is enabled you have the ability to configure fine-grained accesses via: 

* <code>com.openexchange.share.servlet.limit.timeFrame.guests</code>: sliding time frame (in milliseconds) the limits are valid for named guests. Setting to 0 will disable the check for named guests (if not overwritten via config cascade).
* <code>com.openexchange.share.servlet.limit.timeFrame.links</code>: sliding time frame (in milliseconds) the limits are valid for anonymous guests. Setting to 0 will disable the check for anonymous guests (if not overwritten via config cascade).
* <code>com.openexchange.share.servlet.limit.size.guests</code>: the limit in bytes for named guests (valid for aboves time frame). Setting to 0 will disable the size check for named guests (if not overwritten via config cascade).
* <code>com.openexchange.share.servlet.limit.size.links</code>: the limit in bytes for anonymous guests (valid for aboves time frame). Setting to 0 will disable the size check for anonymous guests (if not overwritten via config cascade).
* <code>com.openexchange.share.servlet.limit.count.guests</code>: the limit for a number of downloads (valid for aboves time frame) for named guests. Setting to 0 will disable the count check for named guests (if not overwritten via config cascade).
* <code>com.openexchange.share.servlet.limit.count.links</code>: the limit for a number of downloads (valid for aboves time frame) for anonymous guests. Setting to 0 will disable the count check for anonymous guests (if not overwritten via config cascade).

###Example configurations

A mixed configuration is possible. Have a look at the following examples assuming that <code>com.openexchange.share.servlet.limit.enabled</code> is set to <code>true</code>.

The following configuration only checks the count limit for anonymous guests. Named guests and the limit for download sizes will not be checked

* <code>com.openexchange.share.servlet.limit.timeFrame.guests:0</code> (# disabled)
* <code>com.openexchange.share.servlet.limit.timeFrame.links:3600000</code> (# 60 minutes)
* <code>com.openexchange.share.servlet.limit.size.guests:0</code> (# not considered as disabled)
* <code>com.openexchange.share.servlet.limit.size.links:0</code> (# size for 60 minutes not checked)
* <code>com.openexchange.share.servlet.limit.count.guests:0</code> (# not considered as disabled)
* <code>com.openexchange.share.servlet.limit.count.links:100</code> (# 100 downloads within 60 minutes)

The following configuration will check the count limit for named guests and the size limit for anonymous guests (both within the last 60 minutes).

* <code>com.openexchange.share.servlet.limit.timeFrame.guests:3600000</code> (# 60 minutes)
* <code>com.openexchange.share.servlet.limit.timeFrame.links:3600000</code> (# 60 minutes)
* <code>com.openexchange.share.servlet.limit.size.guests:0</code> (# disabled)
* <code>com.openexchange.share.servlet.limit.size.links:1073741824</code> (# 1 GB)
* <code>com.openexchange.share.servlet.limit.count.guests:1000</code> (# 1000 downloads within 60 minutes)
* <code>com.openexchange.share.servlet.limit.count.links:0</code> (# disabled)

It is possible to reload an adapted configuration by using reloadconfiguration command line tool.

