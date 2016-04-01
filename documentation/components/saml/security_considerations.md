---
title: SAML 2.0 Security Considerations
---

# General Assumptions

The Open-Xchange SAML 2.0 implementation supports a subset of the bindings and profiles defined in the [specification](http://saml.xml.org/saml-specifications):

  * Profiles
    - Web Browser SSO Profile
    - Single Logout Profile
  * Bindings
    - HTTP-Redirect
    - HTTP-POST

For production usage the Open-Xchange SAML core implementation must always be supplemented by a customer-specific part. This part is responsible for most of the security-related aspects, i.e.

  * Providing keys for signing SAML messages and validation of signatures of incoming messages
  * Providing keys to decrypt incoming messages
  * Validation of authentication responses and assertions and providing the according user information
  * Validation of incoming logout requests (IdP-initiated single logout)
  * Validation of incoming logout responses (SP-initiated single logout)

Open-Xchange provides default tooling for all those cases to enable customers to behave as strict and standard-compliant as possible, but has no influence on actual implementations. For compatibility reasons customers can even implement non-standard-compliant behavior. Any provided digital certificates are not validated by the core implementation. It's in the customers responsibility to provide only verfied certificates and to validate certificates contained in incoming SAML messages.


# 3rd Party Dependencies

Open-Xchange uses several 3rd party libraries for (un-)marshalling SAML messages, encryption/decryption, creation and validation of digital signatures etc. Those are namely:

  * __Shibboleth Open SAML-Java v2.6.5:__ Comprehensive SAML tooling.
  * __Apache Santuario v1.5.8:__ Creation/validation of digital XML signatures and en-/decryption of SAML objects.
  * __Bouncy Castle Crypto APIs v1.5.1:__ Comprehensive cryptography toolkit.


# Authentication Flow

If the customer-specific part provides a signing key, every `<AuthnRequest>` is digitally signed. For every generated `<AuthnRequest>` some state is maintained internally to relate incoming `<Response>` messages to their original requests. The state is mapped to a unique identifier (128bit Java UUID) and passed around via the `RelayState` parameter. Incoming `<Response>` messages are only accepted if a valid `RelayState` parameter is part of the request and the message can therefore be related to a previously sent `<AuthnRequest>`. `<Response>` messages are only considered eligible if they arrive within 5 minutes after sending out the according `<AuthnRequest>`. The unique ID of every incoming `<Response>` message is remembered for 120 minutes to detect possible replay attacks within that time frame. The final validation of the `<Response>` message and the contained `Assertion` elements lies within the responsibility of the customer-specific part as well as the determination of the final user entity.

After a `<Response>` message has been validated and an according user entity has been determined, the user agent (i.e. the end-users web browser) is redirected to another endpoint where the final Open-Xchange login process is performed and a new session is created. This endpoint recognizes the requesting user by a formerly generated token consisting of two 128bit Java UUIDs. The token is linked with some server-side state and considered valid for one minute. The resulting session is treated as any other Open-Xchange session. All common measures like IP checks, cookie hashes, separation of session ID and cookies are applied on subsequent requests.
