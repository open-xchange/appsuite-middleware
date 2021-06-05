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

package com.openexchange.nosql.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.nosql.cassandra.exceptions.CassandraServiceExceptionCodes;

/**
 * {@link CassandraServices} - A utility class for Cassandra service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class CassandraServices {

    /**
     * Initializes a new {@link CassandraServices}.
     */
    private CassandraServices() {
        super();
    }

    /**
     * Executes given query using specified session.
     *
     * @param query The query to execute
     * @param session The session to use
     * @return The result of the query. That result will never be <code>null</code>, but can be empty (and will be for any non <code>SELECT</code> query).
     * @throws OXException If executing the query fails
     */
    public static ResultSet executeQuery(String query, Session session) throws OXException {
        if (Strings.isEmpty(query) || null == session) {
            return null;
        }

        try {
            return session.execute(query);
        } catch (com.datastax.driver.core.exceptions.NoHostAvailableException e) {
            throw CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE_SIMPLE.create(e, new Object[0]);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            throw CassandraServiceExceptionCodes.QUERY_EXECUTION_ERROR.create(e, query);
        } catch (com.datastax.driver.core.exceptions.QueryValidationException e) {
            throw CassandraServiceExceptionCodes.QUERY_VALIDATION_ERROR.create(e, query);
        } catch (com.datastax.driver.core.exceptions.DriverException e) {
            throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Executes given query using specified session.
     *
     * @param statemnent The statement to execute
     * @param session The session to use
     * @return The result of the query. That result will never be <code>null</code>, but can be empty (and will be for any non <code>SELECT</code> query).
     * @throws OXException If executing the query fails
     */
    public static ResultSet executeQuery(Statement statemnent, Session session) throws OXException {
        if (null == statemnent || null == session) {
            return null;
        }

        try {
            return session.execute(statemnent);
        } catch (com.datastax.driver.core.exceptions.NoHostAvailableException e) {
            throw CassandraServiceExceptionCodes.CONTACT_POINTS_NOT_REACHABLE_SIMPLE.create(e, new Object[0]);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            throw CassandraServiceExceptionCodes.QUERY_EXECUTION_ERROR.create(e, statemnent);
        } catch (com.datastax.driver.core.exceptions.QueryValidationException e) {
            throw CassandraServiceExceptionCodes.QUERY_VALIDATION_ERROR.create(e, statemnent);
        } catch (com.datastax.driver.core.exceptions.DriverException e) {
            throw CassandraServiceExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
