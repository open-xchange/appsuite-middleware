---
title: Client Developer Guide
---

# General Assumptions

Starting with version v7.8.0, OX App Suite can act as an OAuth 2.0 provider that allows access to certain API calls. In terms of [RFC 6749](http://tools.ietf.org/html/rfc6749) the App Suite backend acts as `authorization server` and `resource server` while every user is a `resource owner`. Client applications must be of type `confidential` according to the `web application` profile, i.e. they must be able to securely store API access credentials on an application server without exposing them to the resource owner. The only supported grant type is `authorization code`. Every time a user grants access to his personal data, your application will receive both, an `access token` and a `refresh token`. The former one is a short-living token (usually one hour), that must be sent along with every API call. The latter one lives as long as the user does not revoke access for your application and can always be exchanged against a fresh token pair. The access token type is `bearer` as defined in [RFC 6750](http://tools.ietf.org/html/rfc6750).

Before you can develop against the API of an OX App Suite deployment you must register your application at the according service provider. The registration process is out of scope here. Please contact your service provider for more information. As a result of the registration process you will get two tokens from your service provider: a `client identifier` and a `client secret`. The former one must be provided in every call targeting the authorization API. The latter one is only needed when an authorization code is exchanged for an access/refresh token pair and must strictly be kept secret.

All calls towards the authorization or token endpoint enforce HTTPS. Plain HTTP calls result in redirects to the secure location. However you must never perform plain HTTP calls. Especially calls to the token endpoint will otherwise expose your client secret!

# Authorization Flow

## Step 1: Request an authorization code

`GET https://ox.example.com/appsuite/api/oauth/provider/authorization`

|  Parameter  |  Description  |  Required  |
|-------------|---------------|------------|
| client_id | The client identifier provided by your OX service provider. | Yes |
| redirect_uri | The URI used for transmitting the authorization code. Must be one of the URIs that have been registered at your service provider, otherwise it will not be accepted. | Yes |
| state | An arbitrary string that helps you to prevent CSRF attacks and to associate the authorization code with the initial request. | Yes |
| response_type | Must always be `code`. | Yes |
| scope | The scope according to the API requests you are going to make after access was granted. Can be omitted, in which case the default scope is applied that has been submitted during client registration. A scope is a space-separated string of scope tokens, e.g. `read_contacts write_contacts`. | No |
| language | An optional locale (e.g. `de_DE`) that is used to translate the user-visible parts of the authorization flow if possible. If omitted `en_US` is used. | No |

The response is a login screen. You probably want to display it within a popup window:
![OAuth Login Screen](login_screen.png "fig:OAuth Login Screen")

After signing in the user gets display a screen where he can choose to grant or deny access to the requesting application:
![OAuth Grant Screen](grant_screen.png "fig:OAuth Grant Screen")

This screen will present all necessary information to the user, i.e. which app tries to obtain access for which scopes. The user can then decide to grant access (by providing his credentials) or to deny the request. In both cases as well as in certain error cases an authorization code or an error is provided to your application via another redirect:

    HTTP/1.1 302 Found
    Location: https://myclient.com/oauth/callback?code=07d3c9d7e6fc4e828c600bba0eee2ad0&state=1234
    
| Parameter | Description |
|-----------|-------------|
| code      | The authorization code. Only present if the request succeeded and therefore `error` is absent. |
| state | The state parameter provided by your application during the authorization request. This parameter is always present except in one case: You did not include a state parameter and therefore receive an error. |
| error | This parameter is present if your request failed or the user denied access. Its value is one of the error codes defined [here](http://tools.ietf.org/html/rfc6749#section-4.1.2.1). Potential errors are listed in the table below. |
| error_description | A description that further describes an occurred error. Only present if `error` is present. |

| Error Code | Description |
|------------|-------------|
| invalid_request | Your request was malformed, i.e. a mandatory parameter was missing or a parameters value was invalid. |
| invalid_scope | The requested scope or one of its tokens was invalid. |
| temporarily_unavailable | The service is currently not available. You may try again later. |
| server_error | An internal error occurred. You may try again later or contact your service provider if it happens again. |
| access_denied | The user denied your request or is not allowed to grant access to 3rd party applications. |
| unsupported\_response\_type | You provided a `response_type` different than "code". |


## Step 2: Obtain a token pair

`POST https://ox.example.com/appsuite/api/oauth/provider/accessToken`

The request body must be of type `application/x-www-form-urlencoded` containing the following parameters:

|  Parameter  |  Description  |  Required  |
|-------------|---------------|------------|
| client_id | The client identifier provided by your OX service provider. | Yes |
| client_secret | The client secret provided by your OX service provider. | Yes |
| redirect_uri | The same redirect URI that was used to request the authorization code. | Yes |
| grant_type | Must be "authorization_code". | Yes |
| code | The authorization code. | Yes |

The response is in JSON format and contains either a token pair or an error code, depending on the succession of the request. A successful response looks like this:

    HTTP/1.1 200 OK
    {
      "access_token": "0062a74e65a74cefb364dcd17648eb04",
      "refresh_token": "a6fa1687d3434a08aa4b60b2c8bab1ec",
      "token_type": "Bearer",
      "expires_in": 3600,
      "scope": "read_contacts read_calendar"
    }

`expires_in` contains the number of seconds the access token is valid from now on. `scope` contains the scope that was granted. It might differ from the initial requested scope in cases the user itself does not have all necessary permissions that you requested.

The following error conditions exist:

-   **invalid\_request:** The request was malformed. HTTP status 400.
-   **unauthorized\_client:** Client secret was invalid. HTTP status 401.
-   **server\_error:** An internal error occurred. HTTP status 500.

An error response will look like this:

    HTTP/1.1 400 Bad Request
    {
      "error": "invalid_request",
      "error_description": "invalid parameter value: client_id"
    }

## Step 3: Refresh an access token

You must remember the `expires_in` value and obtain a new access token before this timeout is exceeded by transmitting the refresh token.

`POST https://ox.example.com/appsuite/api/oauth/provider/accessToken`

|  Parameter  |  Description  |  Required  |
|-------------|---------------|------------|
| client_id | The client identifier provided by your OX service provider. | Yes |
| client_secret | The client secret provided by your OX service provider. | Yes |
| grant_type | Must be "refresh_token". | Yes |
| refresh_token | The refresh token. | Yes |

The response is the same as in step 2. Note that also a new refresh token is generated and contained in the response. The one that was used in the request is not valid any longer.

## Step 4: Revoke requested access

If you don't longer need a formerly granted access, you are encouraged to revoke it. This will invalidate the according access and refresh token what is desired from a security perspective.

`GET https://ox.example.com/appsuite/api/oauth/provider/revoke`

|  Parameter  |  Description  |  Required  |
|-------------|---------------|------------|
| access_token | The access token. Note that the whole grant will be revoked, i.e. the according refresh token will also be invalidated. | No |
| refresh_token | The refresh token. | No |

It's up to you if you want to invalidate by access or refresh token, as long as you provide one them and it's still valid. If the token is not valid, an error is returned:

    HTTP/1.1 400 Bad Request
    {
      "error": "invalid_request",
      "error_description": "invalid parameter value: access_token"
    }
    
## Validating the token

Provided that the Open-Xchange Server is also acting as authorization server verifying a token using the Open-Xchange Authorization Server endpoint is relatively simple. Your application includes the access token in the `access_token` parameter for the following endpoint:

    GET /appsuite/api/oauth/provider/tokeninfo??access_token=ae113KfFBGRNJru1FQd44AzqT3Zg...

That endpoint accepts an access token and returns information about that access token including which application was it issued to, the scopes the user consented to, the remaining lifetime of the token, and the context/user identifiers. Example:

    {
      "audience": "ZGVmYXVsdA/4ecafb74...b94b0a8e4e0e325d07f6d0",
      "context_id": 1,
      "user_id": 2,
      "expiration_date": "2016-09-15T00:00:00",
      "scope": "write_contacts read_contacts"
    }
    
If the token has expired, has been tampered with, or the permissions revoked, the Open-Xchange Authorization Server will respond with an error. The error surfaces as a 400 status code, and a JSON body as follows:

    {"error":"invalid_token"}

# Available APIs

## HTTP JSON API

With OAuth you can access a subset of the existing [HTTP API](http://oxpedia.org/wiki/index.php?title=HTTP_API). There are a few differences to the existing API documentation you have to keep in mind:

-   The available modules and actions must be accessed via a special servlet path `/appsuite/api/oauth/modules`.

        GET /appsuite/api/oauth/modules/contacts?action=all&folder=123

-   The session parameter can be omitted for every request. Instead the access token must be provided as authorization header:

        Authorization: Bearer 0062a74e65a74cefb364dcd17648eb04

-   In addition to the normal error handling every call may also result in a specific OAuth error response. Possible errors are:
    -   **Missing/invalid access token:** No access token was provided or the provided one is invalid. The response follows the scheme defined in [RFC 6750, section 3](http://tools.ietf.org/html/rfc6750#section-3). Example:
    
            HTTP/1.1 401 Unauthorized
            WWW-Authenticate: Bearer realm="example",
                              error="invalid_token",
                              error_description="The access token expired"

    -   **Insufficient scope:** You cannot perform this call because the OAuth grant does not include a required scope. Example:
    
            HTTP/1.1 403 Forbidden
            Content-Type: application/json;charset=UTF-8

            {
              "error": "insufficient_scope",
              "scope": "write_contacts"
            }

    -   **Invalid request:** The request was considered invalid from an OAuth perspective. Example:
            
            HTTP/1.1 400 Bad Request
            Content-Type: application/json;charset=UTF-8

            {
              "error": "invalid_request",
              "error_description": "Some detailed description relevant for client developers."
            }

Find the OAuth-enabled modules and actions below. Every action is bound to a specific scope. However some actions are available implicitly if access for any scope is granted. E.g. you may always request a users details or configuration if any kind of OAuth access is granted, but you may only change a users configuration, if the `write_userconfig` scope is granted. The view on the folder tree is always limited by the granted scope. E.g. if you were granted `read_contacts` you can also perform all read-only requests that target contact folders. In turn you may only create/modify/delete contact folders if obtained the `write_contacts` scope.


### reminder

| Action | Scope |
|--------|-------|
| delete | write_reminders |
| remindAgain | write_reminders |
| range | read_reminders |
| updates | read_reminders |


### config

| Name       | Scope            |
|------------|------------------|
| path (GET) | <any>            |
| path (PUT) | write_userconfig |


### user/me

| Name | Scope |
|------|-------|
| GET  | <any> |


### folders

| Name       | Scope     |
|------------|-----------|
| clear      | <depends> |
| update     | <depends> |
| new        | <depends> |
| updates    | <depends> |
| get        | <depends> |
| root       | <depends> |
| allVisible | <depends> |
| path       | <depends> |
| delete     | <depends> |
| list       | <depends> |


### tasks

| Name    | Scope       |
|---------|-------------|
| delete  | write_tasks |
| copy    | write_tasks |
| get     | read_tasks  |
| search  | read_tasks  |
| updates | read_tasks  |
| new     | write_tasks |
| list    | read_tasks  |
| update  | write_tasks |
| confirm | write_tasks |
| all     | read_tasks  |


### contact

| Name            | Scope          |
|-----------------|----------------|
| delete          | write_contacts |
| listuser        | read_contacts  |
| birthdays       | read_contacts  |
| autocomplete    | read_contacts  |
| advanchedSearch | read_contacts  |
| copy            | write_contacts |
| anniversaries   | read_contacts  |
| get             | read_contacts  |
| search          | read_contacts  |
| updates         | read_contacts  |
| new             | write_contacts |
| getuser         | read_contacts  |
| list            | read_contacts  |
| update          | write_contacts |
| all             | read_contacts  |


### calendar

| Name                | Scope          |
|---------------------|----------------|
| delete              | write_calendar |
| resolveuid          | read_calendar  |
| copy                | write_calendar |
| get                 | read_calendar  |
| getChangeExceptions | read_calendar  |
| search              | read_calendar  |
| updates             | read_calendar  |
| freebusy            | read_calendar  |
| newappointments     | read_calendar  |
| has                 | read_calendar  |
| new                 | write_calendar |
| list                | read_calendar  |
| update              | write_calendar |
| all                 | read_calendar  |
| confirm             | write_calendar |


## Card- and CalDAV

If installed, the interfaces for Card- and CalDAV are also available via OAuth. Clients can notice it based on the `WWW-Authenticate` headers of responses to unauthorized requests. If OAuth is supported, a header declaring `Bearer` as supported auth scheme will be present. The according scope values are `carddav` and `caldav`. The interfaces support OAuth natively so there is no extra servlet path or the like.
