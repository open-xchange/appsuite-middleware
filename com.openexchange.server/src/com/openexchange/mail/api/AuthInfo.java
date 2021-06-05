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

package com.openexchange.mail.api;

/**
 * {@link AuthInfo} - Provides authentication information; such as login, password and authentication type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class AuthInfo {

    private final String login;
    private final String password;
    private final AuthType authType;
    private final int oauthAccountId;

    /**
     * Initializes a new {@link AuthInfo}.
     *
     * @param login The login string
     * @param password The password or OAuth token
     * @param authType The authentication type
     * @param oauthAccountId The optional identifier of the associated OAuth account
     */
    public AuthInfo(String login, String password, AuthType authType, int oauthAccountId) {
        super();
        this.login = login;
        this.password = password;
        this.authType = null == authType ? AuthType.LOGIN : authType;
        this.oauthAccountId = oauthAccountId < 0 ? -1 : oauthAccountId;
    }

    /**
     * Gets the login
     *
     * @return The login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the authentication type
     *
     * @return The authentication type
     */
    public AuthType getAuthType() {
        return authType;
    }

    /**
     * Gets the optional identifier of the associated OAuth account
     *
     * @return The identifier of the associated OAuth account or <code>-1</code>
     */
    public int getOauthAccountId() {
        return oauthAccountId;
    }

}
