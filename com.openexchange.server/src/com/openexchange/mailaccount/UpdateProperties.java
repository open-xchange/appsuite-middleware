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

package com.openexchange.mailaccount;

import java.sql.Connection;
import com.openexchange.session.Session;

/**
 * {@link UpdateProperties} - Specifies additional properties to consider during an update operation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class UpdateProperties {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder instance initialized with values from given update properties.
     *
     * @param updateProperties The update properties to initialize from
     * @return The new builder instance
     */
    public static Builder builder(UpdateProperties updateProperties) {
        Builder builder = new Builder();
        if (null != updateProperties) {
            builder.setChangePrimary(updateProperties.isChangePrimary());
            builder.setChangeProtocol(updateProperties.isChangeProtocol());
            builder.setCon(updateProperties.getCon());
            builder.setSession(updateProperties.getSession());
        }
        return builder;
    }

    /**
     * A builder for an {@code UpdateProperties} instance.
     */
    public static class Builder {

        private Session session;
        private Connection con;
        private boolean changePrimary;
        private boolean changeProtocol;

        /**
         * Initializes a new {@link UpdateProperties.Builder}.
         */
        public Builder() {
            super();
        }

        /**
         * Sets the session
         *
         * @param session The session to set
         */
        public Builder setSession(Session session) {
            this.session = session;
            return this;
        }

        /**
         * Sets the connection
         *
         * @param con The connection to use or <code>null</code>
         */
        public Builder setCon(Connection con) {
            this.con = con;
            return this;
        }

        /**
         * Sets whether primary account is allowed to be changed
         *
         * @param changePrimary <code>true</code> if primary account is allowed to be changed; else <code>false</code>
         */
        public Builder setChangePrimary(boolean changePrimary) {
            this.changePrimary = changePrimary;
            return this;
        }

        /**
         * Sets whether mail/transport protocol is allowed to be changed
         *
         * @param changeProtocol <code>true</code> if mail/transport protocol is allowed to be changed; else <code>false</code>
         */
        public Builder setChangeProtocol(boolean changeProtocol) {
            this.changeProtocol = changeProtocol;
            return this;
        }

        /**
         * Creates the appropriate {@code UpdateProperties} instance
         *
         * @return The instance
         */
        public UpdateProperties build() {
            return new UpdateProperties(session, con, changePrimary, changeProtocol);
        }

    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    private final Session session;
    private final Connection con;
    private final boolean changePrimary;
    private final boolean changeProtocol;

    UpdateProperties(Session session, Connection con, boolean changePrimary, boolean changeProtocol) {
        super();
        this.session = session;
        this.con = con;
        this.changePrimary = changePrimary;
        this.changeProtocol = changeProtocol;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the connection
     *
     * @return The connection or <code>null</code>
     */
    public Connection getCon() {
        return con;
    }

    /**
     * Checks whether primary account is allowed to be changed
     *
     * @return <code>true</code> if primary account is allowed to be changed; otherwise <code>false</code>
     */
    public boolean isChangePrimary() {
        return changePrimary;
    }

    /**
     * Checks whether mail/transport protocol is allowed to be changed
     *
     * @return <code>true</code> if mail/transport protocol is allowed to be changed; otherwise <code>false</code>
     */
    public boolean isChangeProtocol() {
        return changeProtocol;
    }

}
