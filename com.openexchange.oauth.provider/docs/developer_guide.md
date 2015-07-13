Open-Xchange OAuth 2.0 Client Developer Guide
=============================================

General Assumptions
-------------------
Starting with version 7.8.0 OX App Suite can act as an OAuth 2.0 provider that allows access to certain API calls. In terms of [RFC 6749](http://tools.ietf.org/html/rfc6749) the App Suite backend acts as `authorization server` and `resource server` while every user is a `resource owner`. Client applications must be of type `confidential` according to the `web application` profile, i.e. they must be able to securely store API access credentials on an application server without exposing them to the resource owner. The only supported grant type is `authorization code`. Every time a user grants access to his personal data, your application will receive both, an `access token` and a `refresh token`. The former one is a short-living token (usually one hour), that must be sent along with every API call. The latter one lives as long as the user does not revoke access for your application and can always be exchanged against a fresh token pair. The access token type is `bearer` as defined in [RFC 6750](http://tools.ietf.org/html/rfc6750).

Before you can develop against the API of an OX App Suite deployment you must register your application at the according service provider. The registration process is out of scope here. Please contact your service provider for more information. As a result of the registration process you will get two tokens from your service provider: a `client identifier` and a `client secret`. The former one must be provided in every call targeting the authorization API. The latter one is only needed when an authorization code is exchanged for an access/refresh token pair and must strictly be kept secret.

All calls towards the authorization or token endpoint enforce HTTPS. Plain HTTP calls result in redirects to the secure location. However you must never perform plain HTTP calls. Especially calls to the token endpoint will otherwise expose your client secret!

Authorization Flow
------------------

###Step 1: Request an authorization code###

`GET https://ox.example.com/appsuite/api/oauth/provider/authorization`

<table>
  <thead>
    <tr>
      <th>Parameter</th>
      <th>Description</th>
      <th>Required</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>client_id</td>
      <td>The client identifier provided by your OX service provider.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>redirect_uri</td>
      <td>The URI used for transmitting the authorization code. Must be one of the URIs that have been registered at your service provider, otherwise it will not be accepted.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>state</td>
      <td>An arbitrary string that helps you to prevent CSRF attacks and to associate the authorization code with the initial request.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>response_type</td>
      <td>Must always be "code".</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>scope</td>
      <td>The scope according to the API requests you are going to make after access was granted. Can be omitted, in which case the default scope is applied that has been submitted during client registration. A scope is a space-separated string of scope tokens, e.g. "read_contacts write_contacts".</td>
      <td>No</td>
    </tr>
    <tr>
      <td>language</td>
      <td>An optional locale (e.g. "de_DE") that is used to translate the user-visible parts of the authorization flow if possible. If omitted "en_US" is used.</td>
      <td>No</td>
    </tr>
  </tbody>
</table>

The response is a login screen. You propably want to display it within a popup window:
![OAuth Login Screen](login_screen.png "OAuth Login Screen")

After signing in the user gets display a screen where he can choose to grant or deny access to the requesting application:
![OAuth Grant Screen](grant_screen.png "OAuth Grant Screen")

This screen will present all necessary information to the user, i.e. which app tries to obtain access for which scopes. The user can then decide to grant access (by providing his credentials) or to deny the request. In both cases as well as in certain error cases an authorization code or an error is provided to your application via another redirect:

    HTTP/1.1 302 Found
    Location: https://myclient.com/oauth/callback?code=07d3c9d7e6fc4e828c600bba0eee2ad0&state=1234

<table>
  <thead>
    <tr>
      <th>Parameter</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>code</td>
      <td>The authorization code. Only present if the request succeeded and therefore <code>error</code> is absent.</td>
    </tr>
    <tr>
      <td>state</td>
      <td>The state parameter provided by your application during the authorization request. This parameter is always present except in one case: You did not include a state parameter and therefore receive an error.</td>
    </tr>
    <tr>
      <td>error</td>
      <td>
        This parameter is present if your request failed or the user denied access. Its value is one of the error codes defined <a href="http://tools.ietf.org/html/rfc6749#section-4.1.2.1" target="_blank">here</a>. Errors are returned in the following cases:
        <ul>
          <li><strong>invalid_request:</strong> Your request was malformed, i.e. a mandatory parameter was missing or a parameters value was invalid.</li>
          <li><strong>invalid_scope:</strong> The requested scope or one of its tokens was invalid.</li>
          <li><strong>temporarily_unavailable:</strong> The service is currently not available. You may try again later.</li>
          <li><strong>server_error:</strong> An internal error occured. You may try again later or contact your service provider if it happens again.</li>
          <li><strong>access_denied:</strong> The user denied your request or is not allowed to grant access to 3rd party applications.</li>
          <li><strong>unsupported_response_type:</strong> You provided a <code>response_type</code> different than "code"</li>
        </ul>
      </td>
    </tr>
    <tr>
      <td>error_description</td>
      <td>A description that further describes an occurred error. Only present if <code>error</code> is present.</td>
    </tr>
  </tbody>
</table>

###Step 2: Obtain a token pair###

`POST https://ox.example.com/appsuite/api/oauth/provider/accessToken`

The request body must be of type `application/x-www-form-urlencoded` containing the following parameters:

<table>
  <thead>
    <tr>
      <th>Parameter</th>
      <th>Description</th>
      <th>Required</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>client_id</td>
      <td>The client identifier provided by your OX service provider.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>client_secret</td>
      <td>The client secret provided by your OX service provider.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>redirect_uri</td>
      <td>The same redirect URI that was used to request the authorization code.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>grant_type</td>
      <td>Must be "authorization_code".</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>code</td>
      <td>The authorization code.</td>
      <td>Yes</td>
    </tr>
  </tbody>
</table>

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
  * **invalid_request:** The request was malformed. HTTP status 400.
  * **unauthorized_client:** Client secret was invalid. HTTP status 401.
  * **server_error:** An internal error occurred. HTTP status 500.


An error response will look like this:
  
    HTTP/1.1 400 Bad Request
    {
      "error": "invalid_request",
      "error_description": "invalid parameter value: client_id"
    }

###Step 3: Refresh an access token###
You must remember the `expires_in` value and obtain a new access token before this timeout is exceeded by transmitting the refresh token.

`POST https://ox.example.com/appsuite/api/oauth/provider/accessToken`

<table>
  <thead>
    <tr>
      <th>Parameter</th>
      <th>Description</th>
      <th>Required</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>client_id</td>
      <td>The client identifier provided by your OX service provider.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>client_secret</td>
      <td>The client secret provided by your OX service provider.</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>grant_type</td>
      <td>Must be "refresh_token".</td>
      <td>Yes</td>
    </tr>
    <tr>
      <td>refresh_token</td>
      <td>The refresh token.</td>
      <td>Yes</td>
    </tr>
  </tbody>
</table>

The response is the same as in step 2. Note that also a new refresh token is generated and contained in the response. The one that was used in the request is not valid any longer.

###Step 4: Revoke requested access###
If you don't longer need a formerly granted access, you are encouraged to revoke it. This will invalidate the according access and refresh token what is desired from a security perspective.

`GET https://ox.example.com/appsuite/api/oauth/provider/revoke`

<table>
  <thead>
    <tr>
      <th>Parameter</th>
      <th>Description</th>
      <th>Required</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>access_token</td>
      <td>The access token. Note that the whole grant will be revoked, i.e. the according refresh token will also be invalidated.</td>
      <td>No</td>
    </tr>
    <tr>
      <td>refresh_token</td>
      <td>The refresh token.</td>
      <td>No</td>
    </tr>
  </tbody>
</table>

It's up to you if you want to invalidate by access or refresh token, as long as you provide one them and it's still valid. If the token is not valid, an error is returned:

    HTTP/1.1 400 Bad Request
    {
      "error": "invalid_request",
      "error_description": "invalid parameter value: access_token"
    }

Available APIs
--------------

With OAuth you can access a subset of the existing [HTTP API](http://oxpedia.org/wiki/index.php?title=HTTP_API). There are a few differences to the existing API documentation you have to keep in mind:

  * The available modules and actions must be accessed via a special servlet path `/appsuite/api/oauth/modules`.

        GET /appsuite/api/oauth/modules/contacts?action=all&folder=123

  * The session parameter can be omitted for every request. Instead the access token must be provided as authorization header:

        Authorization: Bearer 0062a74e65a74cefb364dcd17648eb04
  
  * In addition to the normal error handling every call may also result in a specific OAuth error response. Possible errors are:
      * **Missing/invalid access token:** No access token was provided or the provided one is invalid. The response follows the scheme defined in [RFC 6750, section 3](http://tools.ietf.org/html/rfc6750#section-3). Example:

            HTTP/1.1 401 Unauthorized
            WWW-Authenticate: Bearer realm="example",
                              error="invalid_token",
                              error_description="The access token expired"

      * **Insufficient scope:** You cannot perform this call because the OAuth grant does not include a required scope. Example:

            HTTP/1.1 403 Forbidden
            Content-Type: application/json;charset=UTF-8

            {
              "error": "insufficient_scope",
              "scope": "write_contacts"
            }

      * **Invalid request:** The request was considered invalid from an OAuth perspective. Example:

            HTTP/1.1 400 Bad Request
            Content-Type: application/json;charset=UTF-8

            {
              "error": "invalid_request",
              "error_description": "Some detailed description relevant for client developers."
            }

Below is a table that describes all available modules and actions. Every action is bound to a specific scope. However some actions are available implicitly if access for any scope is granted. E.g. you may always request a users details or configuration if any kind of OAuth access is granted, but you may only change a users configuration, if the `write_userconfig` scope is granted. The view on the folder tree is always limited by the granted scope. E.g. if you were granted `read_contacts` you can also perform all read-only requests that target contact folders. In turn you may only create/modify/delete contact folders if obtained the `write_contacts` scope.

<table>
  <thead>
    <tr>
      <th>Module</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>reminder</td>
      <td>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Scope</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>delete</td>
              <td>write_reminders</td>
            </tr>
            <tr>
              <td>remindAgain</td>
              <td>write_reminders</td>
            </tr>
            <tr>
              <td>range</td>
              <td>read_reminders</td>
            </tr>
            <tr>
              <td>updates</td>
              <td>read_reminders</td>
            </tr>
          </tbody>
        </table>
      </td>
    </tr>
    <tr>
      <td>config</td>
      <td>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Scope</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>path (GET)</td>
              <td>&lt;any&gt;</td>
            </tr>
            <tr>
              <td>path (PUT)</td>
              <td>write_userconfig</td>
            </tr>
          </tbody>
        </table>
      </td>
    </tr>
    <tr>
      <td>user/me</td>
      <td>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Scope</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>GET</td>
              <td>&lt;any&gt;</td>
            </tr>
          </tbody>
        </table>
      </td>
    </tr>
    <tr>
      <td>folders</td>
      <td>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Scope</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>clear</td>
              <td>&lt;depends&gt;</td>
            </tr>
            <tr>
              <td>update</td>
              <td>&lt;depends&gt;</td>
            </tr>
            <tr>
              <td>new</td>
              <td>&lt;depends&gt;</td>
            </tr>
            <tr>
              <td>updates</td>
              <td>&lt;depends&gt;</td>
            </tr>
            <tr>
              <td>get</td>
              <td>&lt;depends&gt;</td>
            </tr>
            <tr>
              <td>root</td>
              <td>&lt;depends&gt;</td>
            </tr>
            <tr>
              <td>allVisible</td>
              <td>&lt;depends&gt;</td>
            </tr>
            <tr>
              <td>path</td>
              <td>&lt;depends&gt;</td>
            </tr>
            <tr>
              <td>delete</td>
              <td>&lt;depends&gt;</td>
            </tr>
            <tr>
              <td>list</td>
              <td>&lt;depends&gt;</td>
            </tr>
          </tbody>
        </table>
      </td>
    </tr>
    <tr>
      <td>tasks</td>
      <td>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Scope</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>delete</td>
              <td>write_tasks</td>
            </tr>
            <tr>
              <td>copy</td>
              <td>write_tasks</td>
            </tr>
            <tr>
              <td>get</td>
              <td>read_tasks</td>
            </tr>
            <tr>
              <td>search</td>
              <td>read_tasks</td>
            </tr>
            <tr>
              <td>updates</td>
              <td>read_tasks</td>
            </tr>
            <tr>
              <td>new</td>
              <td>write_tasks</td>
            </tr>
            <tr>
              <td>list</td>
              <td>read_tasks</td>
            </tr>
            <tr>
              <td>update</td>
              <td>write_tasks</td>
            </tr>
            <tr>
              <td>confirm</td>
              <td>write_tasks</td>
            </tr>
            <tr>
              <td>all</td>
              <td>read_tasks</td>
            </tr>
          </tbody>
        </table>
      </td>
    </tr>
    <tr>
      <td>contact</td>
      <td>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Scope</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>delete</td>
              <td>write_contacts</td>
            </tr>
            <tr>
              <td>listuser</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>birthdays</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>autocomplete</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>advanchedSearch</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>copy</td>
              <td>write_contacts</td>
            </tr>
            <tr>
              <td>anniversaries</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>get</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>search</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>updates</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>new</td>
              <td>write_contacts</td>
            </tr>
            <tr>
              <td>getuser</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>list</td>
              <td>read_contacts</td>
            </tr>
            <tr>
              <td>update</td>
              <td>write_contacts</td>
            </tr>
            <tr>
              <td>all</td>
              <td>read_contacts</td>
            </tr>
          </tbody>
        </table>
      </td>
    </tr>
    <tr>
      <td>calendar</td>
      <td>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Scope</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>delete</td>
              <td>write_calendar</td>
            </tr>
            <tr>
              <td>resolveuid</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>copy</td>
              <td>write_calendar</td>
            </tr>
            <tr>
              <td>get</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>getChangeExceptions</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>search</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>updates</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>freebusy</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>newappointments</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>has</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>new</td>
              <td>write_calendar</td>
            </tr>
            <tr>
              <td>list</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>update</td>
              <td>write_calendar</td>
            </tr>
            <tr>
              <td>all</td>
              <td>read_calendar</td>
            </tr>
            <tr>
              <td>confirm</td>
              <td>write_calendar</td>
            </tr>
          </tbody>
        </table>
      </td>
    </tr>
  </tbody>
</table>