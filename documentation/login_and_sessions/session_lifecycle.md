---
title: OX session lifecycle
icon: fas fa-cookie-bite
tags: Session
---

# Lifecycle of an OX Session

This page describes in detail how a session is created, the components of a session and how they work together and which stages a session goes through during its existence. 

## Creating a session

Creating a session is a very straight forward process. Let's fire up a network sniffer and see what's exchanged between client and server during login (redacted for brevity, some of the stuff that's sent back and forth is not really relevant to our discussion):

```
POST /ajax/login?action=login&client=open-xchange-appsuite&staySignedIn=true HTTP/1.1
Host: localhost
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:70.0) Gecko/20100101 Firefox/70.0
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

name=username%40contextname&password=somePassword
```

Let's go through this one thing at a time. Look at the first line: 

`POST /ajax/login?action=login&client=open-xchange-appsuite&staySignedIn=true HTTP/1.1`

It's an HTTP POST that wants to do a login ( /ajax/login?action=login ). 

`POST /ajax/login?action=login&client=open-xchange-appsuite&staySignedIn=true HTTP/1.1`

The first thing we have to look at is the client identifier (&client=...). The client identifier is `open-xchange-appsuite`. The client identifier is used to differentiate between different client programs that use the same cookie store. For example one session in your browser is opened by the OX App Suite and another one by a browser plugin. In order for the cookies used in session handling not overwriting each other, each client program provides its own client identifier, that is later used to construct the cookie names by way of the **Cookie Hash**.

`POST /ajax/login?action=login&client=open-xchange-appsuite&staySignedIn=true HTTP/1.1`

The second thing that jumps out is the `staySignedIn` parameter. This parameter affects the lifetime of the created session and its cookies. If left away or set to `false`, the session will time-out after 60 inactive minutes (default, can be controlled by `com.openexchange.sessiond.sessionDefaultLifeTime`) and the cookies will be set without TTL, meaning they will be gone if the web browser is closed. If set to `true`, the session will last for an idle time as long as `com.openexchange.sessiond.sessionLongLifeTime`. The cookies are decorated with that configured TTL and can be restored via the `autologin` call.

`User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:70.0) Gecko/20100101 Firefox/70.0`

The User-Agent header is also used in constructing the cookie names used, or more precisely the **Cookie Hash**. 

`name=username%40contextname&password=somePassword`

This line contains the parameters sent in the POST request. "name" is set to "username@contextname", and the password is set to "somePassword". This information will be used by the authentication system to authenticate the user or deny the login request. Let's say everything goes right with our login attempts and look at the servers answer, again redacted for brevity and relevance to our discussion:

```
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8
Set-Cookie: open-xchange-secret-IaxgQLSrL7j1zE1Yceasg=1c0c09bc54564dcabf0c8b9a21d8fc59; Expires=Wed, 20-Nov-2019 15:40:56 GMT; Path=/; Secure; HttpOnly
Set-Cookie: open-xchange-public-session-OF7B6xxx3jbjvxmcsELGdA=17bc398e29724e308c3f9bd25ae64ac8; Expires=Wed, 20-Nov-2019 15:40:56 GMT; Path=/; Secure; HttpOnly
Set-Cookie: open-xchange-session-IaxgQLSrL7j1zE1Yceasg=4eea89150dab48f683a34e6cbe7e7aca; Expires=Wed, 20-Nov-2019 15:40:56 GMT; Path=/; Secure; HttpOnly
Transfer-Encoding: chunked

90e
{"session":"4eea89150dab48f683a34e6cbe7e7aca", ... }
0
```

The answer provides the client with the two parts it later needs to construct valid requests: The **Session Secret** and the **Session ID**. As you can see in this line, the session secret and ID are transferred as a cookie:

```
Set-Cookie: open-xchange-secret-IaxgQLSrL7j1zE1Yceasg=1c0c09bc54564dcabf0c8b9a21d8fc59; Expires=Wed, 20-Nov-2019 15:40:56 GMT; Path=/; Secure; HttpOnly
Set-Cookie: open-xchange-session-IaxgQLSrL7j1zE1Yceasg=4eea89150dab48f683a34e6cbe7e7aca; Expires=Wed, 20-Nov-2019 15:40:56 GMT; Path=/; Secure; HttpOnly
Transfer-Encoding: chunked
```

Notice the name of the cookies, which always starts with `open-xchange-secret-` and `open-xchange-session-` followed by the **cookie hash** that is normally calculated from the User-Agent header of the login request, and the value of the client parameter and an additional hash to prevent brute force attacks on cookie names. The expiry time of the cookie is goverened by the cookie lifetime configuration parameter (`com.openexchange.cookie.ttl`) and whether `staySignedIn` is requested or not. As I tried this request on Wed, 13-Nov-2019 with `staySignedIn` set to `true`, the cookie will live one week.

If `staySignedIn` was set to `false`, the *Set-Cookie* lines would look like this, the *Expires* parameter is not set, those cookies will be removed when closing the browser:

```
Set-Cookie: open-xchange-secret-IaxgQLSrL7j1zE1Yceasg=6cbe0ebe261c4cbab501f176c8a4caad; Path=/; Secure; HttpOnly
Set-Cookie: open-xchange-public-session-OF7B6xxx3jbjvxmcsELGdA=87840b5ae9c445308c80944e66411fe3; Path=/; Secure; HttpOnly
Set-Cookie: open-xchange-session-IaxgQLSrL7j1zE1Yceasg=8b254fed8d30454882468c56137b3b40; Path=/; Secure; HttpOnly
```

The **Session ID** is also transmitted to the client in the response:

`{"session":"4eea89150dab48f683a34e6cbe7e7aca", ... }`

This **session id** will later be sent to the server in all requests as the `session` parameter.

## Using a session 

Let's look at the next request the UI performs:

```
GET /ajax/folders?action=list&all=0&altNames=true&parent=default0&timezone=UTC&tree=0&session=4eea89150dab48f683a34e6cbe7e7aca&columns=...
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:70.0) Gecko/20100101 Firefox/70.0
Cookie: ... JSESSIONID=1397204484404800359.OX1; open-xchange-secret-IaxgQLSrL7j1zE1Yceasg=1c0c09bc54564dcabf0c8b9a21d8fc59; open-xchange-public-session-OF7B6xxx3jbjvxmcsELGdA=17bc398e29724e308c3f9bd25ae64ac8; open-xchange-session-IaxgQLSrL7j1zE1Yceasg=4eea89150dab48f683a34e6cbe7e7aca
```

This requests retrieves certain configuration data. Notice the **Session ID** that is transferred as a request parameter and the **Session Secret** that is transferred as a cookie. Only when these two are part of the same session, will the request be accepted.

## Retrieving a session with autologin

One of the first things the OX App Suite does after having been loaded by your browser is trying to revive a possibly existing session. For this, the OX App Suite issues the autologin call:

```
GET /ajax/login?action=autologin&client=open-xchange-appsuite HTTP/1.1
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:70.0) Gecko/20100101 Firefox/70.0
Cookie: ... JSESSIONID=1397204484404800359.OX1; open-xchange-secret-IaxgQLSrL7j1zE1Yceasg=1c0c09bc54564dcabf0c8b9a21d8fc59; open-xchange-public-session-OF7B6xxx3jbjvxmcsELGdA=17bc398e29724e308c3f9bd25ae64ac8; open-xchange-session-IaxgQLSrL7j1zE1Yceasg=4eea89150dab48f683a34e6cbe7e7aca

HTTP/1.1 200 OK
Content-Type:application/json; charset=UTF-8
Pragma: no-cache
Cache-Control: post-check=0, pre-check=0
Keep-Alive: timeout=5, max=99
Connection: Keep-Alive
Transfer-Encoding: chunked

976
{"session":"4eea89150dab48f683a34e6cbe7e7aca", ... }
```

Note the cookie `open-xchange-session-IaxgQLSrL7j1zE1Yceasg` containing the **Session ID** is used to retrieve the session. If a session could be found it is returned, much like a regular login response.

## Session hibernation for long running sessions

OX App Suite supports long running sessions, that can be revived even after many days. To facilitate that, sessions that are not in active use are slimmed down and placed in a kind of 'hibernation mode'. Autologin can still reactivate these sessions while they don't take up as much memory as a regular active sessions. You can fine tune when a session is sent into hibernation, and when a session is discarded in the [configuration](https://documentation.open-xchange.com/components/middleware/config{{site.baseurl}}#mode=tags&tag=Session).

### Session rotation

The default OX App Suite configuration sets the system up for long running sessions, that can be recovered. Which configuration options make this happen? Glad you asked. Let's first look at the file `sessiond.properties`:

```
 #
 # sessiond.properties
 #
 
 # Maximum value in milliseconds a session is allowed to be kept without request. After this time the session is put into the long life time
 # container and all temporary session data is removed.
 com.openexchange.sessiond.sessionDefaultLifeTime=3600000
 
 # This amount of time a session can life in the long life time container. The session can be restored from here but it won't have any
 # temporary session data anymore. Restoring temporary session data may cause some slower functionality and maybe temporary errors on image,
 # proxy data or the like. Can contain units of measurement: D(=days) W(=weeks) H(=hours) M(=minutes).
 com.openexchange.sessiond.sessionLongLifeTime=1W
```

The session containers still need to be explained. There are two types of them, *short-term containers* and *long-term containers*. Directly after creating a session, it is put in the first *short-term container*. Every 6 minutes those sessions in short-term containers are rotated, meaning a new empty short-term container is created, put in first position and all other containers' position is incremented by 1. If a session is used, it gets moved into the (new) first container again. `com.openexchange.sessiond.sessionDefaultLifeTime` configures how long a session should stay inactive in short-term containers, which is one hour by default, leading to 10 short-term containers. When rotated, sessions in the formerly 10th short-term container are stripped from temporary data and put into hibernation in the first *long-term container*. So, for an inactive session, there are 10 short-term containers and, depending on when the containers get rotated after session creation, between 54 and 60 minutes until put into hibernation.

Sessions in hibernation can still be restored, but restoring temporary session data may cause some slower functionality and maybe temporary errors on image, proxy data or the like. Session inactive lifetime for the long-term containers can be configured in property `com.openexchange.sessiond.sessionLongLifeTime`, which is one week by default. Long-term containers are rotated in a similar way to the short-term containers, but only after one hour. There are 168 hours in one week, but the first hour is taken by short-term containers, so there are 167 remaining long-term containers for the session to move around before being finally removed when reaching the last long-term container.

Of course it is meaningful to coordinate the values of `com.openexchange.sessiond.sessionLongLifeTime`, defining the maximum session idle time and `com.openexchange.cookie.ttl` in *server.properties*, defining the expiry date of (session-) cookies.

## Closing a session

Finally, and for completeness sake, a session can be closed by issuing a 'logout' request:

```
GET /ajax/login?action=logout&session=1603f458d1c94869a69df2654d981a45 HTTP/1.1
Host: localhost
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:70.0) Gecko/20100101 Firefox/70.0
Content-Type: application/x-www-form-urlencoded
Cookie: ... JSESSIONID=1397204484404800359.OX1; open-xchange-secret-IaxgQLSrL7j1zE1Yceasg=0417958f2729482b988051b96cf59fea; open-xchange-public-session-OF7B6xxx3jbjvxmcsELGdA=320e21856b3940569582d5013e0f7726; open-xchange-session-IaxgQLSrL7j1zE1Yceasg=1603f458d1c94869a69df2654d981a45

HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8
Pragma: no-cache
Cache-Control: post-check=0, pre-check=0
Set-Cookie: open-xchange-session-IaxgQLSrL7j1zE1Yceasg=invalid; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/; HttpOnly
Set-Cookie: open-xchange-secret-IaxgQLSrL7j1zE1Yceasg=invalid; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/; HttpOnly
Set-Cookie: open-xchange-public-session-OF7B6xxx3jbjvxmcsELGdA=invalid; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/; HttpOnly
Set-Cookie: JSESSIONID=1397204484404800359.OX1; Expires=Thu, 01-Jan-1970 00:00:10 GMT; Path=/; HttpOnly

Content-Length: 0
Keep-Alive: timeout=5, max=90
Connection: Keep-Alive
```

Notice that the session secret cookie and the session id cookie are removed.
