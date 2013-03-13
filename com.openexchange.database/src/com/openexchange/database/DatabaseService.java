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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * Service interface class for accessing the database system.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface DatabaseService extends ConfigDatabaseService {

    /**
     * Returns a read only connection to the database of the specified context.
     * @param ctx Context.
     * @return a read only connection to the database of the specified context.
     * @throws OXException if no connection can be obtained.
     */
    Connection getReadOnly(Context ctx) throws OXException;

    /**
     * Returns a read only connection to the database of the context with the specified identifier.
     * @param contextId identifier of the context.
     * @return a read only connection to the database of the specified context.
     * @throws OXException if no connection can be obtained.
     */
    Connection getReadOnly(int contextId) throws OXException;

    /**
     * Returns a writable connection to the database of the specified context.
     * @param ctx Context.
     * @return a writable connection to the database of the specified context.
     * @throws OXException if no connection can be obtained.
     */
    Connection getWritable(Context ctx) throws OXException;

    /**
     * Returns a writable connection to the database of the context with the specified identifier.
     * @param contextId identifier of the context.
     * @return a writable connection to the database of the specified context.
     * @throws OXException if no connection can be obtained.
     */
    Connection getWritable(int contextId) throws OXException;

    /**
     * Returns a writable connection to the database of the context with the specified identifier. This connection will not have a
     * connection timeout to support long running update tasks.
     * @param contextId identifier of the context.
     * @return a writable connection to the database of the specified context without a connection timeout.
     * @throws OXException if no connection can be obtained.
     */
    Connection getForUpdateTask(int contextId) throws OXException;

    /**
     * This method is for moving contexts only.
     * @param poolId identifier of the database pool.
     * @param schema schema name.
     * @return a connection to the database from the given pool directed to the given schema.
     * @throws OXException if no connection can be obtained.
     */
    Connection get(int poolId, String schema) throws OXException;

    /**
     * This method is only for administrative access to contexts.
     * @param poolId identifier of the database pool.
     * @param schema schema name.
     * @return a connection to the database without a time-out from the given pool directed to the given schema.
     * @throws OXException if no connection can be obtained.
     */
    Connection getNoTimeout(int poolId, String schema) throws OXException;

    /**
     * This method is only for administrative access to contexts.
     * @param poolId identifier of the pool the connection should be returned to.
     * @param con connection to return.
     */
    void backNoTimeoout(int poolId, Connection con);

    /**
     * Returns a read only connection to the database of the specified context to the pool.
     * @param ctx Context.
     * @param con Read only connection to return.
     */
    void backReadOnly(Context ctx, Connection con);

    /**
     * Returns a read only connection to the database of the context with the specified identifier to the pool.
     * @param contextId identifier of the context.
     * @param con Read only connection to return.
     */
    void backReadOnly(int contextId, Connection con);

    /**
     * Returns a writable connection to the database of the specified context to the pool.
     * @param ctx Context.
     * @param con Writable connection to return.
     */
    void backWritable(Context ctx, Connection con);

    /**
     * Returns a writable connection to the database of the context with the specified identifier to the pool.
     * @param contextId identifier of the context.
     * @param con Writable connection to return.
     */
    void backWritable(int contextId, Connection con);

    /**
     * Returns a writable connection to the database of the context with the specified identifier to the pool. This method must be used if
     * the connection is obtained with {@link #getForUpdateTask(int)}.
     * @param contextId identifier of the context.
     * @param con Writable connection to return.
     */
    void backForUpdateTask(int contextId, Connection con);

    /**
     * This method is for moving contexts only.
     * @param poolId identifier of the pool the connection should be returned to.
     * @param con connection to return.
     */
    void back(int poolId, Connection con);

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
     * Invalidates all cached database pooling information for a context. This are especially the assignments to database servers.
     * @param contextId unique identifier of the context.
     * @throws OXException if resolving the server identifier fails.
     */
    void invalidate(int contextId) throws OXException;

    /**
     * Writes a database assignment for a certain context.
     * @param assignment the assignment to write.
     */
    void writeAssignment(Connection con, Assignment assignment) throws OXException;

}
