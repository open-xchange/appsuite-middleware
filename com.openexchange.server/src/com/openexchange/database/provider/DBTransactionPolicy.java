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

package com.openexchange.database.provider;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * A {@link DBTransactionPolicy} governs how transaction handling is done. Note: This swallows and logs exceptions.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface DBTransactionPolicy {

    /**
     * Do not partake in transaction handling, presumably to let something higher up in the call chain handle transactions.
     */
    public static final DBTransactionPolicy NO_TRANSACTIONS = new DBTransactionPolicy() {

        @Override
        public void commit(Connection con) {
            // Don't do a thing
        }

        @Override
        public void rollback(Connection con) {
            // Don't do a thing
        }

        @Override
        public void setAutoCommit(Connection con, boolean autoCommit) {
            // Don't do a thing
        }

    };

    /**
     * Partake in transaction handling normally. Just delegates to the corresponding methods on the connection.
     */
    public static final DBTransactionPolicy NORMAL_TRANSACTIONS = new DBTransactionPolicy() {

        @Override
        public void commit(Connection con) throws SQLException {
            con.commit();
        }

        @Override
        public void rollback(Connection con) throws SQLException {
            con.rollback();
        }

        @Override
        public void setAutoCommit(Connection con, boolean autoCommit) throws SQLException {
            con.setAutoCommit(autoCommit);
        }

    };

    public void setAutoCommit(Connection con, boolean autoCommit) throws SQLException;
    public void commit(Connection con) throws SQLException;
    public void rollback(Connection con) throws SQLException;
}
