---
title: Operator Guide
icon: fa-book
tags: SAML, Installation, Configuration, SSO
---

# Installation

The SAML integration is provided by the `open-xchange-saml-core` package and requires an additional package that provides `open-xchange-saml-backend` and contains your custom SAML backend. It is also necessary that a package providing `open-xchange-authentication` is installed. For your convenience you can install the metapackage `open-xchange-saml` which depends on a core and backend package.


# Configuration

## Backend Configuration

The main configuration takes place in `/opt/open-xchange/etc/saml.properties`. Step through this file and set the property values. See [saml configuration](https://documentation.open-xchange.com/components/middleware/config/develop/index.html#mode=features&feature=Saml) for further explanations.

When multiple *SAML backends* are installed, it may be required to have individual configuration. Please refer to the specific *SAML backend* documentation if it supplies own configuration or relies upon `/opt/open-xchange/etc/saml.properties`.

The path of the final login redirect that ends up in the web UI along with a valid session must be configured via `/opt/open-xchange/etc/server.properties`. If you haven't already (because of other requirements), set [com.openexchange.UIWebPath](/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.UIWebPath) to `/appsuite/`.

SAML sessions will not contain the users password. Thus it cannot be used to encrypt/decrypt secrets for external services (e.g. OAuth token). Configure `/opt/open-xchange/etc/secret.properties` to support other encryption/decryption mechanisms (e.g. "\<list\>").

The same problem exists for the primary mail account. Therefore the IMAP accounts must be accessible with a global master password or OAuth tokens. You need to configure this in `/opt/open-xchange/etc/mail.properties`


### Autologin

By default the SAML autologin mechanism is enabled. So an existing web session is reused when an user refreshes the page. To disable the SAML autologin mechanism disable the `com.openexchange.saml.enableAutoLogin` property. When disabled, every page refresh from the user will initiate a new IdP roundtrip, create a new user session and will cause additional load on the IDM. This works independently from the usual App Suite autologin behavior, which is not considered in SAML scenarios.

Furthermore it is possible to re-enter an existing App Suite session when returning from an IdP login roundtrip, if the according IdP session also was still active. This is realized by relating the `SessionIndex` attribute of the SAML assertion to the App Suite session that was created as a result of it. The `SessionIndex` attribute is stored within App Suite sessions and can be used to look-up and re-use an existing session created with the same `SessionIndex` value before. The mechanism relies on the HTTP sticky session, i.e. an App Suite session can only be re-used if the IdP-to-SP redirect ends up on the same middleware node where an according session has been created before. The config setting to enable this behavior is `com.openexchange.saml.enableSessionIndexAutoLogin`.


## Frontend Configuration

The frontend SAML plugin is deactivated by default. In order to enable the special SAML login handling, enable a samlLogin flag in `/opt/open-xchange/etc/as-config.yml`:

    default:
        host: all
        samlLogin: true

**Please note:** Make sure to use spaces to indent, not tabs!

## IdP Configuration

Unsolicited responses or IdP-initiated login does support additional parameters carried by the *RelayState*. These parameters can either be handled by a custom SAML Backend or must be in the following form. A base64 encoded string that may hold any of the following key-values, split by `:` where each key uses a `=` split from its value:

* domain
* loginpath
* client

Example `domain=example.com:client=specialClient`
Encoded RelayState = `ZG9tYWluPWV4YW1wbGUuY29tOmNsaWVudD1zcGVjaWFsQ2xpZW50`
