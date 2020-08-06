---
title: Mail Access via XOAUTH2 or OAUTHBEARER
icon: fas fa-id-badge
tags: Mail, Configuration, OAuth
---

The OX middleware supports authentication against IMAP, SMTP and ManageSieve servers using `SASL=XOAUTH2` or `SASL=OAUTHBEARER` mechanisms. This document describes how it can be used.


# Primary mail account

## Prerequisites

* Obviously the mail server needs to support it. Dovecot does so by a specific [passdb implementation](https://doc.dovecot.org/configuration_manual/authentication/oauth2).
* Your App Suite authentication plugin must provide OAuth Bearer tokens during sign-in and these must be stored within user sessions. Best out-of-the-box support is granted by using [OpenID Connect 1.0 Single-Sign-On](../login_and_sessions/openid_connect_1.0_sso.html).
* For non-web clients accessing App Suite HTTP API, a secondary mechanism to obtain OAuth tokens for username/password login must be provided. Again, [OpenID Connect 1.0 Single-Sign-On](../login_and_sessions/openid_connect_1.0_sso.html) can provide that, if the IDM/AM system supports the "Resource Owner Password Credentials Flow". Alternatively, an authentication plugin based on plain OAuth 2.0 and that grant flow [also exists](../login_and_sessions/oauth_password_grant.html).
* The mail server must accept the tokens that are issued to App Suite during sign-in. Therefore ensure scope, audience, etc.
* If you have a choice, make a decision whether to use [XOAUTH2 (Google Proprietary)](https://developers.google.com/gmail/imap/xoauth2-protocol) or [OAUTHBEARER (RFC 7628)](https://tools.ietf.org/html/rfc7628). They are basically technically equivalent, while `OAUTHBEARER` requires larger SASL responses but therefore being an internet standard.


## Configuration

### IMAP and SMTP

Adjust `/opt/open-xchange/etc/mail.properties`:

```
com.openexchange.mail.authType = <either "xoauth2" or "oauthbearer">
com.openexchange.mail.transport.authType = <either "xoauth2" or "oauthbearer">
```

**Like for (master) password authentication, it is important to have `com.openexchange.mail.loginSource` set properly! OAuth 2.0 for email always requires a combination of username and token.**

However, `com.openexchange.mail.passwordSource` does not play a role when `authType` refers to an OAuth type.


### ManageSieve

Adjust `/opt/open-xchange/etc/mailfilter.properties`:

```
com.openexchange.mail.filter.passwordSource = session
com.openexchange.mail.filter.preferredSaslMech = <either "XOAUTH2" or "OAUTHBEARER">
```

**Like for IMAP/SMTP, ensure that `com.openexchange.mail.filter.credentialSource` refers to a source that picks the right username at runtime!**


## Internal Mechanics

With the described configuration, Middleware will check IMAP/SMTP/ManageSieve pre-authentication capabilities to contain the according SASL mechanism. It will then obtain the access token from the current user session (parameter `__session.oauth.access`) and create and send the according SASL response.

On every HTTP API request the session access token is checked for validity, based on an expiry date (also set as session parameter: `__session.oauth.access.expiry`) and usually some leap time to eagerly refresh the token.

If the token is expired or to-be-expired in shorter than the eager refresh time, the token gets refreshed. Per default, using the OpenID Connect 1.0 integration or the OAuth 2.0 Authentication Service, it looks for a refresh token stored as session parameter `__session.oauth.refresh` and performs a refresh grant flow against the configured token endpoint.

If no refresh token was issued/stored in the session or token refresh fails with a permanent error, e.g. `invalid_grant`, the App Suite session is terminated and the user forced to sign-in again.


# External mail accounts

Currently only GMail is supported using `XOAUTH2` for IMAP. As a prerequisite, Google OAuth must be setup according to [this guide](../3rd_party_integrations/google.html). The application needs to have scope `https://mail.google.com/` applied. Please note that this usually requires to go through an expensive security audit, as Google claims this to be a "restricted scope".
