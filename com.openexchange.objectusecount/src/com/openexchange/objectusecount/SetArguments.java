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

/**
 * {@link SetArguments} - Specifies arguments to use when setting use count(s).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class SetArguments extends AbstractArguments {

    /**
     * A builder for an {@code IncrementArguments} instance.
     */
    public static class Builder {

        private final int objectId;
        private final int folderId;
        private final int value;
        private Connection con;
        private boolean throwException = false;

        /**
         * Initializes a new {@link SetArguments.Builder}.
         *
         * @param objectId The object identifier
         * @param folderId The folder identifier
         */
        public Builder(int objectId, int folderId, int value) {
            super();
            this.objectId = objectId;
            this.folderId = folderId;
            this.value = value;
        }

        /**
         * Sets whether an exception is supposed to be thrown or not. Default is <code>false</code>.
         *
         * @param throwException The throwException to set
         * @return This builder
         */
        public Builder setThrowException(boolean throwException) {
            this.throwException = throwException;
            return this;
        }

        /**
         * Sets the connection.
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
        public SetArguments build() {
            return new SetArguments(con, objectId, folderId, value, throwException);
        }

    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    private final int objectId;
    private final int value;
    private final int folderId;

    SetArguments(Connection con, int objectId, int folderId, int value, boolean throwException) {
        super(con, throwException);
        this.value = value;
        this.objectId = objectId;
        this.folderId = folderId;
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
     * Gets the value
     *
     * @return The value
     */
    public int getValue() {
        return value;
    }

}
