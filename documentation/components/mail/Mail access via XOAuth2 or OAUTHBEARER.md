---
title: Mail access via XOAuth2 or OAUTHBEARER
---

The OX middleware supports the authentication against mailservers via XOAuth2 or OAUTHBEARER. This document describes how it can be used.

# External mail accounts

External mail accounts are working right out the box. To create an external mail account with oauth authentication a functional oauth account with the ox "Mail" scope is needed.
If an account is available the client only needs to announce the oauth account which should be used for the mail account during creation. It can be done by setting the following fields:

```
{
...
"mail_oauth" = <oauth account id>
"transport_auth" = "custom"
"transport_oauth" = <oauth account id>
...
}
```

Whether XOAuth2 or OAUTHBEARER is used depends on the oauth provider. For example the google oauth provider (gmail) is using XOAuth2. Others may use OAUTHBEARER instead.


# Primary mail account

In order to use oauth authentication for the primary mail account you need to configure the following properties:

```
com.openexchange.mail.authType = <either "xoauth2" or "oauthbearer">
com.openexchange.mail.transport.authType = <either "xoauth2" or "oauthbearer">
```
If those properties are configured the ox middleware tries to connect to the primary mail account via oauth.
In order to do this it uses the session property `com.openexchange.saml.AccessToken` which needs to contain a valid oauth access token.

Therefore the session needs to be enhanced with this token during the authentication process.
