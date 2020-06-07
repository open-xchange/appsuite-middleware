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
     * Gets a read-only connection for reading from the config database.
     *
     * @return A read-only connection for reading from the config database
     * @throws OXException If no connection can be obtained.
     */
    Connection getReadOnly() throws OXException;

    /**
     * Gets a read-write connection to the config database.
     *
     * @return A read-write connection to the config database
     * @throws OXException If no connection can be obtained
     */
    Connection getWritable() throws OXException;

    /**
     * Gets a read-write connection to the config database.
     * <p>
     * This connection does not have a connection timeout set to allow long running update tasks
     *
     * @return A read-write connection to the config database without a connection timeout
     * @throws OXException If no connection can be obtained
     */
    Connection getForUpdateTask() throws OXException;

    /**
     * Gives back a read-only connection to the config database to the pool.
     *
     * @param con The connection to return
     */
    void backReadOnly(Connection con);

    /**
     * Gives back a read-write connection to the config database to the pool.
     *
     * @param con The connection to return
     */
    void backWritable(Connection con);

    /**
     * Gives back a read-write connection to the config database to the pool that was only used for reading information from the master
     * database server.
     *
     * @param con The connection to return
     */
    void backWritableAfterReading(Connection con);

    /**
     * Gives back a read-write connection to the config database to the pool.
     * <p>
     * This method must be used if the connection is obtained with {@link #getForUpdateTask()}.
     *
     * @param con The read-write connection to return
     */
    void backForUpdateTask(Connection con);

    /**
     * Gives back a read-write connection to the config database to the pool.
     * <p>
     * It should be used to return a writable connection if it was only used for reading information from the master database server.
     * This method must be used if the connection is obtained with {@link #getForUpdateTask()}.
     *
     * @param con The read-write connection to return
     */
    void backForUpdateTaskAfterReading(Connection con);

    /**
     * Lists all identifiers of the contexts associated with specified database pool.
     *
     * @param poolId The identifier of the database pool
     * @param offset The start offset or <code>-1</code> to get full list
     * @param length The max. number of context identifiers to return (starting from offset) or <code>-1</code> to get full list
     * @return All identifiers of associated contexts
     * @throws OXException If context identifiers cannot be returned
     */
    int[] listContexts(int poolId, int offset, int length) throws OXException;

    /**
     * Gets the identifier of the registered server matching the configured <code>SERVER_NAME</code> property.
     *
     * @return The server identifier
     * @throws OXException If there is no such registered server matching configured <code>SERVER_NAME</code> property
     */
    int getServerId() throws OXException;

    /**
     * Gets the configured server name (see <code>SERVER_NAME</code> property in 'system.properties' file)
     *
     * @return The server name
     * @throws OXException If server name is absent
     */
    String getServerName() throws OXException;

    /**
     * Gets the database pool for specified context.
     *
     * @param contextId The identifier of the context for which to return the database pool
     * @return The identifier of the database pool associated with the context
     * @throws OXException If an error occurs trying to determine the database pool
     * @see #getSchemaInfo(int)
     */
    int getWritablePool(int contextId) throws OXException;

    /**
     * Gets the schema name for specified context.
     *
     * @param contextId The identifier of the context for which to return the schema name
     * @return The schema name
     * @throws OXException If an error occurs trying to determine the schema name
     * @see #getSchemaInfo(int)
     */
    String getSchemaName(int contextId) throws OXException;

    /**
     * Gets the schema information (pool identifier and schema name) for specified context.
     *
     * @param contextId The identifier of the context for which to return the schema information
     * @return The schema information (pool identifier and schema name) for the context
     * @throws OXException If an error occurs trying to determine the schema information
     */
    SchemaInfo getSchemaInfo(int contextId) throws OXException;

    /**
     * Finds all contexts their data is stored in the same schema and on the same database like the given one.
     *
     * @param contextId The identifier of a context assigned to a certain database schema
     * @return All contexts having their data in the same schema and on the same database
     * @throws OXException If some problem occurs.
     */
    int[] getContextsInSameSchema(int contextId) throws OXException;

    /**
     * Finds all contexts their data is stored in the same schema and on the same database like the given one.
     *
     * @param con A connection to the config database
     * @param contextId identifier of a context.
     * @return All contexts having their data in the same schema and on the same database.
     * @throws OXException If contexts cannot be returned
     */
    int[] getContextsInSameSchema(Connection con, int contextId) throws OXException;

    /**
     * Finds all contexts their data is stored in the same schema and on the same database like the given one.
     *
     * @param con A connection to the config database
     * @param poolId The identifier of the database host, on which the schema resides
     * @param schema The name of the schema
     * @return All contexts having their data in the same schema and on the same database.
     * @throws OXException If contexts cannot be returned
     */
    int[] getContextsInSchema(Connection con, int poolId, String schema) throws OXException;

    /**
     * Searches for schemas that store less than <code>maxContexts</code> contexts and which are stored on given database identified by
     * <code>poolId</code>.
     *
     * @param con A connection to the config database
     * @param poolId only schema stored on the given database should be considered.
     * @param maxContexts configured maximum allowed contexts for a database schema.
     * @return A list of schemas that are not filled up to the given maximum number of contexts.
     * @throws OXException If reading from the database fails.
     */
    String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts) throws OXException;

    /**
     * Gets the number of contexts per schema that are located in given database identified by <code>poolId</code>.
     *
     * @param con A connection to the config database
     * @param poolId The pool identifier
     * @param maxContexts The configured maximum allowed contexts for a database schema.
     * @return A mapping providing the count per schema
     * @throws OXException If schema count cannot be returned
     */
    Map<String, Integer> getContextCountPerSchema(Connection con, int poolId, int maxContexts) throws OXException;

    /**
     * Invalidates all cached database pooling information for one or more contexts. Especially the assignments to database servers.
     *
     * @param contextIds The unique identifiers of the contexts.
     */
    void invalidate(int... contextIds);

    /**
     * Writes a database assignment for a certain context.
     *
     * @param assignment The assignment to write
     */
    void writeAssignment(Connection con, Assignment assignment) throws OXException;

    /**
     * Deletes a database assignment for a certain context.
     *
     * @param con A writable connection in a transaction to the config database
     * @param contextId The identifier of the context for which the database assignment is ought to be deleted
     */
    void deleteAssignment(Connection con, int contextId) throws OXException;

    /**
     * Acquires a global lock for specified database
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Given connection is required to be in transaction mode.
     * </div>
     * <p>
     *
     * @param con The connection (in transaction mode)
     * @param writePoolId The identifier of the (read-write) database for which to acquire a lock
     * @throws OXException If lock cannot be acquired
     */
    void lock(Connection con, int writePoolId) throws OXException;

    /**
     * Gets all existing schemas in this installation.
     *
     * @param con The connection to the config database
     * @return All existing schemas as a mapping from schema-name to read DB pool ID
     * @throws OXException If all existing schemas cannot be retrieved from config database
     */
    Map<String, Integer> getAllSchemata(Connection con) throws OXException;

}
