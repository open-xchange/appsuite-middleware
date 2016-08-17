---
title: Security Considerations
---

# Authorization

  * Granting access to external applications is a two-step process. Firstly the user needs to login via a special login screen. Secondly he must grant access to the application on a second screen. All requested permissions and details about the requesting application are shown to him.
  * On successful authentication a transient session is created. Some OAuth parameters are used as additional input for the cookie hashes. Therefore those sessions should not be usable outside the authorization context. After granting/denying access the session is terminated again. If the user closes the browser window instead, the session will continue to exist for approximately the configured short term lifetime. All subsequent requests after signing in verify the users session based on the provided session ID and secret cookie. If IP check is configured to happen, it will also take place.
  * Autologin as provided by some well-known OAuth service providers is not supported. A separate sign-in process is currently always necessary.
  * To exacerbate bypassing the authorization flow via the provided screens, a special token is generated on requesting the login screen and needs to be passed around with subsequent requests. Additionally the referer header of every request is checked to always reflect the expected former request.
  * We require the client application to send a state parameter along with the initial request.
  * The authorization code is valid for max. 10 minutes. Exchanging it always results in a refresh/access token pair.


# Permissions / Limits

  * A user may grant access to at most 50 different clients.
  * A user may allow the generation of at most 10 refresh/access token pairs per application. Every further requests leads to a deletion of the oldest token pair.
  * An application may request access to every available scope. However the granted scope is always limited by the users permissions. If the client requests permissions the user doesn't have, the according scope token will be ignored. The finally granted scope is part of the token response.
  * It is possible to deny OAuth access at all for certain users via a config-cascade enabled property `com.openexchange.oauth.provider.enabled`.


# Data Storage

  * OAuth clients are stored within the global DB. Their secrets are encrypted via a configurable static secret.
  * OAuth token pairs are stored within the user DB without any encryption.


# API Access

  * A selected subset of the public HTTP API will be published under a separate path to support OAuth.
  * The Card- and CalDAV endpoints will be extended to support OAuth directly via the `Bearer` authentication scheme.
  

# Other

  * Every time a user grants access to an external application, a notification email is sent out to his primary mail address.
  * Refresh tokens don't expire but can be revoked by both, the user and the client application.
  * Authorization codes consist either of 64 random chars or have a length of 48 chars, where 16 chars consist of encoded user identifiers and 32 chars are random and based on 128bit Java UUIDs. The length is determined by the configured storage (hazelcast [default] or database).
  * Access tokens have a life time of 60 minutes. Every time the client requests a new one the according refresh token is also changed.
  * Access and refresh tokens have a length of 48 chars. 16 chars consist of encoded user identifiers, while 32 chars are random and based on 128bit Java UUIDs.
  * Client IDs consist of an encoded context group identifier and 256 partially random bits based on two Java UUIDs.
  * Client Secrets consist of 256 partially random bits based on two Java UUIDs.
  * Client redirect URIs must point to HTTPs endpoints unless the host is `localhost`, `127.0.0.1` or `[::1]`. This is to ease client development.
