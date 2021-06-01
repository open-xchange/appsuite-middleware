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

package com.openexchange.share.impl.cleanup;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.AbstractCleanUpExecution;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;

/**
 * {@link GuestCleanupExecution}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class GuestCleanupExecution extends AbstractCleanUpExecution {

    private static final Logger LOG = LoggerFactory.getLogger(GuestCleanupExecution.class);

    private final ServiceLookup services;
    private final long guestExpiry;

    /**
     * Initializes a new {@link GuestCleanupExecution}.
     *
     * @param services A service lookup reference
     * @param guestExpiry the time span (in milliseconds) after which an unused guest user can be deleted permanently
     */
    public GuestCleanupExecution(ServiceLookup services, long guestExpiry) {
        super();
        this.services = services;
        this.guestExpiry = guestExpiry;
    }

    @Override
    public void executeFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        try {
            cleanupSchema(schema, connectionProvider.getConnection());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, "Interrupted during cleanup");
        } catch (Exception e) {
            if (OXException.class.isInstance(e)) {
                throw (OXException) e;
            }
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected error during cleanup");
        }
    }

    private void cleanupSchema(String schema, Connection connection) throws Exception {
        List<GuestCleanupTask> cleanupTasks = getCleanupTasksForSchema(connection);
        if (cleanupTasks.isEmpty()) {
            LOG.debug("No guest users found in database schema '{}', skipping cleanup task.", schema);
            return;
        }
        LOG.debug("Found {} guest users in database schema '{}', preparing corresponding cleanup tasks.", I(cleanupTasks.size()), schema);
        for (GuestCleanupTask cleanupTask : cleanupTasks) {
            if (Thread.currentThread().isInterrupted()) {
                LOG.info("Interrupting guest cleanup task on schema '{}'.", schema);
                return;
            }
            cleanupTask.call();
        }
    }

    private List<GuestCleanupTask> getCleanupTasksForSchema(Connection connection) throws OXException {
        List<GuestCleanupTask> cleanupTasks = new LinkedList<GuestCleanupTask>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT cid, id FROM user WHERE guestCreatedBy>0;");
            rs = stmt.executeQuery();
            while (rs.next()) {
                cleanupTasks.add(new GuestCleanupTask(services, rs.getInt(1), rs.getInt(2), guestExpiry));
            }
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
        return cleanupTasks;
    }

}
