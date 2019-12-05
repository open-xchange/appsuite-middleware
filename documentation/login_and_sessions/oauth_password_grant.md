---
title: OAuth Password Grant Authentication Plugin
icon: fa fa-plug
tags: OpenId, SSO, Configuration
---

# Introduction

User authentication at OX App Suite web UI or APIs can be handled using the [OAuth 2.0 Resource Owner Password Credentials Grant](https://tools.ietf.org/html/rfc6749#section-4.3). In this case, username and password are sent to an OAuth Authorization Server, which returns a token pair on successful authentication. The token pair is then stored within the App Suite session and can for example be used to authenticate at the email backend.

**Important:** 

  * If OpenID Connect SSO is used, this plugin must not be used. Instead `open-xchange-oidc` offers the same mechanism as configurable fallback for non-web clients.
  * Internally, this feature registers an according AuthenticationService. It is therefore required that no other package providing open-xchange-authentication is installed or any other AuthenticationService is enabled.


# Configuration

The full config reference can be found under the `com.openexchange.authentication.oauth` namespace in the config properties documentation.


## Example

Create `/opt/open-xchange/etc/authentication-oauth.properties`:

```
com.openexchange.authentication.oauth.tokenEndpoint = https://id.example.com/oauth2/token
com.openexchange.authentication.oauth.clientId = <client-id>
com.openexchange.authentication.oauth.clientSecret = <client-secret>
com.openexchange.authentication.oauth.scope = email
```


# User resolution

For App Suite session creation, an according App Suite user entity needs to be resolved based on either the provided username or an attribute contained in the JSON token response from the Authorization Server. Per default, it is expected that the the provided username matches the pattern `<user-name>@<context-name>`. `<context-name>` must be a valid login mapping of the context or the numeric context identifier. `<user-name>` must match the provisioned user name.

The resolution behavior is configurable to use different parameters and different patterns to match input values to lookup values. For details, see configuration documentations for the following properties:

  * `com.openexchange.authentication.oauth.contextLookupSource`
  * `com.openexchange.authentication.oauth.contextLookupParameter`
  * `com.openexchange.authentication.oauth.contextLookupNamePart`
  * `com.openexchange.authentication.oauth.userLookupSource`
  * `com.openexchange.authentication.oauth.userLookupParameter`
  * `com.openexchange.authentication.oauth.userLookupNamePart`


# Logging

Set the following loggers to level `DEBUG` to debug login/session related issues:

  * `com.openexchange.authentication.oauth`
  * `com.openexchange.session.oauth`
  * `com.openexchange.ajax.LoginServlet`

