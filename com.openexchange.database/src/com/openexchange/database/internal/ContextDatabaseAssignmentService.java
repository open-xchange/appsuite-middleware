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

package com.openexchange.database.internal;

import java.sql.Connection;
import java.util.Map;
import com.openexchange.database.Assignment;
import com.openexchange.exception.OXException;

/**
 * {@link ContextDatabaseAssignmentService}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
interface ContextDatabaseAssignmentService {

    /**
     * Gets a database assignment for a context. If the cache is enabled this method looks into the cache for the assignment and loads it
     * from the database if cache is disabled or the cache doesn't contain the entry.
     *
     * @param contextId The unique identifier of the context.
     * @return the assignment.
     * @throws OXException If getting the assignment fails.
     */
    AssignmentImpl getAssignment(int contextId) throws OXException;

    /**
     * Invalidates the assignments for one or more contexts in the cache.
     * @param con writable connection to the config database in a transaction.
     * @param contextIds The unique identifiers of the contexts
     * @throws OXException If getting the server identifier fails.
     */
    void invalidateAssignment(int... contextIds) throws OXException;

    /**
     * Writes a database assignment for a context into the database. Normally this is done within a transaction on the config database.
     * Therefore a connection to the config database must be given. This connections needs to be to the write host and in a transaction.
     * This method can overwrite existing assignments.
     * @param con writable database connection to the config database.
     * @param assignment database assignment for a context that should be written.
     * @throws OXException if writing to the persistent storage fails.
     */
    void writeAssignment(Connection con, Assignment assignment) throws OXException;

    /**
     * Deletes a database assignment for the given context. This should be done within a transaction on the config database.
     * @param con writable database connection to the config database. This connection should be in a transaction.
     * @param contextId identifier of the context that database assignment should be deleted.
     * @throws OXException if deleting in the persistent storage fails.
     */
    void deleteAssignment(Connection con, int contextId) throws OXException;

    /**
     * Determines all context IDs which reside in given schema.
     * @param con a connection to the config database
     * @param schema the database schema
     * @param writePoolId corresponding write pool ID (master database)
     * @return an array of <code>int</code> representing all retrieved context identifier
     * @throws OXException if there is no connection to the config database slave is available or reading from the database fails.
     */
    int[] getContextsFromSchema(Connection con, int writePoolId, String schema) throws OXException;

    int[] getContextsInDatabase(int poolId) throws OXException;

    String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts) throws OXException;

    /**
     * Gets the number of contexts per schema that are located in given database identified by <code>poolId</code>.
     *
     * @param con The connection to the config database
     * @param poolId The pool identifier
     * @param maxContexts The max. number of contexts per schema
     * @return A maping providing the count per schema
     * @throws OXException If schema count cannot be returned
     */
    Map<String, Integer> getContextCountPerSchema(Connection con, int poolId, int maxContexts) throws OXException;

    void lock(Connection con, int writePoolId) throws OXException;
}
