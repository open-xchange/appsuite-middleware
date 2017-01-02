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

package com.openexchange.objectusecount;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * {@link IncrementArguments} - Specifies arguments to use when incrementing use count(s).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class IncrementArguments extends AbstractArguments {

    /**
     * A builder for an {@code IncrementArguments} instance.
     */
    public static class Builder {

        private final int objectId;
        private final int folderId;
        private final int userId;
        private final Collection<String> mailAddresses;
        private Connection con;
        private boolean throwException = false;

        /**
         * Initializes a new {@link IncrementArguments.Builder}.
         *
         * @param mailAddresses The mail addresses by which to look-up contacts to update
         */
        public Builder(Set<String> mailAddresses) {
            super();
            this.mailAddresses = mailAddresses;
            this.userId = -1;
            this.objectId = -1;
            this.folderId = -1;
        }

        /**
         * Initializes a new {@link IncrementArguments.Builder}.
         *
         * @param mailAddress The mail address by which to look-up the contact to update
         */
        public Builder(String mailAddress) {
            super();
            this.mailAddresses = Collections.singleton(mailAddress);
            this.userId = -1;
            this.objectId = -1;
            this.folderId = -1;
        }

        /**
         * Initializes a new {@link IncrementArguments.Builder}.
         *
         * @param userId The user identifier whose associated contact is supposed to be updated
         */
        public Builder(int userId) {
            super();
            this.userId = userId;
            this.mailAddresses = null;
            this.objectId = -1;
            this.folderId = -1;
        }

        /**
         * Initializes a new {@link IncrementArguments.Builder}.
         *
         * @param objectId The object identifier
         * @param folderId The folder identifier
         */
        public Builder(int objectId, int folderId) {
            super();
            this.objectId = objectId;
            this.folderId = folderId;
            this.mailAddresses = null;
            this.userId = -1;
        }

        /**
         * Sets whether an exception is supposed to be thrown or not. Default is <code>false</code>
         *
         * @param throwException The throwException to set
         * @return This builder
         */
        public Builder setThrowException(boolean throwException) {
            this.throwException = throwException;
            return this;
        }

        /**
         * Sets the connection
         *
         * @param con The connection to use or <code>null</code>
         * @return This builder
         */
        public Builder setCon(Connection con) {
            this.con = con;
            return this;
        }

        /**
         * Creates the appropriate {@code UpdateProperties} instance
         *
         * @return The instance
         */
        public IncrementArguments build() {
            return new IncrementArguments(con, mailAddresses, objectId, folderId, userId, throwException);
        }

    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    private final Collection<String> mailAddresses;
    private final int objectId;
    private final int userId;
    private final int folderId;

    protected IncrementArguments(Connection con, Collection<String> mailAddresses, int objectId, int folderId, int userId, boolean throwException) {
        super(con, throwException);
        this.mailAddresses = mailAddresses;
        this.userId = userId;
        this.objectId = objectId;
        this.folderId = folderId;
    }

    /**
     * Gets the mail addresses
     *
     * @return The mail addresses
     */
    public Collection<String> getMailAddresses() {
        return mailAddresses;
    }

    /**
     * Gets the object identifier
     *
     * @return The object identifier
     */
    public int getObjectId() {
        return objectId;
    }

    /**
     * Gets the folder identifier
     *
     * @return The folder identifier
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

}
