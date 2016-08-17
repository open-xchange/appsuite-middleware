/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.saml;

import com.openexchange.saml.impl.DefaultConfig;

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

}
