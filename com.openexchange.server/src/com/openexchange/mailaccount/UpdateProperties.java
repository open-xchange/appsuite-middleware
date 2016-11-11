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
