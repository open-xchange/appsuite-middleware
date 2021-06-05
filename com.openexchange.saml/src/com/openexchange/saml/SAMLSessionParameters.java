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


/**
 * SAML specific keys of session properties.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLSessionParameters {

    /**
     * com.openexchange.saml.Authenticated
     * <p>
     * See also <code>com.openexchange.saml.oauth.OAuthFailedAuthenticationHandler.AUTHENTICATED</code>.
     */
    public static final String AUTHENTICATED = "com.openexchange.saml.Authenticated";

    /**
     * com.openexchange.saml.SubjectID
     */
    public static final String SUBJECT_ID = "com.openexchange.saml.SubjectID";

    /**
     * com.openexchange.saml.SessionNotOnOrAfter
     */
    public static final String SESSION_NOT_ON_OR_AFTER = "com.openexchange.saml.SessionNotOnOrAfter";

    /**
     * com.openexchange.saml.SessionIndex
     */
    public static final String SESSION_INDEX = "com.openexchange.saml.SessionIndex";

    /**
     * com.openexchange.saml.SessionCookie
     */
    public static final String SESSION_COOKIE = "com.openexchange.saml.SessionCookie";

    /**
     * com.openexchange.saml.AccessToken
     */
    public static final String ACCESS_TOKEN = "com.openexchange.saml.AccessToken";

    /**
     * com.openexchange.saml.RefreshToken
     */
    public static final String REFRESH_TOKEN = "com.openexchange.saml.RefreshToken";

    /**
     * com.openexchange.saml.SamlPath
     */
    public static final String SAML_PATH = "com.openexchange.saml.SamlPath";

    /**
     * com.openexchange.saml.SingleLogout
     */
    public static final String SINGLE_LOGOUT = "com.openexchange.saml.SingleLogout";

    // In case of adding new constants here; see SAMLSessionStorageParameterNamesProvider!
}
