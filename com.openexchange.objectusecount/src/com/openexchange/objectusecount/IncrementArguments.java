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
