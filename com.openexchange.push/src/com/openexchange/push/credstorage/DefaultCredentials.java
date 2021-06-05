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

package com.openexchange.push.credstorage;

import com.openexchange.session.Session;

/**
 * {@link DefaultCredentials} - The default credentials implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class DefaultCredentials implements Credentials {

    /**
     * Creates a new builder.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>DefaultCredentials</code> */
    public static class Builder {

        private int contextId;
        private int userId;
        private String password;
        private String login;

        Builder() {
            super();
        }

        /**
         * Copies arguments from given credentials
         *
         * @param credentials The credentials
         * @return This builder
         */
        public Builder copyFromCredentials(Credentials credentials) {
            contextId = credentials.getContextId();
            userId = credentials.getUserId();
            password = credentials.getPassword();
            login = credentials.getLogin();
            return this;
        }

        /**
         * Copies arguments from given session
         *
         * @param session The session
         * @return This builder
         */
        public Builder copyFromSession(Session session) {
            contextId = session.getContextId();
            userId = session.getUserId();
            password = session.getPassword();
            login = session.getLoginName();
            return this;
        }

        /**
         * Sets the context identifier
         *
         * @param contextId The context identifier to set
         * @return This builder
         */
        public Builder withContextId(int contextId) {
            this.contextId = contextId;
            return this;
        }

        /**
         * Sets the user identifier
         *
         * @param userId The user identifier to set
         * @return This builder
         */
        public Builder withUserId(int userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the password
         *
         * @param password The password to set
         * @return This builder
         */
        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the login
         *
         * @param login The login to set
         * @return This builder
         */
        public Builder withLogin(String login) {
            this.login = login;
            return this;
        }

        /**
         * Builds the instance of <code>DefaultCredentials</code> from this builde's arguments.
         *
         * @return The <code>DefaultCredentials</code> instance
         */
        public DefaultCredentials build() {
            return new DefaultCredentials(contextId, userId, password, login);
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final int contextId;
    private final int userId;
    private final String password;
    private final String login;

    /**
     * Initializes a new {@link DefaultCredentials}.
     */
    DefaultCredentials(int contextId, int userId, String password, String login) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.password = password;
        this.login = login;
    }

    /**
     * Initializes a new {@link DefaultCredentials}.
     *
     * @param credentials The source credentials
     */
    public DefaultCredentials(Credentials credentials) {
        super();
        contextId = credentials.getContextId();
        userId = credentials.getUserId();
        password = credentials.getPassword();
        login = credentials.getLogin();
    }

    /**
     * Initializes a new {@link DefaultCredentials}.
     *
     * @param session The source session
     */
    public DefaultCredentials(Session session) {
        super();
        contextId = session.getContextId();
        userId = session.getUserId();
        password = session.getPassword();
        login = session.getLoginName();
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("DefaultCredentials [userId=").append(userId).append(", contextId=").append(contextId).append(", ");
        if (password != null) {
            builder.append("password=").append(password).append(", ");
        } else {
            builder.append("no-password").append(", ");
        }
        if (login != null) {
            builder.append("login=").append(login);
        } else {
            builder.append("no-login");
        }
        builder.append(']');
        return builder.toString();
    }

}
