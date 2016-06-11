---
title: SAML 2.0 SSO Integration
---

# Introduction

Starting with 7.8.0 OX App Suite supports single sign-on via SAML 2.0. In concrete the backend supports the *Web Browser SSO Profile* and the *Single Logout Profile*, supported bindings are *HTTP-Redirect* and *HTTP-POST*. The core implementation of SAML needs always be complemented by an environment-specific counterpart. Such a counterpart is called a *SAML backend*. It is responsible for resolving users by incoming assertions and can make use of several extension points that allow to customize the generation and processing of SAML messages.


#Supported Message Flows

Every SAML request message (i.e. `<AuthnRequest>` or `<LogoutRequest>`) is sent via the *HTTP-Redirect* binding to a formerly configured endpoint. The binding for `<LogoutResponse>` messages, that are sent in response to IdP-initiated logout requests is configurable and can be either *HTTP-Redirect* or *HTTP-POST*. Messages of type `<Response>` are accepted via the *HTTP-POST* binding only, while `<LogoutRequest>` and `<LogoutResponse>` messages are accepted via *HTTP-Redirect* too.

Below are illustrations of the supported message flows. For reasons of clarity several query parameters like *SAMLRequest* or *RelayState* have been omitted.


## SP-initiated Login

![SP-initiated login flow](SAML_login_flow.png "SP-initiated login flow")


## SP-initiated Logout

![SP-initiated logout flow](SAML_sp_logout_flow.png "SP-initiated logout flow")


## IdP-initiated Logout

![IdP-initiated logout flow](SAML_idp_logout_flow.png "IdP-initiated logout flow")


# Developers Guide

The core implementation is contained in its own bundle `com.openexchange.saml` within the backend repository and relies on the OpenSAML library (https://wiki.shibboleth.net/confluence/display/OpenSAML). This guide assumes that you are familiar with the terminology and technological aspects of SAML 2.0.

## The SAML backend

A SAML backend consists at least of an implementation of `com.openexchange.saml.spi.SAMLBackend`. An instance of this implementation must be registered as OSGi service under this interface. It is considered best practice to start with inheriting from `com.openexchange.saml.spi.AbstractSAMLBackend` instead of implementing the interface directly. This reduces the number of methods to implement while default implementations are used where it is possible. You probably need to override some of the other methods as well to customize their behavior. Especially the validation of SAML responses needs likely to be adjusted, as the default strategy is very strict and will fail if the IdP does not obey the specification in every point. Start with reading the JavaDoc of the mentioned classes and follow their references to get an overview of what you need to implement. Additionally there is an example implementation in the `examples/backend-samples` repository that targets WSO2 Identity Server as IdP. The according project is `com.openexchange.saml.wso2`, its packaging information is contained in `open-xchange-saml-wso2`.

SAML might replace the web login of OX App Suite but it cannot be used by non-web clients that make use of the HTTP API directly. Therefore it uses sepcial login/logout calls instead of changing the behavior of the calls from the login module. While those calls are based on an installed authentication service (i.e. a package that provides `open-xchange-authentication`), SAML is not. Nevertheless an authentication service must always be provided. If you want SAML as your only login mechanims, you can simply register an instance of `com.openexchange.saml.spi.DisabledAuthenticationService` as OSGi service and let your package provide `open-xchange-authentication`. You can also decide to implement an own authentication service that takes care of authentication for other clients. It is also possible to install one of the existing `open-xchange-authentication` providers to allow e.g. IMAP or LDAP authentication.


# Operators Guide

## Installation

After development of the SAML backend is done you can setup OX App Suite as usual. Additionally you need to install `open-xchange-saml-core` and a package that provides `open-xchange-saml-backend` and contains your custom SAML backend. It is also necessary that a package providing `open-xchange-authentication` is installed. For your convenience you can install the metapackage `open-xchange-saml` which depends on a core and backend package. Finally the package for SAML support within App Suite UI is needed: `open-xchange-appsuite-saml`.

    $ apt-get install open-xchange-saml open-xchange-appsuite-saml


## Configuration

### Backend Configuration

The main configuration takes place in `/opt/open-xchange/etc/saml.properties`. Step through this file and set the property values in accordance to their explanation.

As the lifetime of user sessions is under control of the SAML IdP, you must not activate autologin. It's currently not supported anyway. Make sure that `com.openexchange.sessiond.autologin` in `/opt/open-xchange/etc/sessiond.properties` is set to `false`. Also have a look at the other properties within that file. Its noteworthy that every refresh (e.g. closing the App Suite browser tab and opening it again at a later point) will create a new user session. So the lifetime of App Suite sessions should be short while a user should be able to acquire quite some sessions in parallel.

The path of the final login redirect that ends up in the web UI along with a valid session must be configured via `/opt/open-xchange/etc/server.properties`. If you haven't already (because of other requirements), set `com.openexchange.UIWebPath` to `/appsuite/`. App Suite is the only officially supported frontend for SAML authentication.

Note that session fail-over is currently not possible, because the central session storage only works with autologin enabled.

SAML sessions will not contain the users password. Thus it cannot be used to encrypt/decrypt secrets for external services (e.g. OAuth token). Configure `/opt/open-xchange/etc/secret.properties` to support other encryption/decryption mechanisms (e.g. "\<list\>").

The same problem exists for the primary mail account. Therefore the IMAP accounts must be accessible with a global master password. You need to configure this in `/opt/open-xchange/etc/mail.properties`


### Frontend Configuration

The frontend plugin `open-xchange-appsuite-saml` is deactivated by default. In order to enable the special SAML login handling, enable a samlLogin flag in `/opt/open-xchange/etc/as-config.yml`:

    default:
        host: all
        samlLogin: true

**Please note:** Make sure to use spaces to indent, not tabs!
