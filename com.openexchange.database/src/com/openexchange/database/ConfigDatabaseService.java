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

package com.openexchange.database;

import java.sql.Connection;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ConfigDatabaseService}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@SingletonService
public interface ConfigDatabaseService {

    /**
     * Returns a connection for reading from the config database.
     * @return a connection for reading from the config database.
     * @throws OXException if no connection can be obtained.
     */
    Connection getReadOnly() throws OXException;

    /**
     * Returns a connection to the config database.
     * @return a connection to the config database.
     * @throws OXException if no connection can be obtained.
     */
    Connection getWritable() throws OXException;

    /**
     * Returns a writable connection to the config database. This connection will not have a
     * connection timeout to support long running update tasks.
     * @return a writable connection to the config database without a connection timeout.
     * @throws OXException if no connection can be obtained.
     */
    Connection getForUpdateTask() throws OXException;

    /**
     * Returns a read only connection to the config database to the pool.
     * @param con Connection to return.
     */
    void backReadOnly(Connection con);

    /**
     * Returns a writable connection to the config database to the pool.
     * @param con Connection to return.
     */
    void backWritable(Connection con);

    /**
     * Returns a writable connection to the config database to the pool. This method must be used if
     * the connection is obtained with {@link #getForUpdateTask()}.
     * @param con Writable connection to return.
     */
    void backForUpdateTask(Connection con);

    /**
     * Returns a writable connection to the config database to the pool. It should be used to return a writable connection if it was only
     * used for reading information from the master database server.This method must be used if the connection is obtained with
     * {@link #getForUpdateTask()}.
     * @param con Writable connection to return.
     */
    void backForUpdateTaskAfterReading(Connection con);

    int[] listContexts(int poolId) throws OXException;

    int getServerId() throws OXException;

    String getServerName() throws OXException;

    int getWritablePool(int contextId) throws OXException;

    String getSchemaName(int contextId) throws OXException;

    /**
     * Finds all contexts their data is stored in the same schema and on the same database like the given one.
     * @param contextId identifier of a context.
     * @return all contexts having their data in the same schema and on the same database.
     * @throws OXException if some problem occurs.
     */
    int[] getContextsInSameSchema(int contextId) throws OXException;

    /**
     * Finds all contexts their data is stored in the same schema and on the same database like the given one.
     * @param con connection to the config database
     * @param contextId identifier of a context.
     * @return all contexts having their data in the same schema and on the same database.
     * @throws OXException if some problem occurs.
     */
    int[] getContextsInSameSchema(Connection con, int contextId) throws OXException;

    int[] getContextsInSchema(Connection con, int poolId, String schema) throws OXException;

    /**
     * Searches for schemas that store less that <code>maxContexts</code> contexts and that are stored on given database identified by
     * <code>poolId</code>.
     * @param con connection to the config database
     * @param poolId only schema stored on the given database should be considered.
     * @param maxContexts configured maximum allowed contexts for a database schema.
     * @return a list of schemas that are not filled up to the given maximum number of contexts.
     * @throws OXException if reading from the database fails.
     */
    String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts) throws OXException;

    /**
     * Gets the number of contexts per schema that are located in given database identified by <code>poolId</code>.
     *
     * @param con The connection to the config database
     * @param poolId The pool identifier
     * @param maxContexts The configured maximum allowed contexts for a database schema.
     * @return A mapping providing the count per schema
     * @throws OXException If schema count cannot be returned
     */
    Map<String, Integer> getContextCountPerSchema(Connection con, int poolId, int maxContexts) throws OXException;

    /**
     * Invalidates all cached database pooling information for one or more contexts. This are especially the assignments to database servers.
     * @param contextIds unique identifiers of the contexts.
     */
    void invalidate(int... contextIds);

    /**
     * Writes a database assignment for a certain context.
     * @param assignment the assignment to write.
     */
    void writeAssignment(Connection con, Assignment assignment) throws OXException;

    /**
     * Deletes a database assignment for a certain context;
     * @param con writable connection in a transaction to the config database.
     * @param contextId for this context the database assignment is deleted.
     */
    void deleteAssignment(Connection con, int contextId) throws OXException;

    void lock(Connection con, int writePoolId) throws OXException;
}
