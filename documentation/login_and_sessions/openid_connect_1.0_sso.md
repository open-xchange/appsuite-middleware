---
title: OpenID Connect 1.0 SSO
icon: fab fa-openid
tags: OpenId, SSO, Configuration
---

# Introduction

With version 7.10.0 App Suite introduces the support for Single Sign On (SSO) with OpenID which is also compatible with version 7.8.4. OpenID Connect 1.0 is a simple identity layer on top of the OAuth 2.0 protocol. 
It enables Clients to verify the identity of the End-User based on the authentication performed by an Authorization Server, as well as to obtain basic profile information about the End-User in an interoperable and REST-like manner. 
The implementation supports handling of multiple OpenID providers (OP) for different hosts from one single instance. Logout with termination of the OPs session is available alongside an autologin mechanism which checks 
for a valid OP session before login. So far only the code flow for login and a third party initiated login are supported. There is also no possibility to gather additional user information from an OP after authorization so far. 
The session status mechanism with two communicating IFrames like suggested by the standard is also not supported.

The full OpenID specification can be found [here](http://openid.net/specs/openid-connect-core-1_0.html).

# Feature overview
* Web SSO using Authorization Code Grant
* HTTP API login with username/password using Resource Owner Password Credentials Grant
* Autologin with valid session confirmation on OP side
* Direct autologin via session storage in own OIDC Cookie
* Logout with additional redirect to OP for session termination
* Direct logout from App Suite
* OAuth token refresh
* Third Party initiated login
* Multiple registration of OpenID backends in one instance
* Multiple tenants working with one backend

# Supported Message Flows
So far only code flow is supported for login. The logout mechanisms are either with termination of the OP session or without. 
There are also two autologin flows supported, one with redirect to the OP for a valid session check and one without.

## Web SSO login flow
The following diagram describes the whole code flow login process. The current implementation does not gather additional user informations like described in step 6.

![Code flow login](openid_connect_1.0_sso/APIgw_Relationship Oauth2.png "Code flow login")

## API login flow

OX Mail, Exchange-ActiveSync, CalDAV/CardDAV or other clients/protocols might only support direct username/password authentication. It is possible to still use OpenID Connect for according authentication requests, by using the [Resource Owner Password Credentials Grant](https://tools.ietf.org/html/rfc6749#section-4.3), which is
part of the OAuth 2.0 Core Framework. This is an optional feature that needs to be enabled explicitly via the `com.openexchange.oidc.enablePasswordGrant` configuration property.

**Important:** Internally, this feature registers an according `AuthenticationService`. It is therefore required that no other package providing `open-xchange-authentication` is installed. For compatibility reasons `open-xchange-oidc` will not conflict with according packages like `open-xchange-authentication-database`!

## OAuth Tokens

**An OAuth bearer access token is always required to be issued along with the ID token!**

Note that OAuth tokens contained in token responses are always set as session parameters and internally validated on each request. If the Authorization Server issues an access token with expiry date, this date will determine how long the App Suite session can be used. OIDC sessions will be terminated on the first request that happens after `expires_in - (com.openexchange.oidc.oauthRefreshTime / 1000)` seconds.

If a refresh token is contained in the token response, access tokens will be refreshed during the first request that happens after `expires_in - (com.openexchange.oidc.oauthRefreshTime / 1000)` seconds. A failed refreshed due an invalid/revoked refresh token or an `invalid_grant` response will lead to the session being terminated.

If `expires_in` is not contained in the token response, the session will live as long as the configured short-term session lifetime, no matter if a refresh token is contained or not. The access token will never be refreshed in that case.

## Autologin with check for a valid OP session
![Autologin via OP](openid_connect_1.0_sso/Autologin via OP.png "Autologin via OP")

If no valid session is present on side of the OP, the user is asked to login first. The handling on side of the Relying party is untouched by this scenario. 

## Autologin directly in App Suite
![Autologin direct](openid_connect_1.0_sso/Autologin direct.png "Autologin direct")

If no OIDC cookie exists, the standard login procedure is triggered.

## Logout with redirect to OP and termination of session
![Logout via OP](openid_connect_1.0_sso/Logout via OP.png "Logout via OP")

To enable the logout with previous termination of the OP session, the `com.openexchange.oidc.ssoLogout` property has to be set to `true`. Additionaly the `com.openexchange.oidc.rpRedirectURIPostSSOLogout` property has to be configured to redirect the user to the logout mechanism that handles the response on App Suite site. This location has to be registered and known for the client, otherwise the logout request will not be handled. Finally, property `com.openexchange.oidc.rpRedirectURILogout` holds the location, where the user should be redirected after a succesful logout, a custom goodbye page, for example.

If an error occurs during the logout process, like an invalid response from the OP, the RP session is terminated anyways. Additionally there is an example implementation of the verification dialog in the `examples/backend-samples` repository which should work with a connect2ID OpenID server. 
The according project is `com.openexchange.sample.c2id-logout-page-jsp`. The example is called with the following parameters:

```
  id_token_hint:eyJraWQiOiJDWHVwIiwiYWxn...
  post_logout_redirect_uri:https://192.168.33.109//appsuite/api/oidc/logout
  state:di26WOr8iZyVFReDvgsNwueDolfgwuB1rpjbo3t99Wo
```

* `id_token_hint`: The users id token, to acquire the correct session.
* `post_logout_redirect`: Where should the user be redirected after the confirmation.
* `state`: A generated state property, to verify the response later.


## Direct Logout from App Suite
![Logout direct](openid_connect_1.0_sso/Logout direct.png "Logout direct")

The direct logout without termination of the OP session is enabled by default, the property is `com.openexchange.oidc.ssoLogout` which is set to `false`.

# Developers Guide
There are three relevant bundles, the `com.openexchange.oidc` bundle, which contains all relevant interfaces. The default implementation, contained in the `com.openexchange.oidc.impl` bundle, which uses the Nimbus SDK
 to provide the needed OpenID features, located in the `com.nimbus` bundle. Further details can be found [here](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk). 
 The default implementation is designed to work with the [connect2id](https://connect2id.com/products/server) OpenID server, per default all features can be used, except the logout via OP flow. 
 Therefore the connect2ID server has to be extended by a logout confirmation page like described before.

## The OIDC Backend
The core implementation of the OIDC Backend can be used as reference to all further implementations. To keep the needed effort as small as possible, every new implementation should extend the core `com.openexchange.oidc.spi.AbstractOIDCBackend` 
and override only those functions, that should behave differently. The configuration of every backend is loaded from an implementation of the `com.openexchange.oidc.OIDCBackendConfig` interface. 
The developer should also extend the core implementation of this interface, which is `com.openexchange.oidc.spi.AbstractOIDCBackendConfig` and replace only those property calls, that are different from the core configuration. 
This way multiple backends can share the same properties, which reduces redundancy and keeps maintenance efforts small. 
The `com.openexchange.oidc.tools.OIDCTools` provide a set of useful functions and constants, remember to have a look at those, before implementing the same for every backend.

## The used Cookies

The used cookies are the `open-xchange-public-session-`, `open-xchange-secret-` and `open-xchange-session-` cookie.

## Tokens

The OAuth access and refresh tokens are both stored in the user session, alongside with the ID token of the user. The refresh token is automatically used to get a new Access token, when needed.
The IDToken usually stores the users unique identification in the `sub` field, but can use other claims to identify App Suite users, too. How contexts and users are resolved from an ID token is configurable and can also be overridden with custom OIDC Backends.


# Operators Guide

If you want to enable this feature without starting the implemented core backend, you have to disable it by setting `com.openexchange.oidc.startDefaultBackend=false`. 
For an overview of all possible properties and their description, take a look at all [OpenID properties](https://documentation.open-xchange.com/components/middleware/config/{{ site.baseurl }}/index.html#mode=features&feature=OpenID)

## Configuration
You can find a description of every property on the property documentation site [by searching for `oidc`](https://documentation.open-xchange.com/components/middleware/config/{{ site.baseurl }}/#mode=search&term=oidc). Alternatively, take a look at the `com.openexchange.oidc.OIDCConfig` and `com.openexchange.oidc.OIDCBackendConfig` classes. 

If you don't specify a distinct UI web path for your backend, via `com.openexchange.oidc.uiWebPath`, you have to configure the default path of the web UI, which is used in several places, via `/opt/open-xchange/etc/server.properties`. 
If you haven't already (because of other requirements), set [com.openexchange.UIWebPath](https://documentation.open-xchange.com/components/middleware/config/{{ site.baseurl }}/index.html#com.openexchange.UIWebPath) to `/appsuite/`.

### User resolution

For OX session creation, an according OX user entity needs to be resolved based on the issued ID token. Per default, it is expectedt that the `sub` claim contains a value in the form of `<user-name>@<context-name>`. `<context-name>` must be a valid login mapping of the context or the numeric context identifier. `<user-name>` must match the provisioned user name.

The resolution behavior is configurable to use different claims and different patterns to match claim values to lookup values. For details, see configuration documentations for the following properties:

* com.openexchange.oidc.contextLookupClaim
* com.openexchange.oidc.contextLookupNamePart
* com.openexchange.oidc.userLookupClaim
* com.openexchange.oidc.userLookupNamePart

With a customized OIDC backend, the resolution behavior can be overridden based on customer requirements.

### Autologin configuration

If you want to use the OpenId login feature, you have to make sure that the regular autologin mechanism is disabled by setting the assosiated Sessiond property to false `com.openexchange.sessiond.autologin=false`. This is a crucial precondition for any of the provided autologin mechanisms. 

### Frontend Configuration

The frontend plugin `open-xchange-appsuite-oidc` is deactivated by default. In order to enable the special OpenID login handling, enable the oidcLogin flag via the `/opt/open-xchange/etc/as-config.yml`. E.g.:

```yaml
default:
    host: my-domain.com
    oidcLogin: true
```

