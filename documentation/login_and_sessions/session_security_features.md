---
title: OX session security features
icon: fas fa-cookie-bite
tags: Cookies, Authentication, Session, Security
---

# Security features in session handling

This page gives an overview of the security features of OX session handling, and the ways to configure them. Since a session represents an authenticated user it's important that we make sure a session is only used by the person having created it. We ensure this in a variety of ways. 

## Use HTTPS

This is probably the most important hint. When using HTTPS the traffic between the browser and backend is encrypted, and unless someone has the servers private key on hand, no one can snoop around in the headers and body of the request. Also when using HTTPS the OX server marks its cookies so that they may ONLY be transferred using an HTTPS transport. This will protect the **Session Secret** and the **Session ID** from being read by others. 

## IPCheck

When the **IP Check** is enabled, the OX server records the IP address of the client that created the session. Only requests coming from that IP address may then use the session. This is great for protecting the session from being taken over by someone else. It falls a little short when the accesses are bundled up by a proxy and becomes a nuisance when the client frequently changes IP addresses (mobile phones that roam a lot being the canonical example). In any case we suggest leaving the IP check active. The file **server.properties** contains the configuration for this:

`com.openexchange.IPCheck=true`


## HTTP Only Cookies

Normally code running in the browser may access the cookies for the domain the page was loaded from. To urge a browser not to pass cookies on to javascript code, cookies may contain the Http-Only flag. Since the OX cookies are only relevant for the communication of the browser with the server (the frontend need not know their content), it's wise to set this flag. Sometimes, though, browsers may balk at this property, so you have the option of turning this off, though normally, you shouldn't be required to do so. Again, this can be configured in the **server.properties**:

`com.openexchange.cookie.httpOnly=true`
  

## Cookie Hash

The **cookie hash** has a dual role:

1. It prevents OX cookies from overwriting each other when two clients use the same cookie store (e.g. the browser itself and a browser plugin)
2. It serves as a fingerprint of the client

The **cookie hash** is usually calculated using the **client** parameter and the **User-Agent** header. The **client** is then stored along with the session in the OX backend. When another request comes along, the hash is recalculated using the **User-Agent** header of the new request and the stored **client** value. It therefore verifies that the User-Agent header is the same. You can finetune this process a bit. 

The first thing you can influence is, which headers are used in constructing the cookie hash. **User-Agent** is a given, further headers can be added using the **com.openexchange.cookie.hash.fields** parameter. It contains a comma separated list of header names to be included in the hash calculation. Say, for example, your apache configuration creates a client fingerprint and sets the header X-Security-ClientFingerprint (a hypothetical example), and you want to OX server to use this when verifying the session integrity. Open up the **server.properties** and set the following configuration option:

`com.openexchange.cookie.hash.fields=X-Security-ClientFingerprint`

The OX server will then use the User-Agent header, the client parameter and the X-Security-ClientFingerprint header to ensure the session is only used by the same client.

The second thing you can configure is whether the hash value should be calculated and checked on every request. In essence setting this property to anything else than **calculate** removes the second role of the cookie hash. It might be needed if you use an exotic authentication mechanism, where sessions are created by a different client than the one using the session, or if clients for one reason or another switch the value of the User-Agent header during a session. The configuration parameter **com.openexchange.cookie.hash** can be set either to **calculate** (the default), meaning the fingerprinting mechanism will be used or **remember** meaning only the first role will be fulfilled. Again, this can be set in the file **server.properties**:

`com.openexchange.cookie.hash=calculate`

For security reasons the cookie hash is salted by default to avoid a potential brute force attack to cookie hashes. It is highly recommended to replace the default value with any random string with at least 16 characters. The value must be the same on all running middleware nodes. Again, this can be set in the file **server.properties**:

`com.openexchange.cookie.hash.salt=replaceMe1234567890`
