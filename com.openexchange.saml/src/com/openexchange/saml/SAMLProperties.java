/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.saml;

import com.openexchange.config.lean.Property;

/**
 * Contains the keys of <code>saml.properties</code>.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public enum SAMLProperties implements Property {
    /**
     * Toggle to enable/disable the feature as a whole
     */
    ENABLED("enabled", Boolean.FALSE),
    /**
     * The binding via which logout responses are sent.
     */
    LOGOUT_RESPONSE_BINDING("logoutResponseBinding", "http-redirect"),
    /**
     * The URL of the assertion consumer service.
     */
    ACS_URL("acsURL"),
    /**
     * Whether single logout is supported.
     */
    ENABLE_SINGLE_LOGOUT("enableSingleLogout", Boolean.FALSE),
    /**
     * The URL of the single logout service.
     */
    SLS_URL("slsURL"),
    /**
     * The unique entity ID of the service provider.
     */
    ENTITY_ID("entityID"),
    /**
     * The human-readable name of the service provider.
     */
    PROVIDER_NAME("providerName"),
    /**
     * The unique entity ID of the identity provider.
     */
    IDP_ENTITY_ID("idpEntityID"),
    /**
     * The URL of the identity provider where authentication requests are redirected to.
     */
    IDP_LOGIN_URL("idpAuthnURL"),
    /**
     * The URL of the identity provider where logout responses are redirected/posted to.
     */
    IDP_LOGOUT_URL("idpLogoutURL"),
    /**
     * Whether the metadata service shall be enabled.
     */
    ENABLE_METADATA_SERVICE("enableMetadataService", Boolean.FALSE),
    /**
     * The HTML template to use when logout responses are sent to the IdP via HTTP POST.
     */
    LOGOUT_RESPONSE_POST_TEMPLATE("logoutResponseTemplate", "saml.logout.response.html.tmpl"),
    /**
     * Whether SAML-specific auto-login is enabled.
     */
    ENABLE_AUTO_LOGIN("enableAutoLogin", Boolean.TRUE),
    /**
     * Whether SAML unsolicited responses are enabled.
     */
    ALLOW_UNSOLICITED_RESPONSES("allowUnsolicitedResponses", Boolean.TRUE),
    /**
     * Whether SAML-specific auto-login is enabled, that uses the SessionIndex of the AuthnResponse.
     */
    ENABLE_SESSION_INDEX_AUTO_LOGIN("enableSessionIndexAutoLogin", Boolean.FALSE),
    /**
     * Starts a very dangerous debug backend
     */
    DEBUG_BACKEND("startVeryDangerousDebugBackend", Boolean.FALSE);

    private static final String PREFIX = "com.openexchange.saml.";

    private Object defValue;
    private String fqn;

    /**
     * Initializes a new {@link SAMLProperties} with an empty default value.
     *
     * @param name The name
     */
    private SAMLProperties(String name){
        this(name, null);
    }

    /**
     * Initializes a new {@link SAMLProperties}.
     *
     * @param name The name
     * @param defValue The default value
     */
    private SAMLProperties(String name, Object defValue){
        this.fqn = PREFIX + name;
        this.defValue = defValue;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return defValue;
    }

}
