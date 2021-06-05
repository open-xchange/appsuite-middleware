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

import java.util.Set;
import com.openexchange.saml.spi.DefaultConfig;

/**
 * Contains configuration settings of the SAML feature.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 * @see DefaultConfig
 */
public interface SAMLConfig {

    public enum Binding {
        HTTP_REDIRECT, HTTP_POST;
    }

    /**
     * Gets the human-readable name of the service provider.
     *
     * @return The provider name. Never <code>null</code>.
     */
    String getProviderName();

    /**
     * Gets the entity ID of the service provider.
     *
     * @return The entity ID. Never <code>null</code>.
     */
    String getEntityID();

    /**
     * Gets the URL of the assertion consumer service (ACS).
     *
     * @return The ACS URL. Never <code>null</code>.
     */
    String getAssertionConsumerServiceURL();

    /**
     * Gets the URL of the single logout service.
     *
     * @return The URL or <code>null</code> if single logout is not enabled.
     */
    String getSingleLogoutServiceURL();

    /**
     * Gets the binding via which LogoutResponse messages shall be delivered.
     *
     * @return The binding or <code>null</code> if single logout is not configured.
     */
    Binding getLogoutResponseBinding();

    /**
     * Gets the entity ID of the identity provider.
     *
     * @return The ID. Never <code>null</code>.
     */
    String getIdentityProviderEntityID();

    /**
     * Gets the URL of the identity provider (IDP).
     *
     * @return The URL. Never <code>null</code>.
     */
    String getIdentityProviderAuthnURL();

    /**
     * Whether the single logout profile is enabled.
     *
     * @return <code>true</code> if the profile is enabled, otherwise false.
     */
    boolean singleLogoutEnabled();

    /**
     * The URL of the single logout service.
     *
     * @return The URL or <code>null</code>, if the profile is not supported.
     */
    String getIdentityProviderLogoutURL();

    /**
     * Whether the SPs metadata XML shall be made available via HTTP. The according
     * servlet will then be available under <code>http(s)://{hostname}/{prefix}/saml/metadata</code>.
     *
     * @return <code>true</code> if the servlet shall be registered, otherwise <code>false</code>.
     */
    boolean enableMetadataService();

    /**
     * The HTML template to use when logout responses are sent to the IdP via HTTP POST.
     *
     * @return The file name of the template which is located in the default template folder.
     */
    String getLogoutResponseTemplate();

    /**
     * Gets whether SAML-specific auto-login is enabled.
     *
     * @return <code>true</code> if auto-login is on.
     */
    boolean isAutoLoginEnabled();

    /**
     * Gets Whether SAML unsolicited responses are enabled.
     *
     * @return <code>true</code> if unsolicited responses are on.
     */
    boolean isAllowUnsolicitedResponses();

    /**
     * Gets whether SAML-specific auto-login is enabled, that uses the SessionIndex of the AuthnResponse.
     *
     * @return <code>true</code> if auto-login is on for SessionIndex.
     */
    boolean isSessionIndexAutoLoginEnabled();

    /**
     * Gets the hosts that this SAMLConfig is related to;
     * @return The Set of hosts or all, that this configuration should apply to
     */
    Set<String> getHosts();
}
