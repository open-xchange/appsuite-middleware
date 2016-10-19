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

package com.openexchange.cluster.lock.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CleanupStaleClusterLocks}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CleanupStaleClusterLocks implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupStaleClusterLocks.class);

    private ServiceLookup services;

    /** The amount of time to wait until logging a progress event */
    private static final long LOG_DELAY = TimeUnit.SECONDS.toMillis(15);

    /** The amount of nanoseconds after which a cluster lock is considered to be stale */
    private static final long LOCK_TTL = TimeUnit.MINUTES.toNanos(30);

    /**
     * Initialises a new {@link CleanupStaleClusterLocks}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public CleanupStaleClusterLocks(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        long now = System.currentTimeMillis();
        try {
            DatabaseService databaseService = services.getService(DatabaseService.class);
            Set<String> schemata = getAllSchemata(databaseService);
            Set<String> processedSchemata = new HashSet<String>(schemata.size());

            ContextService contextService = services.getService(ContextService.class);
            List<Integer> contextIds = contextService.getAllContextIds();

            int done = 0;
            int total = contextIds.size();
            long lastLogTime = now;
            LOGGER.info("Starting the clean up stale cluster locks task. Iterating through {} contexts", total);
            for (Integer cid : contextIds) {
                cleanupContext(now, databaseService, cid, schemata, processedSchemata);
                lastLogTime = logProgress(done++, total, lastLogTime);
            }
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred during the execution of the clean up stale cluster locks task: {}", e.getMessage(), e);
        }
        LOGGER.info("Clean up stale cluster locks task is done. Running time {} seconds.", TimeUnit.SECONDS.convert(System.currentTimeMillis() - now, TimeUnit.MILLISECONDS));
    }

    /**
     * @param databaseService
     * @return
     * @throws SQLException
     * @throws OXException
     */
    private Set<String> getAllSchemata(DatabaseService databaseService) throws SQLException, OXException {
        Connection configDBConnection = databaseService.getReadOnly();
        PreparedStatement ps = configDBConnection.prepareStatement("SELECT DISTINCT db_schema FROM context_server2db_pool");
        ResultSet rs = ps.executeQuery();
        Set<String> schemata = new HashSet<String>();
        while (rs.next()) {
            schemata.add(rs.getString(1));
        }
        return schemata;
    }

    /**
     * Clean ups the stale cluster locks for the specified context
     * 
     * @param now The time now, i.e. since {@link CleanupStaleClusterLocks} task started
     * @param databaseService The {@link DatabaseService}
     * @param cid The context identifier for which to clean up the stale cluster locks
     * @throws OXException if an error is occurred
     */
    private void cleanupContext(long now, DatabaseService databaseService, Integer cid, Set<String> schemata, Set<String> processedSchemata) throws OXException {
        Connection roConnection = null;
        Connection rwConnection = null;
        PreparedStatement preparedStatement = null;
        try {
            int index = 1;
            String schemaName = databaseService.getSchemaName(cid);
            if (processedSchemata.contains(schemaName)) {
                LOGGER.debug("The context '{}' located in schema '{}' was already processed", cid, schemaName);
                return;
            }
            processedSchemata.add(schemaName);
            roConnection = databaseService.getReadOnly(cid);
            if (!DBUtils.tableExists(roConnection, "clusterLocks")) {
                return;
            }
            rwConnection = databaseService.getWritable(cid);
            preparedStatement = rwConnection.prepareStatement("DELETE FROM clusterLock WHERE ? - timestamp > ?");
            preparedStatement.setLong(index++, now);
            preparedStatement.setLong(index++, LOCK_TTL);
            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                LOGGER.info("Found and cleaned up {} stale cluster locks for context {}", rows, cid);
            }
        } catch (SQLException e) {
            ClusterLockExceptionCodes.SQL_ERROR.create(e.getMessage(), e);
        } finally {
            DBUtils.closeResources(null, null, roConnection, true, cid);
            DBUtils.closeResources(null, preparedStatement, rwConnection, false, cid);
        }
    }

    /**
     * Logs the progress if the time is elapsed
     * 
     * @param done The amount of cleaned up contexts
     * @param total The amount of total contexts
     * @param lastLogTime The last time this entry was logged
     * @return The lastLogTime specified or the newest timestamp (if the event was logged)
     */
    private long logProgress(int done, int total, long lastLogTime) {
        long timeNow = System.currentTimeMillis();
        if (timeNow > lastLogTime + LOG_DELAY) {
            LOGGER.info("Progress of clean up stale cluster locks task at {}% ({}/{} contexts)", Integer.valueOf((done * 100 / total)), done, total);
            lastLogTime = timeNow;
        }
        return lastLogTime;
    }
}
