---
Title: SAML Bearer Assertion Authorization Grant
---

With version 7.8.4 the middleware supports OAuth authentication for the primary mail account via SAML Bearer Assertion Authorization Grant.
This document describes how to configure the middleware to use an rfc6749 compliant token endpoint to gain oauth access and refresh tokens.
Furthermore this document describes how to extend a custom SAML implementation to support OAuth authentication.


# How to configure the middleware against a token endpoint

**Prerequisite**:

* A running SAML configuration
* A running and configured mail server supporting oauthbearer authentication
* A running token endpoint supporting the SAML Bearer Assertion Authorization Grant and (optional) the Token Refresh Grant. E.g. via a running wso2 identity server.

Now you only need to configure the following properties:

```
com.openexchange.mail.authType=oauthbearer
com.openexchange.mail.transport.authType=oauthbearer

com.openexchange.saml.oauth.token = <the token endpoint url>
com.openexchange.saml.oauth.clientId = <the client id>
com.openexchange.saml.oauth.clientSecret = <the client secret>
```

Once configured the workflow is the following:

1. A user initiates the login to the middleware
2. The user is redirected to the identity provider.
3. The user enters his credentials and is redirected back to the middleware with a signed SAML 2.0 bearer token
4. The middleware validates the token
5. The middleware requests an access token from the token endpoint using the SAML bearer assertion authorization grant
6. The middleware creates a session and the user is then logged in
7. The user accesses the primary mail account
8. The middleware connects to the mail server using the access tokens previously obtained from the token endpoint
9. The mail server validates the token against a rfc7662 introspection endpoint
10. The user is logged into the mailserver


# How to adapt a custom SAML implementation

In order to support OAuth authentication with your custom SAML backend you need to adapt your own com.openexchange.saml.spi.SAMLBackend.

The properties map inside the com.openexchange.saml.spi.AuthenticationInfo object returned from the SAMLBackend needs to contain the following properties:

* com.openexchange.saml.AccessToken
* com.openexchange.saml.RefreshToken (optional)

You also need to define and register a com.openexchange.mail.api.AuthenticationFailedHandler with a service ranking > 100.
The AuthenticationFailedHandler should only refresh the access and refresh tokens in case the AuthType of the MailConfig is an OAuth type.
If the AuthType is an OAUTH type the following steps must be followed:

1. Obtain new OAuth tokens
2. Update the corresponding session parameters
3. Update the password of the MailConfig with the new access tokens
4. Store the session via the SessiondService
5. Return with "return AuthenticationFailureHandlerResult.createRetryResult();"

If the AuthenticationFailedHandler is unable to refresh the tokens it should remove the session and return with an SESSION_EXPIRED exception.

E.g.:

```Java
public class OAuthFailedAuthenticationHandler implements AuthenticationFailedHandler {
    @Override
    public AuthenticationFailureHandlerResult handleAuthenticationFailed(OXException failedAuthentication, Service service, MailConfig mailConfig, Session session) throws OXException {
      if( AuthType.isOAuthType(mailConfig.getAuthType())){
          SessiondService sessiondService = Services.getService(SessiondService.class);
          if(session.containsParameter(Session.PARAM_OAUTH_REFRESH_TOKEN)){
              // try to get new tokens here
              session.setParameter(Session.PARAM_OAUTH_ACCESS_TOKEN, accessToken);
              session.setParameter(Session.PARAM_OAUTH_REFRESH_TOKEN, refreshToken);
              mailConfig.setPassword(accessToken);
              sessiondService.storeSession(session.getSessionID());
              return AuthenticationFailureHandlerResult.createRetryResult();
          }
          // Unable to refresh access token -> logout
          sessiondService.removeSession(session.getSessionID());
          return AuthenticationFailureHandlerResult.createErrorResult(SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID()));
      }
      return AuthenticationFailureHandlerResult.createContinueResult();
    }
}
```
