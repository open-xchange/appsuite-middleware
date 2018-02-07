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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.auth.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * {@link AuthInfo} - Provides authentication information; such as login, password and authentication type.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class AuthInfo {

    /**
     * Creates a new builder instance.
     *
     * @return The builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected String login;
        protected String password;
        protected AuthType authType;
        protected Integer oauthAccountId;
        protected String token;

        protected Builder() {
            super();
        }

        public Builder setLogin(String login) {
            this.login = login;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setOauthAccountId(Integer oauthAccountId) {
            this.oauthAccountId = oauthAccountId;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setAuthType(AuthType authType) {
            this.authType = authType;
            return this;
        }

        public AuthInfo build() {
            return new AuthInfo(login, password, oauthAccountId, token, authType);
        }
    }

    // ----------------------------------------------------------------

    @JsonProperty("login")
    private final String login;
    @JsonProperty("password")
    private final String password;
    @JsonProperty("authType")
    private final AuthType authType;
    @JsonProperty("oauthAccountId")
    private final Integer oauthAccountId;
    @JsonProperty("token")
    private final String token;

    public static AuthInfo NONE = new AuthInfo(null, null, null, null, AuthType.NONE);

    /**
     * Initializes a new {@link AuthInfo}.
     *
     * @param login The login string
     * @param password The password or OAuth token
     * @param authType The authentication type
     * @param oauthAccountId The optional identifier of the associated OAuth account
     */
    @JsonCreator
    public AuthInfo(@JsonProperty("login") String login, @JsonProperty("password") String password, @JsonProperty("oauthAccountId") Integer oauthAccountId, @JsonProperty("token") String token, @JsonProperty("authType") AuthType authType) {
        super();
        this.login = login;
        this.password = password;
        this.token = token;
        this.authType = null == authType ? AuthType.NONE : authType;
        this.oauthAccountId = oauthAccountId;
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
    public Integer getOauthAccountId() {
        return oauthAccountId;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!AuthInfo.class.isInstance(obj)) {
            return false;
        }
        AuthInfo other = (AuthInfo) obj;
        if (authType != other.authType) {
            return false;
        }
        if (login == null) {
            if (other.login != null) {
                return false;
            }
        } else if (!login.equals(other.login)) {
            return false;
        }

        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }

        if (oauthAccountId == null) {
            if (other.oauthAccountId != null) {
                return false;
            }
        } else if (!oauthAccountId.equals(other.oauthAccountId)) {
            return false;
        }

        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((authType == null) ? 0 : authType.hashCode());
        result = prime * result + ((oauthAccountId == null) ? 0 : oauthAccountId.hashCode());
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        return result;
    }
}
