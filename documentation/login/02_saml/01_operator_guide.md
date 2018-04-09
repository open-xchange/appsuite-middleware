---
title: Operator Guide
---

# Installation

After development of the SAML backend is done you can setup OX App Suite as usual. Additionally you need to install `open-xchange-saml-core` and a package that provides `open-xchange-saml-backend` and contains your custom SAML backend. It is also necessary that a package providing `open-xchange-authentication` is installed. For your convenience you can install the metapackage `open-xchange-saml` which depends on a core and backend package. Finally the package for SAML support within App Suite UI is needed: `open-xchange-appsuite-saml`.

    $ apt-get install open-xchange-saml open-xchange-appsuite-saml


# Configuration

## Backend Configuration

The main configuration takes place in `/opt/open-xchange/etc/saml.properties`. Step through this file and set the property values. See [saml configuration](https://documentation.open-xchange.com/components/middleware/config/develop/index.html#mode=features&feature=Saml) for further explanations.

When multiple *SAML backends* are installed, it may be required to have individual configuration. Please refer to the specific *SAML backend* documentation if it supplies own configuration or relies upon `/opt/open-xchange/etc/saml.properties`.

As the lifetime of user sessions is under control of the SAML IdP, you must not activate autologin. It's currently not supported anyway. Make sure that `com.openexchange.sessiond.autologin` in `/opt/open-xchange/etc/sessiond.properties` is set to `false`. Also have a look at the other properties within that file. Its noteworthy that every refresh (e.g. closing the App Suite browser tab and opening it again at a later point) will create a new user session. So the lifetime of App Suite sessions should be short while a user should be able to acquire quite some sessions in parallel.

The path of the final login redirect that ends up in the web UI along with a valid session must be configured via `/opt/open-xchange/etc/server.properties`. If you haven't already (because of other requirements), set [com.openexchange.UIWebPath](/components/middleware/config{{ site.baseurl }}/index.html#com.openexchange.UIWebPath) to `/appsuite/`. App Suite is the only officially supported frontend for SAML authentication.

Note that session fail-over is currently not possible, because the central session storage only works with autologin enabled.

SAML sessions will not contain the users password. Thus it cannot be used to encrypt/decrypt secrets for external services (e.g. OAuth token). Configure `/opt/open-xchange/etc/secret.properties` to support other encryption/decryption mechanisms (e.g. "\<list\>").

The same problem exists for the primary mail account. Therefore the IMAP accounts must be accessible with a global master password. You need to configure this in `/opt/open-xchange/etc/mail.properties`


## Frontend Configuration

The frontend plugin `open-xchange-appsuite-saml` is deactivated by default. In order to enable the special SAML login handling, enable a samlLogin flag in `/opt/open-xchange/etc/as-config.yml`:

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
