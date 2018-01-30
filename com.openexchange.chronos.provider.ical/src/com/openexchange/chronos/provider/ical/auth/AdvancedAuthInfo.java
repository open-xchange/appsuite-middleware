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

package com.openexchange.chronos.provider.ical.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openexchange.auth.info.AuthInfo;
import com.openexchange.auth.info.AuthType;

/**
 * {@link AdvancedAuthInfo}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class AdvancedAuthInfo extends AuthInfo {

    /**
     * Creates a new builder instance.
     *
     * @return The builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AuthInfo.Builder {

        private String encyrptedPassword;

        Builder() {
            super();
        }

        public Builder setEncryptedPassword(String encyrptedPassword) {
            this.encyrptedPassword = encyrptedPassword;
            return this;
        }

        @Override
        public Builder setLogin(String login) {
            this.login = login;
            return this;
        }

        @Override
        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        @Override
        public Builder setOauthAccountId(Integer oauthAccountId) {
            this.oauthAccountId = oauthAccountId;
            return this;
        }

        @Override
        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        @Override
        public Builder setAuthType(AuthType authType) {
            this.authType = authType;
            return this;
        }

        @Override
        public AdvancedAuthInfo build() {
            return new AdvancedAuthInfo(login, password, encyrptedPassword, oauthAccountId, token, authType);
        }
    }

    // ----------------------------------------------------------------

    private final String encryptedPassword;

    public static AdvancedAuthInfo NONE_ADVANCED = new AdvancedAuthInfo(null, null, null, null, null, AuthType.NONE);

    /**
     * Initializes a new {@link AdvancedAuthInfo}.
     *
     * @param login The login string
     * @param password The password or OAuth token
     * @param authType The authentication type
     * @param oauthAccountId The optional identifier of the associated OAuth account
     */
    @JsonCreator
    public AdvancedAuthInfo(@JsonProperty("login") String login, @JsonProperty("password") String password, String encryptedPassword, @JsonProperty("oauthAccountId") Integer oauthAccountId, @JsonProperty("token") String token, @JsonProperty("authType") AuthType authType) {
        super(login, password, oauthAccountId, token, authType);
        this.encryptedPassword = encryptedPassword;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if (!equals) {
            return false;
        }
        if (!AdvancedAuthInfo.class.isInstance(obj)) {
            return false;
        }
        AdvancedAuthInfo other = (AdvancedAuthInfo) obj;
        if (this.encryptedPassword == null) {
            if (other.encryptedPassword != null) {
                return false;
            }
        } else if (!encryptedPassword.equals(other.encryptedPassword)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        final int prime = 31;
        result = prime * result + ((getEncryptedPassword() == null) ? 0 : getEncryptedPassword().hashCode());
        return result;
    }

}
