---
title: OX session glossary
icon: fas fa-cookie-bite
tags: Cookies, Authentication, Session, Security
---

# OX Session System Glossary

## Autologin

When we're talking about **autologin** we're referring to the system that **recovers and restarts** a users session after she did a page refresh, or closed the browser and later reopened it and the OX frontend. If you want to find out more about the session recovery system, please refer to [session lifecycle]({{site.baseurl}}/middleware/login_and_sessions/session_lifecycle.html).

## Auth-ID

The **auth-id** is a unique ID that allows **tracking login/logout requests** across the systems in your apache / OX cluster. It is useful for finding out which login request passed through which systems. 

## Cookie Hash

The **cookie hash** (not the other way around) is a **unique string identifier computed from certain aspects of the login request**. It is used to a) bind the session to certain client characteristics to prevent a session overtake and b) to allow session data of more than one session to be stored in the same cookie store (by providing unique names for the cookies). See also [session lifecycle]({{site.baseurl}}/middleware/login_and_sessions/session_lifecycle.html).

## Client

The **client** is a string identifier, used to **identify a client** that wants to use a session. For example the OX App Suite identifies itself as **open-xchange-appsuite**. The client is usually passed as a parameter to the login call and becomes one component of the **cookie hash**.

## Form login

The **form login** describes a login call that can be triggered by an external and custom form. If you want to provide your own login form, this is the way to go about it. You can read all the details in section 'Form Login'.

## IP Check

The **IP check** describes a **security check** the OX server uses on sessions. Upon session creation the clients IP address is stored along with the session data. Later accesses within the session must then come from the same IP address that created the session. This is used to make session takeovers harder. See also [session security features]({{site.baseurl}}/middleware/login_and_sessions/session_security_features.html).

## Session-ID

The **session id** is a unique string identifying the session. The session id, together with the session secret, is used to verify the authenticity of a session. In subsequent requests the session id will usually be transmitted as a **request parameter**.

## Session Secret

The **session secret**, along with the session id, is used to verify the authenticity of a session. It is always passed to the OX server as a cookie. Only when the session id parameter and the session secret cookie belong to the same session will a request be accepted by the OX backend. The cookie name has the format **open-xchange-secret-[hash]** with 'hash' being the **cookie hash**. If you want to know more about the session secret and how it is used to verify the authenticity of a session, please refer to [session lifecycle]({{site.baseurl}}/middleware/login_and_sessions/session_lifecycle.html) and [session security features]({{site.baseurl}}/middleware/login_and_sessions/session_security_features.html).
