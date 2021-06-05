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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.threadpool.AbstractTask;

/**
 * {@link SchemaCleanupTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SchemaCleanupTask extends AbstractTask<Map<Integer, List<GuestCleanupTask>>> {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaCleanupTask.class);

    protected final ServiceLookup services;
    protected final int representativeContextId;
    protected final long guestExpiry;

    /**
     * Initializes a new {@link SchemaCleanupTask}.
     *
     * @param services A service lookup reference
     * @param representativeContextId The context ID located in target schema
     * @param guestExpiry the time span (in milliseconds) after which an unused guest user can be deleted permanently
     */
    public SchemaCleanupTask(ServiceLookup services, int representativeContextId, long guestExpiry) {
        super();
        this.services = services;
        this.representativeContextId = representativeContextId;
        this.guestExpiry = guestExpiry;
    }

    @Override
    public Map<Integer, List<GuestCleanupTask>> call() throws Exception {
        try {
            return cleanSchema();
        } catch (OXException e) {
            if ("CTX-0002".equals(e.getErrorCode())) {
                LOG.debug("Context {} no longer found, cancelling cleanup.", I(representativeContextId), e);
                return Collections.emptyMap();
            }
            throw e;
        }
    }

    private Map<Integer, List<GuestCleanupTask>> cleanSchema() throws OXException {
        /*
         * Check if context needs update
         */
        Updater updater = Updater.getInstance();
        UpdateStatus status = updater.getStatus(representativeContextId);
        if (status.needsBackgroundUpdates() || status.needsBlockingUpdates() || status.backgroundUpdatesRunning() || status.blockingUpdatesRunning()) {
            LOG.info("Schema of context {} needs update, skipping cleanup task.", I(representativeContextId));
            return Collections.emptyMap();
        }

        /*
         * gather guest users in context & create appropriate clean-up task for them
         */
        return getCleanUpTasksForSchema();
    }

    private Map<Integer, List<GuestCleanupTask>> getCleanUpTasksForSchema() throws OXException {
        DatabaseService databaseService = services.getService(DatabaseService.class);
        Connection con = databaseService.getReadOnly(representativeContextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id FROM user WHERE guestCreatedBy>0");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                LOG.debug("No guest users found in schema of context {}, skipping cleanup task.", I(representativeContextId));
                return Collections.emptyMap();
            }

            Map<Integer, List<GuestCleanupTask>> cleanUpTasksForSchema = new LinkedHashMap<>(512);
            do {
                Integer contextId = Integer.valueOf(rs.getInt(1));
                List<GuestCleanupTask> cleanupTasks = cleanUpTasksForSchema.get(contextId);
                if (null == cleanupTasks) {
                    cleanupTasks = new LinkedList<>();
                    cleanUpTasksForSchema.put(contextId, cleanupTasks);
                }
                int guestID = rs.getInt(2);
                cleanupTasks.add(new GuestCleanupTask(services, contextId.intValue(), guestID, guestExpiry));
            } while (rs.next());

            if (LOG.isDebugEnabled()) {
                for (Map.Entry<Integer,List<GuestCleanupTask>> entry : cleanUpTasksForSchema.entrySet()) {
                    LOG.debug("Found {} guest users in context {}, preparing corresponding cleanup tasks.", I(entry.getValue().size()), entry.getKey());
                }
            }

            return cleanUpTasksForSchema;
        } catch (SQLException e) {
            throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(representativeContextId, con);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + representativeContextId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SchemaCleanupTask)) {
            return false;
        }
        SchemaCleanupTask other = (SchemaCleanupTask) obj;
        if (representativeContextId != other.representativeContextId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SchemaCleanupTask [representativeContextId=" + representativeContextId + "]";
    }

}
