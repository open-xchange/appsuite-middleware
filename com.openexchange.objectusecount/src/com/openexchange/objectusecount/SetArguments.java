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
