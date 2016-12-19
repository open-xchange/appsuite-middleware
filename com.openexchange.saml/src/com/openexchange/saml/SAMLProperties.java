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


/**
 * Contains the keys of <code>saml.properties</code>.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLProperties {

    /**
     * Toggle to enable/disable the feature as a whole
     */
    public static final String ENABLED = "com.openexchange.saml.enabled";

    /**
     * The binding via which logout responses are sent.
     */
    public static final String LOGOUT_RESPONSE_BINDING = "com.openexchange.saml.logoutResponseBinding";

    /**
     * The URL of the assertion consumer service.
     */
    public static final String ACS_URL = "com.openexchange.saml.acsURL";

    /**
     * Whether single logout is supported.
     */
    public static final String ENABLE_SINGLE_LOGOUT = "com.openexchange.saml.enableSingleLogout";

    /**
     * The URL of the single logout service.
     */
    public static final String SLS_URL = "com.openexchange.saml.slsURL";

    /**
     * The unique entity ID of the service provider.
     */
    public static final String ENTITY_ID = "com.openexchange.saml.entityID";

    /**
     * The human-readable name of the service provider.
     */
    public static final String PROVIDER_NAME = "com.openexchange.saml.providerName";

    /**
     * The unique entity ID of the identity provider.
     */
    public static final String IDP_ENTITY_ID = "com.openexchange.saml.idpEntityID";

    /**
     * The URL of the identity provider where authentication requests are redirected to.
     */
    public static final String IDP_LOGIN_URL = "com.openexchange.saml.idpAuthnURL";

    /**
     * The URL of the identity provider where logout responses are redirected/posted to.
     */
    public static final String IDP_LOGOUT_URL = "com.openexchange.saml.idpLogoutURL";

    /**
     * Whether the metadata service shall be enabled.
     */
    public static final String ENABLE_METADATA_SERVICE = "com.openexchange.saml.enableMetadataService";

    /**
     * The HTML template to use when logout responses are sent to the IdP via HTTP POST.
     */
    public static final String LOGOUT_RESPONSE_POST_TEMPLATE = "com.openexchange.saml.logoutResponseTemplate";

    /**
     * Whether SAML-specific auto-login is enabled.
     */
    public static final String ENABLE_AUTO_LOGIN = "com.openexchange.saml.enableAutoLogin";

    /**
     * Whether SAML unsolicited responses are enabled.
     */
    public static final String ALLOW_UNSOLICITED_RESPONSES = "com.openexchange.saml.allowUnsolicitedResponses";

    /**
     * Whether SAML-specific auto-login is enabled, that uses the SessionIndex of the AuthnResponse.
     */
    public static final String ENABLE_SESSION_INDEX_AUTO_LOGIN = "com.openexchange.saml.enableSessionIndexAutoLogin";

}
