---
title: Session Cookies
---

Belows table describes all cookies that may be set by App Suite Middleware to
maintain user session state across HTTP requests. The information in this article
can be used to answer according GDPR inquiries.

**Important:** This article does not list all possible set cookies for OX App
Suite. There might be more that are set by other components, like App Suite UI
and custom integrations (especially authentication plugins and advertisement
integrations).

| Name                               | Example Value                    | Domain    | Path | Lifetime     | Secure? | HTTP only? | Description                                                                                                                                                           | Contains PII? |
|------------------------------------|----------------------------------|-----------|------|--------------|:-------:|:----------:|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| JSESSIONID                         | 2648017316273288615.OX1          | <webmail> | /    | user session |   yes   |     yes    | Identifies the target node in an application cluster and the according HTTP session therein.                                                                          |       no      |
| open-xchange-secret-<hash>         | 1b9910afdf4642039adf41750d3e6079 | <webmail> | /    | user session |   yes   |     yes    | Identifies a user session within an application cluster. Is not alone sufficient to perform requests, it always needs a session ID as part of the request parameters. |       no      |
| open-xchange-public-session-<hash> | b5f9f78dfc5f45d5b791bc37cd972fb0 | <webmail> | /    | user session |   yes   |     yes    | Identifies a user session within an application cluster. Grants access to a limited set of resources without a session ID as part of the request parameters.          |       no      |
| open-xchange-session-<hash>        | 0feaa4e6f4ac43c08d434f1328d5f9f9 | <webmail> | /    | user session |   yes   |     yes    | Contains a session ID that is used to re-establish an existing session as part of the auto-login mechanism.                                                           |       no      |
| open-xchange-shard                 | default                          | <webmail> | /    | user session |   yes   |     yes    | Identifies an application cluster in a potentially multi-shard environment.                                                                                           |       no      |
| open-xchange-saml-<hash>           | 3b5ca0a4e1ed46d28cfaf415b77075c8 | <webmail> | /    | user session |   yes   |     yes    | Contains a session lookup key that is used to re-establish an existing session as part of the auto-login mechanism for SAML SSO.                                      |       no      |
| open-xchange-oidc-<hash>           | f820fa31fe824b31bb34955a03176425 | <webmail> | /    | user session |   yes   |     yes    | Contains a session lookup key that is used to re-establish an existing session as part of the auto-login mechanism for OpenID Connect SSO.                            |       no      |

**Legend:**

 * `<hash>`: A hash value based on certain HTTP request parameters that are supposed to be unique per user session.
 * `<webmail>`: Domain under which the App Suite web interface is directly available, e.g. `webmail.example.com`.