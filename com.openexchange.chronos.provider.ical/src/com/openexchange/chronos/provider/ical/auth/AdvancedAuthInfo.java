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
