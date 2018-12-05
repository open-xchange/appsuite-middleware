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

package com.openexchange.admin.storage.mysqlStorage;

import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;

/**
 * {@link AdminMySQLStorageUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
class AdminMySQLStorageUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AdminMySQLStorageUtil.class);

    //////////////////////////////////// LEASE ////////////////////////////////////

    /**
     * Leases a read-only {@link Connection} for the configuration database
     * 
     * @param cache The {@link AdminCache}
     * @return The leased {@link Connection}
     * @throws StorageException if a pool error is occurred
     */
    static Connection leaseReadConnectionForConfigDB(AdminCache cache) throws StorageException {
        try {
            return cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * Leases a read/write {@link Connection} for the specified context
     * 
     * @param contextId the context identifier
     * @param cache The {@link AdminCache}
     * @return The leased {@link Connection}
     * @throws StorageException if a pool error is occurred
     */
    static Connection leaseConnectionForContext(int contextId, AdminCache cache) throws StorageException {
        try {
            return cache.getConnectionForContext(contextId);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * Leases a read/write {@link Connection} for the specified context. This connection will not have a
     * connection timeout to support long running update tasks.
     * 
     * @param contextId The context identifier
     * @param cache The {@link AdminCache}
     * @return The leased {@link Connection}
     * @throws StorageException if a pool error is occurred
     */
    static Connection leaseWriteContextConnectionWithoutTimeout(int contextId, AdminCache cache) throws StorageException {
        try {
            return cache.getConnectionForContextNoTimeout(contextId);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    //////////////////////////////////// RELEASE ////////////////////////////////////

    /**
     * Releases the specified {@link Connection}
     * 
     * @param connection The {@link Connection} to release
     * @param cache The {@link AdminCache}
     */
    static void releaseConfigDBConnection(Connection connection, AdminCache cache) {
        if (connection == null) {
            return;
        }
        try {
            cache.pushReadConnectionForConfigDB(connection);
        } catch (Exception e) {
            LOG.error("Error pushing configdb connection to pool!", e);
        }
    }

    /**
     * Releases the specified write {@link Connection} for the specified {@link Context}
     * 
     * @param connection The {@link Connection} to release
     * @param context The {@link Context}
     * @param cache The {@link AdminCache}
     */
    static void releaseWriteContextConnection(Connection connection, Context context, AdminCache cache) {
        if (connection == null) {
            return;
        }
        int contextId = context.getId().intValue();
        try {
            cache.pushConnectionForContext(contextId, connection);
        } catch (PoolException e) {
            LOG.error("Error pushing write connection to pool for context {}!", contextId, e);
        }
    }

    /**
     * Releases the specified {@link Connection} for the specified {@link Context} after performing a read
     * 
     * @param connection The {@link Connection}
     * @param contextId The context identifier
     * @param cache The {@link AdminCache}
     */
    static void releaseWriteContextConnectionAfterReading(Connection connection, int contextId, AdminCache cache) {
        if (connection == null) {
            return;
        }
        try {
            cache.pushConnectionForContextAfterReading(contextId, connection);
        } catch (PoolException exp) {
            LOG.error("Pool Error pushing ox read connection to pool!", exp);
        }
    }

    /**
     * Releases the specified {@link Connection} for the specified {@link Context}
     * 
     * @param connection The {@link Connection} to release
     * @param context The {@link Context}
     * @param cache The {@link AdminCache}
     */
    static void releaseWriteContextConnectionWithoutTimeout(Connection connection, int contextId, AdminCache cache) {
        if (connection == null) {
            return;
        }
        try {
            cache.pushConnectionForContextNoTimeout(contextId, connection);
        } catch (PoolException e) {
            LOG.error("Pool Error pushing ox write connection to pool!", e);
        }
    }
}
